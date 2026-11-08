package com.zhanglin.homepage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改个人信息的入参
 *
 * @author 张林
 */
@Data
@Schema(description = "修改个人信息入参")
public class ProfileUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称", example = "张林", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    @Size(max = 255, message = "头像地址最多 255 个字符")
    private String avatar;

    @Schema(description = "一句话简介", example = "Java 全栈练习中")
    @Size(max = 120, message = "一句话简介最多 120 个字符")
    private String headline;

    @Schema(description = "详细介绍")
    private String bio;

    @Schema(description = "邮箱", example = "zhanglin@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最多 100 个字符")
    private String email;

    @Schema(description = "GitHub 主页", example = "https://github.com/xxx")
    @Size(max = 255, message = "GitHub 地址最多 255 个字符")
    private String github;

    @Schema(description = "所在城市", example = "杭州")
    @Size(max = 100, message = "城市最多 100 个字符")
    private String location;

}
