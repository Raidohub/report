package com.robotto.report.infrastructure.execute.importer;

import com.robotto.report.infrastructure.constant.ExecuteType;
import com.robotto.report.infrastructure.persistence.po.ImportLogPo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public interface Importer {

    ExecuteType type();

    /**
     * 导入的具体实现，无论是csv还是excel都只需实现这个方法
     * @param input
     * @param importLogPo
     * @return
     */
    void execute(InputStream input, ImportLogPo importLogPo, HttpServletResponse response) throws Exception;
}
