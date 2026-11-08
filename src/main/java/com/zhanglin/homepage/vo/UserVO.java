package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息出参（不含密码）
 *
 * @author 张林
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色：USER 普通用户 / ADMIN 管理员")
    private String role;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;

}
