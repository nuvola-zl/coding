package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章出参
 *
 * @author 张林
 */
@Data
@Schema(description = "文章")
public class ArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文 Markdown 原文（只有编辑页需要，列表查询不会带出来）")
    private String content;

    @Schema(description = "正文 HTML，已做 XSS 净化，模板里用 th:utext 输出")
    private String contentHtml;

    @Schema(description = "封面图地址")
    private String cover;

    @Schema(description = "标签原始字符串（逗号分隔）")
    private String tags;

    @Schema(description = "标签数组，页面直接 th:each")
    private List<String> tagList;

    @Schema(description = "状态：DRAFT / PUBLISHED")
    private String status;

    @Schema(description = "状态中文名")
    private String statusLabel;

    @Schema(description = "阅读量")
    private Integer viewCount;

    @Schema(description = "预计阅读分钟数")
    private Integer readingMinutes;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "发布时间（格式化字符串）")
    private String publishTimeText;

    @Schema(description = "更新时间（格式化字符串）")
    private String updateTimeText;

    @Schema(description = "上一篇（更早发布的那篇）")
    private ArticleVO prevArticle;

    @Schema(description = "下一篇（更晚发布的那篇）")
    private ArticleVO nextArticle;

}
