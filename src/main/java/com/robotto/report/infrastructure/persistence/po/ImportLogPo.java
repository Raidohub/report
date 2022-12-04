package com.robotto.report.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import com.robotto.report.infrastructure.constant.ExecuteType;
import lombok.*;
import lombok.ToString;

import java.util.Date;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/3 17:01
 **/
@Setter
@Getter
@ToString
@TableName("import_log")
public class ImportLogPo {

    private Long id;
    private ExecuteType type;
    private ExecuteStatus status;
    private String sourceFilename;
    private String sourceFileMd5;
    private Long fileSize;
    private String fileUrl;
    private String errFileUrl;
    private int count;
    private String platform;
    private Date createdTime;
    private String createdBy;
}