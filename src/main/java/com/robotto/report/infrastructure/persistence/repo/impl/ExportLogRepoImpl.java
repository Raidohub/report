package com.robotto.report.infrastructure.persistence.repo.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.persistence.mapper.ExportLogMapper;
import com.robotto.report.infrastructure.persistence.po.ExportLogPo;
import com.robotto.report.infrastructure.persistence.repo.ExportLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/5 23:04
 **/
@Repository
public class ExportLogRepoImpl implements ExportLogRepo {

    @Autowired
    private ExportLogMapper mapper;

    @Override
    public ExportLogPo byId(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int updateStatus(Long id, ExecuteStatus status) {
        return mapper.update(null, new UpdateWrapper<ExportLogPo>()
                .lambda()
                .eq(ExportLogPo::getId, id)
                .set(ExportLogPo::getStatus, status));
    }

    @Override
    public void insert(ExportLogPo exportLogPo) {
        mapper.insert(exportLogPo);
    }

    @Override
    public void update(ExportLogPo exportLogPo, long searchCount) {
        ExecuteStatus exportStatus = exportLogPo.getStatus();
        if (ExecuteStatus.INTERRUPTED.equals(mapper.selectById(exportLogPo.getId()).getStatus())) {
            exportStatus = ExecuteStatus.INTERRUPTED;
        } else if (0 != exportLogPo.getCount() && exportLogPo.getCount() < searchCount) {
            exportStatus = ExecuteStatus.PARTIAL_COMPLETE;
        } else if (exportLogPo.getCount() == searchCount) {
            exportStatus = ExecuteStatus.COMPLETE;
        } else if (0 == exportLogPo.getCount()) {
            exportStatus = ExecuteStatus.FAILED;
        }
        exportLogPo.setStatus(exportStatus);
        mapper.updateById(exportLogPo);
    }

}
