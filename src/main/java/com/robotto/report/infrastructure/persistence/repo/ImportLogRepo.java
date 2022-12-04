package com.robotto.report.infrastructure.persistence.repo;

import com.robotto.report.infrastructure.persistence.po.ImportLogPo;

public interface ImportLogRepo {

    void importBefore(ImportLogPo importLogPo);

    void importAfter(ImportLogPo importLogPo);
}
