package com.robotto.report.application.service;

import com.robotto.report.api.dto.request.ExportReqQry;
import com.robotto.report.infrastructure.constant.ExecuteType;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ReportService {

    void doExport(ExportReqQry exportReqQry, HttpServletResponse response) throws Exception;

    void doImport(MultipartFile file, ExecuteType importType, HttpServletResponse response) throws Exception;

    void template(ExecuteType executeType, String filename, HttpServletResponse response) throws IOException;

    void cancel(long exportId);

    void terminal(long exportId);
}
