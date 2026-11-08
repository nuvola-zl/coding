package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图库图片展示对象
 *
 * @author 张林
 */
@Data
@Schema(description = "图库图片")
public class GalleryVO {

    private Long id;

    private String title;

    private String description;

    @Schema(description = "图片地址")
    private String url;

    @Schema(description = "分类：PERSONAL / PROJECT")
    private String category;

    @Schema(description = "分类中文名")
    private String categoryLabel;

    private Long projectId;

    @Schema(description = "关联作品标题（可能为空）")
    private String projectTitle;

    private Integer sortOrder;

    private String status;

    @Schema(description = "状态中文名")
    private String statusLabel;

    @Schema(description = "创建时间，yyyy-MM-dd")
    private String createTimeText;

}
