package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人信息出参（返回给前端）
 *
 * @author 张林
 */
@Data
@Schema(description = "个人信息")
public class ProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "一句话简介")
    private String headline;

    @Schema(description = "详细介绍")
    private String bio;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "GitHub 主页")
    private String github;

    @Schema(description = "所在城市")
    private String location;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
