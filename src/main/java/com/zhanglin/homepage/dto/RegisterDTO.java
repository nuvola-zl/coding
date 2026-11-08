package com.zhanglin.homepage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册入参
 *
 * @author 张林
 */
@Data
@Schema(description = "注册入参")
public class RegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录账号：4-20 位字母、数字或下划线", example = "zhanglin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "账号只能是 4-20 位字母、数字或下划线")
    private String username;

    @Schema(description = "密码：6-32 位", example = "example123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    private String password;

    @Schema(description = "昵称，不填默认用账号", example = "张林")
    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Schema(description = "头像地址")
    @Size(max = 255, message = "头像地址最多 255 个字符")
    private String avatar;

}
