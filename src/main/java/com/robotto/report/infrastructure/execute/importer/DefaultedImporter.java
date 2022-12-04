package com.robotto.report.infrastructure.execute.importer;

import cn.hutool.core.util.TypeUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.robotto.base.infrastructure.general.exception.BizException;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.execute.bean.BaseImport;
import com.robotto.report.infrastructure.persistence.po.ImportLogPo;
import com.robotto.report.infrastructure.persistence.repo.ImportLogRepo;
import com.robotto.report.infrastructure.properties.ReportProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 14R25
 **/
public abstract class DefaultedImporter<T extends BaseImport> implements Importer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultedImporter.class);
    private final ExecutorService singleExecutor;
    private final Class<T> importClazz;

    @Autowired
    private ReportProp reportProp;
    @Autowired
    public ImportLogRepo importLogRepo;

    {
        importClazz = (Class<T>) TypeUtil.getTypeArgument(getClass(), 0);
        singleExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * IO模式导出，表格模式导出
     * @param importLogPo
     * @return
     */
    public void execute(InputStream input, ImportLogPo importLogPo, HttpServletResponse response) throws Exception {
        importLogRepo.importBefore(importLogPo);

        Meta<T> meta = new Meta<>(this::validateForm, this::insert, importLogPo);
        ReadListenerExt<T> listener = new ReadListenerExt<>(meta, this.singleExecutor);
        EasyExcelFactory.read(input, importClazz, listener).sheet().doRead();

        if (null != meta.getFuture()) {
            logger.info("going to wait");
            meta.getFuture().join();
            logger.info("wait...");
        }

        TimeUnit.MILLISECONDS.sleep(300);
        logger.info("wait end");
        this.executeAfter(response, meta);
    }

    /**
     * 执行结束，后摇动作
     */
    private void executeAfter(HttpServletResponse response, Meta<T> meta) {
        importLogRepo.importAfter(meta.getImportLogPo());
        if (meta.getResolveErrorList().isEmpty() && meta.getBizErrorList().isEmpty()) {
            return;
        }

        try (ServletOutputStream out = response.getOutputStream()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment;filename=" +
                    URLEncoder.encode(meta.getImportLogPo().getType().errorFileName(), StandardCharsets.UTF_8.name()) + ".xlsx");
            logger.info("error {} - {}", meta.getResolveErrorList(), meta.getBizErrorList());
            EasyExcelFactory.write(out).sheet(reportProp.sheetName(1)).doWrite(meta.getErrorList());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    // TODO 导入百分比
    protected List<String> insert(List<T> data, ImportLogPo importLogPo) {
        List<String> result = Collections.emptyList();
        if (data.isEmpty()) {
            return result;
        }

        try {
            result = this.batchInsert(data);
            importLogPo.setCount(importLogPo.getCount()+data.size());
            logger.info("{} import {} pieces of data successfully", this.type(), data.size());
        } catch (BizException e) {
            logger.error("import error {}", e.getMessage());
            importLogPo.setStatus(ExecuteStatus.FAILED);
            throw e;
        } finally {
            importLogRepo.importAfter(importLogPo);
        }
        return result;
    }

    protected abstract List<String> batchInsert(List<T> dataList);

    protected abstract String validateForm(T data);
}
