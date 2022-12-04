package com.robotto.report.infrastructure.execute.exporter;

import com.robotto.crm.api.MemberService;
import com.robotto.crm.dto.request.qry.MemberReqQry;
import com.robotto.report.infrastructure.constant.ExecuteType;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author robotto
 * @version 1.0
 * @date 2022/4/7 09:04
 **/

@DubboService(group = "MEMBER")
public class MemberExporter extends DefaultedExporter<MemberReqQry> {

    private final List<List<String>> head = Arrays.asList(
            Collections.singletonList("用户名"),
            Collections.singletonList("昵称"),
            Collections.singletonList("电话号码"),
            Collections.singletonList("性别"),
            Collections.singletonList("邮箱"),
            Collections.singletonList("客户端"),
            Collections.singletonList("生日"),
            Collections.singletonList("等级"),
            Collections.singletonList("积分"),
            Collections.singletonList("状态")
    );

    private final String[] headKey = new String[]{"username", "nickname", "phoneNo", "gender", "emailAdd", "clientType",
            "birthday", "level", "rewardPoint", "status"};

    @DubboReference
    private MemberService memberService;

    @Override
    public ExecuteType type() {
        return ExecuteType.MEMBER;
    }

    @Override
    public List<List<String>> head() {
        return head;
    }

    @Override
    public String[] headKey() {
        return headKey;
    }

    @Override
    protected MemberReqQry getPageCondition(String searchCondition) throws InstantiationException, IllegalAccessException {
        MemberReqQry pageCondition = super.getPageCondition(searchCondition);
//        pageCondition.setLevel(0);
        return pageCondition;
    }

    @Override
    protected List<?> data(MemberReqQry memberReqQry) {
        return memberService.memberList(memberReqQry);
    }

    @Override
    protected long dataCount(MemberReqQry memberReqQry) {
        return memberService.memberCount(memberReqQry);
    }
}
