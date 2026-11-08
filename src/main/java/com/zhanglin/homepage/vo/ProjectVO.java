package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品出参
 *
 * @author 张林
 */
@Data
@Schema(description = "作品")
public class ProjectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(description = "项目名称")
    private String title;

    @Schema(description = "一句话简介")
    private String summary;

    @Schema(description = "详细介绍")
    private String description;

    @Schema(description = "封面图地址")
    private String cover;

    @Schema(description = "技术栈原始字符串（逗号分隔）")
    private String techStack;

    @Schema(description = "技术栈数组，页面直接 th:each")
    private List<String> techList;

    @Schema(description = "源码仓库地址")
    private String repoUrl;

    @Schema(description = "演示地址")
    private String demoUrl;

    @Schema(description = "时间区间")
    private String period;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：DRAFT / PUBLISHED")
    private String status;

    @Schema(description = "状态中文名")
    private String statusLabel;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新时间（格式化后的字符串，模板直接显示）")
    private String updateTimeText;

}
