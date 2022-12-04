package com.robotto.report.infrastructure.execute.exporter;

import com.robotto.report.api.dto.request.ExportReqQry;
import com.robotto.report.infrastructure.constant.ExecuteType;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface Exporter {

    ExecuteType type();

    List<List<String>> head();

    String[] headKey();

    /**
     * 导出文件
     * @param exportReqQry
     * @param response
     * @throws IOException
     */
    void execute(ExportReqQry exportReqQry, HttpServletResponse response) throws Exception;

    /**
     * 导出模板文件
     * @param executeType
     * @param filename
     * @param response
     * @throws IOException
     */
    void template(ExecuteType executeType, String filename, HttpServletResponse response) throws IOException;

    /**
     * 中断任务
     * @param exportId
     */
    void terminalFuture(Long exportId);

    /**
     * 取消任务
     * @param exportId
     */
    void cancelTask(Long exportId);
}
