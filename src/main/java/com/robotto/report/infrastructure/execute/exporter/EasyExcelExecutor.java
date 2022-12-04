package com.robotto.report.infrastructure.execute.exporter;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/11/20 01:28
 **/
public class EasyExcelExecutor {

    private static final Logger logger = LoggerFactory.getLogger(EasyExcelExecutor.class);
    private static final ThreadLocal<ExcelWriter> threadLocal = new ThreadLocal<>();

    public static void write(List<List<Object>> sheetData, int sheetNo, long exportId, long searchCount, AtomicInteger executedCount,
                             BiConsumer<String, String> consumer) {
        ExcelWriter excelWriter = threadLocal.get();
        if (excelWriter == null) {
            return;
        }

        WriteSheet writeSheet = EasyExcelFactory.writerSheet(sheetNo, "sheet-"+sheetNo).build();
        try {
            excelWriter.write(sheetData, writeSheet);
            logger.info("export {} pieces of data of sheetNo {} completely", sheetData.size(), sheetNo);
        } catch (Exception ex) {
            // 回写错误
            sheetData = Collections.singletonList(Collections.singletonList("write fail as " + ex.getMessage()));
            excelWriter.write(sheetData, writeSheet);
        } finally {
            executedCount.addAndGet(sheetData.size());
            consumer.accept(String.valueOf(exportId), NumberUtil.decimalFormat("#.##%", 1.0*executedCount.get()/searchCount));
        }
    }

    public static void prepare(ByteArrayOutputStream bOut, List<List<String>> head) {
        threadLocal.set(EasyExcelFactory.write(bOut).head(head).excelType(ExcelTypeEnum.XLSX).autoCloseStream(true).build());
    }

    public static void write(OutputStream out, List<List<String>> head, List<List<Object>> data, String sheet) {
        EasyExcelFactory.write(out).head(head).sheet(sheet).doWrite(data);
    }

    public static void flush() {
        Optional.ofNullable(threadLocal.get()).ifPresent(ExcelWriter::finish);
        threadLocal.remove();
    }
}
