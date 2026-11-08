package com.zhanglin.homepage.common.enums;

import lombok.Getter;

/**
 * 内容发布状态。作品（project）、图片（gallery）、文章（article）共用这一套。
 *
 * <p>本来叫 ProjectStatus，做文章模块时发现三个表都在用，名字就名不副实了，
 * 改成 ContentStatus。这类"共用枚举"一旦被多个模块引用，改名成本会随引用数量增长，
 * 所以趁引用还只有两处的时候顺手改掉。</p>
 *
 * @author 张林
 */
@Getter
public enum ContentStatus {

    /** 草稿：只有自己能看到，前台不展示 */
    DRAFT("草稿"),

    /** 已发布：访客可见 */
    PUBLISHED("已发布");

    private final String label;

    ContentStatus(String label) {
        this.label = label;
    }

    /**
     * 字符串转枚举，非法值按草稿处理（宁可看不见，也不要误发布）
     */
    public static ContentStatus of(String name) {
        if (name == null) {
            return DRAFT;
        }
        for (ContentStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return DRAFT;
    }

    /**
     * 页面下拉框直接用
     */
    public static ContentStatus[] all() {
        return values();
    }

}
