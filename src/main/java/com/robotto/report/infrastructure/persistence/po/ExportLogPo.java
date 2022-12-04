package com.robotto.report.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.constant.ExecuteType;
import lombok.*;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
@TableName("export_log")
public class ExportLogPo {

    private Long id;
    private ExecuteType type;
    private ExecuteStatus status;
    private String searchCondition;
    private String filename;
    private String fileUrl;
    private String md5;
    private Long count;
    private String platform;
    private Boolean packed;
    private Date createdTime;
    private String createdBy;
}
