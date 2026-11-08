package com.zhanglin.homepage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图库图片
 *
 * @author 张林
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gallery")
public class Gallery extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图片标题 */
    private String title;

    /** 图片说明 */
    private String description;

    /** 图片访问地址 */
    private String url;

    /** 分类：PERSONAL / PROJECT */
    private String category;

    /** 可选：关联的作品 id */
    private Long projectId;

    /** 排序，越小越靠前 */
    private Integer sortOrder;

    /** 状态：DRAFT / PUBLISHED */
    private String status;

}
