package com.zhanglin.homepage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作品 / 项目
 *
 * @author 张林
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String title;

    /** 一句话简介，列表页展示 */
    private String summary;

    /** 详细介绍，支持换行 */
    private String description;

    /** 封面图地址 */
    private String cover;

    /** 技术栈，逗号分隔 */
    private String techStack;

    /** 源码仓库地址 */
    private String repoUrl;

    /** 在线演示地址 */
    private String demoUrl;

    /** 时间区间 */
    private String period;

    /** 排序，越小越靠前 */
    private Integer sortOrder;

    /** 状态：DRAFT / PUBLISHED */
    private String status;

}
