package com.robotto.report.infrastructure.execute.converter;

import com.robotto.crm.constant.ClientType;
import com.robotto.crm.constant.GenderEnum;
import com.robotto.crm.constant.MemberStatus;

public interface ConverterMap {

    class GenderConverter extends AbstractEnum2StringConverter<GenderEnum> {}
    class ClientTypeConverter extends AbstractEnum2StringConverter<ClientType> {}
    class MemberStatusConverter extends AbstractEnum2StringConverter<MemberStatus> {}
}
