package com.robotto.report.api.dto.response;

import com.robotto.base.response.base.BaseRes;
import com.robotto.report.infrastructure.constant.ExecuteStatus;
import lombok.*;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 19:19
 **/
@Setter
@Getter
public class ImportRes extends BaseRes {

    private static final long serialVersionUID = -3765298280350339823L;
    private String fileUrl;
    private String errFileUrl;
    private ExecuteStatus status;
}
