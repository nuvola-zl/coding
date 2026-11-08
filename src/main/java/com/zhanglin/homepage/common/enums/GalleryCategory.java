package com.zhanglin.homepage.common.enums;

import lombok.Getter;

/**
 * 图库分类
 *
 * @author 张林
 */
@Getter
public enum GalleryCategory {

    /** 个人喜欢：随手拍、生活照、收藏的图 */
    PERSONAL("个人喜欢", "随手拍、收藏的桌面、生活里的碎片"),

    /** 项目图片：作品截图、界面预览 */
    PROJECT("项目图片", "练手项目的界面截图与设计稿");

    private final String label;
    private final String description;

    GalleryCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * 前端传进来的值转枚举，非法值回退到 PERSONAL（避免因为一个参数报 500）
     */
    public static GalleryCategory of(String value) {
        if (value == null) {
            return PERSONAL;
        }
        for (GalleryCategory category : values()) {
            if (category.name().equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return PERSONAL;
    }

}
