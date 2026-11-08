package com.zhanglin.homepage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 图片元信息保存参数（图片本身走 multipart，不在这里）
 *
 * @author 张林
 */
@Data
@Schema(description = "图片元信息")
public class GallerySaveDTO {

    @Schema(description = "标题", example = "雨后的街角")
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    @Schema(description = "说明", example = "2026 年初在杭州拍的")
    @Size(max = 500, message = "说明最多 500 个字符")
    private String description;

    @Schema(description = "分类：PERSONAL / PROJECT", example = "PERSONAL")
    private String category = "PERSONAL";

    @Schema(description = "关联的作品 id，可不填")
    private Long projectId;

    @Schema(description = "排序，越小越靠前")
    private Integer sortOrder = 0;

    @Schema(description = "状态：DRAFT / PUBLISHED", example = "PUBLISHED")
    private String status = "PUBLISHED";

}
