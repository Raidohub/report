package com.robotto.report.infrastructure.persistence.repo.impl;

import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.persistence.mapper.ImportLogMapper;
import com.robotto.report.infrastructure.persistence.po.ImportLogPo;
import com.robotto.report.infrastructure.persistence.repo.ImportLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 14:31
 **/
@Repository
public class ImportLogRepoImpl implements ImportLogRepo {


    @Autowired
    private ImportLogMapper importLogMapper;

    @Override
    public void importBefore(ImportLogPo importLogPo) {
        importLogPo.setStatus(ExecuteStatus.PROCESSING);
        importLogMapper.insert(importLogPo);
    }

    @Override
    public void importAfter(ImportLogPo importLogPo) {
        if (ExecuteStatus.FAILED.equals(importLogPo.getStatus()) && importLogPo.getCount() > 0) {
            importLogPo.setStatus(ExecuteStatus.PARTIAL_COMPLETE);
        } else if (ExecuteStatus.PROCESSING.equals(importLogPo.getStatus())) {
            importLogPo.setStatus(ExecuteStatus.COMPLETE);
        }
        importLogMapper.updateById(importLogPo);
    }
}
