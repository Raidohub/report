package com.robotto.report.infrastructure.constant;

import lombok.Getter;
import java.io.Serializable;

@Getter
public enum ExecuteStatus implements Serializable {
    DELAY("待异步执行"),
    PENDING("待执行"),
    CANCEL("已取消"),
    PROCESSING("执行中"),
    PARTIAL_COMPLETE("部分完成"),
    INTERRUPTED("中断"),
    COMPLETE("完成"),
    FAILED("失败");

    private String name;

    ExecuteStatus(String name) {
        this.name = name;
    }
}
