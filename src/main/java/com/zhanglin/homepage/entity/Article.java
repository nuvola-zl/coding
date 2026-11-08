package com.zhanglin.homepage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文章 / 笔记
 *
 * @author 张林
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要，留空时保存那一刻从正文自动截取 */
    private String summary;

    /** 正文，Markdown 原文——编辑时改的就是这一份 */
    private String content;

    /**
     * 渲染并净化后的 HTML。
     *
     * <p>为什么存两份而不是每次读取时渲染：列表页、详情页都要用 HTML，
     * 每次请求都跑一遍解析 + 白名单过滤，纯属浪费 CPU。写一次读多次，
     * 典型的空间换时间。代价是正文格式规则改了以后要重新刷一遍旧数据，
     * 所以读取时留了一个"空则补渲染"的兜底（见 ArticleServiceImpl）。</p>
     */
    private String contentHtml;

    /** 封面图地址 */
    private String cover;

    /** 标签，英文逗号分隔 */
    private String tags;

    /** 状态：DRAFT / PUBLISHED */
    private String status;

    /** 阅读量 */
    private Integer viewCount;

    /** 预计阅读分钟数，保存时按字数估算 */
    private Integer readingMinutes;

    /** 首次发布时间，草稿为空 */
    private LocalDateTime publishTime;

}
