package com.zhanglin.homepage.common.enums;

import lombok.Getter;

/**
 * 内容发布状态。以后文章、技能等模块都复用这一套。
 *
 * @author 张林
 */
@Getter
public enum ProjectStatus {

    /** 草稿：只有自己能看到，主页不展示 */
    DRAFT("草稿"),

    /** 已发布：访客可见 */
    PUBLISHED("已发布");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    /**
     * 字符串转枚举，非法值按草稿处理（宁可看不见，也不要误发布）
     */
    public static ProjectStatus of(String name) {
        if (name == null) {
            return DRAFT;
        }
        for (ProjectStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return DRAFT;
    }

    /**
     * 页面下拉框直接用
     */
    public static ProjectStatus[] all() {
        return values();
    }

}
