package com.zhanglin.homepage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人信息表
 *
 * @author 张林
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("profile")
public class Profile extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 一句话简介 */
    private String headline;

    /** 详细介绍 */
    private String bio;

    /** 邮箱 */
    private String email;

    /** GitHub 主页 */
    private String github;

    /** 所在城市 */
    private String location;

}
