package com.robotto.report.api.dto.request;

import com.robotto.base.request.base.BaseReqCmd;
import com.robotto.report.infrastructure.constant.ExecuteType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 16:33
 **/
@Setter
@Getter
public class ExportReqQry extends BaseReqCmd {

    private static final long serialVersionUID = 4387452723083041104L;
    private ExecuteType type;
    private String filename = "模板文件";
    private String searchCondition;
    private String platform;
    private boolean packed = false;
    private boolean csv = false;
    private boolean multiSheet = false;
    private boolean async = false;
}
