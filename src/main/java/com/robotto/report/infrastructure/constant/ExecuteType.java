package com.robotto.report.infrastructure.constant;

import cn.hutool.core.date.DateUtil;
import lombok.Getter;

import java.io.Serializable;

@Getter
public enum ExecuteType implements Serializable {
    MEMBER("会员");

    private String name;

    ExecuteType(String name) {
        this.name = name;
    }

    public String errorFileName() {
        return this.name + "-error-"+ DateUtil.now();
    }
}
