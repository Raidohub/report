package com.robotto.report.infrastructure.execute.exporter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotto.base.constant.BaseEnum;
import com.robotto.base.infrastructure.general.assembler.BaseAssembler;
import com.robotto.base.infrastructure.general.exception.BizException;
import com.robotto.base.request.base.BaseReqQry;
import com.robotto.report.api.dto.request.ExportReqQry;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.constant.ExecuteType;
import com.robotto.report.infrastructure.persistence.po.ExportLogPo;
import com.robotto.report.infrastructure.persistence.repo.ExportLogRepo;
import com.robotto.report.infrastructure.properties.ReportProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/5 22:30
 **/

public abstract class DefaultedExporter<T extends BaseReqQry> implements Exporter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultedExporter.class);

    private final Map<Long, List<Future<byte[]>>> futureMap;
    private final Class<T> clazz;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ExportLogRepo exportLogRepo;
    @Autowired
    private ExecutorService taskPoolExecutor;
    @Autowired
    private ReportProp reportProp;

    {
        this.clazz = (Class<T>) TypeUtil.getTypeArgument(getClass(), 0);
        this.futureMap = new ConcurrentHashMap<>(256);
    }

    /**
     * IO?????????????????????????????????
     * @param exportReqQry
     * @param response
     * @return
     */
    public void execute(ExportReqQry exportReqQry, HttpServletResponse response) throws Exception {
        T condition = this.getPageCondition(exportReqQry.getSearchCondition());
        condition.setPageSize(reportProp.getExportSheetSize());
        // ????????????
        long searchCount = this.dataCount(condition);

        // ??????????????????
        ExportLogPo exportLogPo = BaseAssembler.toPo(exportReqQry, ExportLogPo::new);
        exportLogPo.setStatus(ExecuteStatus.PROCESSING);
        this.exportLogRepo.insert(exportLogPo);
        // ????????????
        if (searchCount > this.reportProp.getExportAsyncThreshold()) {
            this.exportLogRepo.updateStatus(exportLogPo.getId(), ExecuteStatus.DELAY);
            return;
        }

        // ?????????????????????sheet??????
        int fileNum = this.reportProp.calFileNum(searchCount);
        int sheetNum = this.reportProp.calSheetNum(searchCount);
        this.responseMedia(response, true);
        try (ServletOutputStream out = response.getOutputStream()) {
            exportLogPo.setCount(this.writeFile(out, condition, fileNum, sheetNum, exportLogPo.getId(), searchCount));
        } finally {
            // ?????????????????????map??????????????????????????????????????????
            this.futureMap.remove(exportLogPo.getId());
            this.exportLogRepo.update(exportLogPo, searchCount);
            this.stringRedisTemplate.delete(exportLogPo.getId().toString());
            logger.info("execute {} end!", exportLogPo.getId());
        }
    }

    private long writeFile(OutputStream out, T condition, int fileNum, int sheetNum, long exportId, long searchCount) throws IOException {
        // ??????????????????????????????????????????
        AtomicInteger executedCount = new AtomicInteger(0);
        // futureList????????????????????????????????????????????????
        List<Future<byte[]>> futureList = new ArrayList<>(fileNum);
        this.futureMap.put(exportId, futureList);

        // ??????????????????????????????????????????
        IntStream.range(0,fileNum).boxed().forEach(fileNo -> futureList.add(taskPoolExecutor.submit(() ->
                this.loadSheetByte(condition, fileNo, sheetNum, exportId, searchCount, executedCount))));
        // ?????????????????????
        try (ZipOutputStream zOut = new ZipOutputStream(out)) {
            /**
             * ????????????futureList??????????????????????????????future.get()???????????????????????????future??????????????????future?????????????????????
             * ????????????????????????sheet???????????????
             */
            IntStream.range(0,fileNum).forEach(fileNo -> this.writeSheet(zOut, futureList, fileNo, exportId));
        }
        return executedCount.get();
    }

    private void writeSheet(ZipOutputStream zOut, List<Future<byte[]>> futureList, int fileNo, long exportId) {
        try {
            // future get ????????????????????????????????????
            byte[] byteArr = futureList.get(fileNo).get();
            this.zip(zOut, byteArr, fileNo+1);
        } catch (ExecutionException | InterruptedException | CancellationException e) {
            logger.info("export {} interrupted!, percentage is {}", exportId,
                    this.stringRedisTemplate.opsForValue().get(String.valueOf(exportId)));
        }
    }

    private byte[] loadSheetByte(T condition, int fileNo, int sheetNum, long exportId, long searchCount, AtomicInteger executedCount) throws IOException {
        // ?????????????????????????????????
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            // EasyExcelExecutor?????????????????? EasyExcel ????????????
            EasyExcelExecutor.prepare(bOut, this.head());
            IntStream.range(1,sheetNum+1).forEach(sheetNo -> {
                int index = fileNo*sheetNum+sheetNo;
                List<List<Object>> sheetData = this.loadSheetByte(condition, index);
                if (sheetData.isEmpty()) {
                    return;
                }

                EasyExcelExecutor.write(sheetData, index, exportId, searchCount, executedCount, (k,v) -> this.stringRedisTemplate.opsForValue().set(k,v));
                logger.info("export processing percentage {}", this.stringRedisTemplate.opsForValue().get(String.valueOf(exportId)));
            });
            EasyExcelExecutor.flush();
            return bOut.toByteArray();
        }
    }

    private List<List<Object>> loadSheetByte(T condition, int sheetNo) {
        List<List<Object>> sheetData = Collections.emptyList();
        // ????????????????????????
        if (Thread.interrupted()) {
            logger.info("load sheetNo {} interrupted!", sheetNo);
            return sheetData;
        }

        try {
            if (sheetNo == 2) {
                int a=1/0;
            }
            T conditionCopy = clazz.newInstance();
            BeanUtils.copyProperties(condition, conditionCopy);
            // ????????????set pageNum????????????????????????
            conditionCopy.setPageNum(sheetNo);
            sheetData = this.loadData(conditionCopy);
            logger.info("load {} pieces data of sheetNo {}", sheetData.size(), sheetNo);
            return sheetData;
        } catch (Exception ex) {
            return Collections.singletonList(Collections.singletonList("load fail cause " + ex.getMessage()));
        }
    }

    @Override
    public void terminalFuture(Long exportId) {
        logger.info("attempt to terminal future {}", this.futureMap.get(exportId).size());
        Optional.ofNullable(this.futureMap.get(exportId)).ifPresent(e -> e.forEach(i -> i.cancel(true)));
        this.exportLogRepo.updateStatus(exportId, ExecuteStatus.INTERRUPTED);
    }

    @Override
    public void cancelTask(Long exportId) {
        ExportLogPo exportLogPo = this.exportLogRepo.byId(exportId);
        if (ExecuteStatus.PENDING.equals(exportLogPo.getStatus())) {
            this.exportLogRepo.updateStatus(exportId, ExecuteStatus.CANCEL);
        } else if (ExecuteStatus.PROCESSING.equals(exportLogPo.getStatus())) {
            this.terminalFuture(exportId);
        }
    }

    @Override
    public void template(ExecuteType executeType, String filename, HttpServletResponse response) throws IOException {
        ExportReqQry exportReqQry = new ExportReqQry();
        exportReqQry.setFilename(filename);

        this.responseMedia(response, false);
        EasyExcelExecutor.write(response.getOutputStream(), this.head(), Collections.emptyList(), this.reportProp.sheetName(1));
    }

    private void responseMedia(HttpServletResponse response, boolean zipIf) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String filenameSuffix = ".zip";
        if (!zipIf) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filenameSuffix = ".xlsx";
        }

        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            String filename = URLEncoder.encode(this.type().getName()+"-"+DateUtil.now(), StandardCharsets.UTF_8.name()) + filenameSuffix;
            response.setHeader("Content-disposition", "attachment;filename=" + filename);
        } catch (UnsupportedEncodingException e) {
            logger.error("filename transfer error as {}", e.getMessage());
            throw new BizException("filename transfer error");
        }
    }

    protected T getPageCondition(String searchCondition) throws InstantiationException, IllegalAccessException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(searchCondition, this.clazz);
        } catch (JsonProcessingException e) {
            logger.info("no search condition");
        }
        return this.clazz.newInstance();
    }

    protected List<List<Object>> loadData(T condition) {
        List<?> subData = this.data(condition);
        List<List<Object>> data = new ArrayList<>(subData.size());

        for (Object obj : subData) {
            Field[] fieldArr = obj.getClass().getDeclaredFields();
            List<Object> line = new ArrayList<>(fieldArr.length);
            data.add(line);

            for (String key : this.headKey()) {
                Object fieldValue = ReflectUtil.getFieldValue(obj, key);
                String value;
                if (fieldValue == null) {
                    value = "";
                } else if (fieldValue instanceof BaseEnum) {
                    value = ((BaseEnum) fieldValue).getName();
                } else if (fieldValue instanceof Boolean) {
                    value = Boolean.TRUE.equals(fieldValue) ? "???" : "???";
                } else if (fieldValue instanceof Date) {
                    value = DateUtil.formatDateTime((Date) fieldValue);
                } else {
                    value = fieldValue.toString();
                }
                line.add(value);
            }
        }
        return data;
    }

    public void zip(ZipOutputStream zOut, byte[] byteArr, int fileNo) {
        String filename = null;
        try {
            filename = URLEncoder.encode(this.type().getName()+"-"+fileNo+"-"+DateUtil.now(), StandardCharsets.UTF_8.name()) + ".xlsx";
            ZipEntry zipEntry = new ZipEntry(filename);
            zOut.putNextEntry(zipEntry);
            zOut.write(byteArr);
            zOut.closeEntry();
            zOut.flush();
            logger.info("zip entity {}", filename);
        } catch (Exception e) {
            logger.info("zip {} error {}", filename, e.getMessage());
        }
    }
    protected abstract List<?> data(T condition);

    protected abstract long dataCount(T condition);
}