package com.robotto.report.infrastructure.persistence.repo;

import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.persistence.po.ExportLogPo;

public interface ExportLogRepo {

    ExportLogPo byId(Long id);

    int updateStatus(Long id, ExecuteStatus status);

    void insert(ExportLogPo exportLogPo);

    void update(ExportLogPo exportLogPo, long searchCount);
}
