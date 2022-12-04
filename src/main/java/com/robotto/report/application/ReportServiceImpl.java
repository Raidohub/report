package com.robotto.report.application;


import com.robotto.base.infrastructure.general.utils.ApplicationUtil;
import com.robotto.report.api.dto.request.ExportReqQry;
import com.robotto.report.application.service.ReportService;
import com.robotto.report.infrastructure.constant.ExecuteType;
import com.robotto.report.infrastructure.execute.exporter.Exporter;
import com.robotto.report.infrastructure.execute.importer.Importer;
import com.robotto.report.infrastructure.persistence.po.ImportLogPo;
import com.robotto.report.infrastructure.persistence.repo.ExportLogRepo;
import com.robotto.report.infrastructure.persistence.repo.ImportLogRepo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 16:18
 **/
@DubboService
public class ReportServiceImpl implements ReportService {

    private static final String EXPORT_TYPE_SUFFIX = "Exporter";
    private static final String IMPORT_TYPE_SUFFIX = "Importer";

    @Autowired
    private ExportLogRepo exportLogRepo;
    @Autowired
    private ImportLogRepo importLogRepo;

    @Override
    public void doImport(MultipartFile file, ExecuteType importType, HttpServletResponse response) throws Exception {
        String beanName = importType.name().toLowerCase(Locale.ROOT) + IMPORT_TYPE_SUFFIX;
        Importer importer = ApplicationUtil.getBean(beanName);

        ImportLogPo importLogPo = new ImportLogPo();
        importLogPo.setType(importType);
        importLogPo.setFileSize(file.getSize());
        importLogPo.setSourceFileMd5(this.calMd5(file));
        importLogPo.setSourceFilename(file.getOriginalFilename());
        try (InputStream input = file.getInputStream()) {
            importer.execute(input, importLogPo, response);
        }
    }

    @Override
    public void doExport(ExportReqQry exportReqQry, HttpServletResponse response) throws Exception {
        String beanName = exportReqQry.getType().name().toLowerCase(Locale.ROOT) + EXPORT_TYPE_SUFFIX;
        Exporter exporter = ApplicationUtil.getBean(beanName);
        exporter.execute(exportReqQry, response);
    }

    @Override
    public void template(ExecuteType executeType, String filename, HttpServletResponse response) throws IOException {
        String beanName = executeType.name().toLowerCase(Locale.ROOT) + EXPORT_TYPE_SUFFIX;
        Exporter exporter = ApplicationUtil.getBean(beanName);
        exporter.template(executeType, filename, response);
    }

    @Override
    public void cancel(long exportId) {
        String beanName = ExecuteType.MEMBER.name().toLowerCase(Locale.ROOT) + EXPORT_TYPE_SUFFIX;
        Exporter exporter = ApplicationUtil.getBean(beanName);
        exporter.cancelTask(exportId);
    }

    @Override
    public void terminal(long exportId) {
        String beanName = ExecuteType.MEMBER.name().toLowerCase(Locale.ROOT) + EXPORT_TYPE_SUFFIX;
        Exporter exporter = ApplicationUtil.getBean(beanName);
        exporter.terminalFuture(exportId);
    }

    private String calMd5(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        //获取文件的byte信息
        byte[] uploadBytes = file.getBytes();
        // 拿到一个MD5转换器
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(uploadBytes);
        //转换为16进制
        return new BigInteger(1, digest).toString(16);
    }
}
