package com.zhanglin.homepage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增 / 编辑作品的入参
 *
 * @author 张林
 */
@Data
@Schema(description = "保存作品入参")
public class ProjectSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目名称", example = "苍穹外卖", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最多 100 个字符")
    private String title;

    @Schema(description = "一句话简介", example = "前后端一体的餐饮外卖系统")
    @Size(max = 200, message = "简介最多 200 个字符")
    private String summary;

    @Schema(description = "详细介绍，支持换行")
    private String description;

    @Schema(description = "封面图地址，留空用占位图")
    @Size(max = 255, message = "封面地址最多 255 个字符")
    private String cover;

    @Schema(description = "技术栈，逗号分隔", example = "Spring Boot,Redis,MySQL")
    @Size(max = 255, message = "技术栈最多 255 个字符")
    private String techStack;

    @Schema(description = "源码仓库地址")
    @Size(max = 255, message = "仓库地址最多 255 个字符")
    private String repoUrl;

    @Schema(description = "在线演示地址")
    @Size(max = 255, message = "演示地址最多 255 个字符")
    private String demoUrl;

    @Schema(description = "时间区间", example = "2026.03 - 2026.06")
    @Size(max = 50, message = "时间区间最多 50 个字符")
    private String period;

    @Schema(description = "排序，越小越靠前", example = "1")
    @Min(value = 0, message = "排序不能小于 0")
    @Max(value = 9999, message = "排序不能超过 9999")
    private Integer sortOrder;

    @Schema(description = "状态：DRAFT 草稿 / PUBLISHED 已发布", example = "PUBLISHED")
    @Pattern(regexp = "DRAFT|PUBLISHED", message = "状态只能是 DRAFT 或 PUBLISHED")
    private String status;

}
