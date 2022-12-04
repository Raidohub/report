package com.robotto.report.api;

import com.robotto.base.infrastructure.general.model.result.Result;
import com.robotto.report.api.dto.request.ExportReqQry;
import com.robotto.report.application.service.ReportService;
import com.robotto.report.infrastructure.constant.ExecuteType;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.io.IOException;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/10 00:30
 **/
@RestController
@RequestMapping("/v1/report")
public class ReportController {

    @DubboReference
    private ReportService reportService;

    @PostMapping("/export")
    public void export(@RequestBody ExportReqQry exportReqQry, HttpServletResponse response) throws Exception {
        if (null == exportReqQry.getType()) {
            throw new ValidationException("导出类型不能为空");
        }
        reportService.doExport(exportReqQry, response);
    }

    @PostMapping("/import")
    public void upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "importType") ExecuteType importType,
                                 HttpServletResponse response) throws Exception {
        reportService.doImport(file, importType, response);
    }

    @GetMapping("/template")
    public void template(@RequestParam(value = "executeType") ExecuteType executeType,
                                   @RequestParam(value = "filename") String filename,
                                   HttpServletResponse response) throws IOException {
        reportService.template(executeType, filename, response);
    }

    @DeleteMapping("/cancel")
    public Result<Object> cancel(Long exportId) {
        if (exportId == null) {
            throw new ValidationException("Export ID不能为空");
        }
        reportService.cancel(exportId);
        return Result.ok();
    }

    @DeleteMapping("/terminal")
    public Result<Object> terminal(Long exportId) {
        if (exportId == null) {
            throw new ValidationException("Export ID不能为空");
        }
        reportService.terminal(exportId);
        return Result.ok();
    }
}
