package com.zhanglin.homepage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增 / 编辑文章的入参
 *
 * @author 张林
 */
@Data
@Schema(description = "保存文章入参")
public class ArticleSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题", example = "逻辑删除的坑", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(max = 150, message = "标题最多 150 个字符")
    private String title;

    @Schema(description = "摘要，留空则自动从正文截取前 120 字")
    @Size(max = 300, message = "摘要最多 300 个字符")
    private String summary;

    @Schema(description = "正文，Markdown 格式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "正文不能为空")
    private String content;

    @Schema(description = "封面图地址（可直接粘贴图库里的图片地址）")
    @Size(max = 255, message = "封面地址最多 255 个字符")
    private String cover;

    @Schema(description = "标签，逗号分隔", example = "Spring Boot,MySQL")
    @Size(max = 255, message = "标签最多 255 个字符")
    private String tags;

    @Schema(description = "状态：DRAFT 草稿 / PUBLISHED 已发布", example = "DRAFT")
    @Pattern(regexp = "DRAFT|PUBLISHED", message = "状态只能是 DRAFT 或 PUBLISHED")
    private String status;

}
