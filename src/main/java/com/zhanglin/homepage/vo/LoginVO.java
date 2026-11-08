package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功返回
 *
 * @author 张林
 */
@Data
@Builder
@Schema(description = "登录结果")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT token")
    private String token;

    @Schema(description = "token 类型，前端拼在请求头里：Authorization: Bearer <token>")
    private String tokenType;

    @Schema(description = "有效期（秒）")
    private Long expiresIn;

    @Schema(description = "当前用户信息")
    private UserVO user;

}
