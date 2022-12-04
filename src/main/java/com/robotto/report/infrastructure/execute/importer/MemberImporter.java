package com.robotto.report.infrastructure.execute.importer;

import com.robotto.base.infrastructure.general.exception.BizException;
import com.robotto.base.infrastructure.general.utils.BeanCopyUtil;
import com.robotto.crm.api.MemberService;
import com.robotto.crm.dto.request.cmd.MemberReqCmd;
import com.robotto.report.infrastructure.constant.ExecuteType;
import com.robotto.report.infrastructure.execute.bean.member.MemberImport;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/5/19 11:08
 **/
@DubboService(group = "MEMBER")
public class MemberImporter extends DefaultedImporter<MemberImport> {

    @DubboReference
    private MemberService memberService;

    @Override
    public ExecuteType type() {
        return ExecuteType.MEMBER;
    }

    @Override
    protected List<String> batchInsert(List<MemberImport> dataList) {
        List<MemberReqCmd> memberReqCmdList = dataList.stream().map(e -> {
            e.setPassword("123456");
            return BeanCopyUtil.copyProperties(e, MemberReqCmd::new);
        }).collect(Collectors.toList());
        dataList.forEach(e -> e.setPassword("123456"));
        return memberService.batchRegister(memberReqCmdList);
    }

    @Override
    protected String validateForm(MemberImport data) {
        MemberReqCmd memberReqCmd = BeanCopyUtil.copyProperties(data, MemberReqCmd::new);
        try {
            memberService.validateForm(memberReqCmd);
        } catch (BizException e) {
            return e.getMessage();
        }
        return null;
    }
}
