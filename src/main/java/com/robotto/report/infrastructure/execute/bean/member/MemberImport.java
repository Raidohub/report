package com.robotto.report.infrastructure.execute.bean.member;

import com.alibaba.excel.annotation.ExcelProperty;
import com.robotto.base.constant.ValidationMsg;
import com.robotto.crm.constant.ClientType;
import com.robotto.crm.constant.GenderEnum;
import com.robotto.crm.constant.MemberStatus;
import com.robotto.report.infrastructure.execute.bean.BaseImport;
import com.robotto.report.infrastructure.execute.converter.ConverterMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/7 09:05
 **/
@Setter
@Getter
@ToString
public class MemberImport extends BaseImport {

    private static final long serialVersionUID = 719096341707005427L;

    @NotNull(message = ValidationMsg.USERNAME_NOT_NULL)
    private String username;
    @NotNull(message = ValidationMsg.NICKNAME_NOT_NULL)
    private String nickname;
    private String phoneNo;
    private String password;
    @ExcelProperty(value = "性别", converter = ConverterMap.GenderConverter.class)
    private GenderEnum gender;
    private String emailAdd;
    @ExcelProperty(value = "客户端", converter = ConverterMap.ClientTypeConverter.class)
    private ClientType clientType;
    private Date birthday;
    private Integer level;
    private Integer rewardPoint;
    @ExcelProperty(value = "状态", converter = ConverterMap.MemberStatusConverter.class)
    private MemberStatus status;
}
