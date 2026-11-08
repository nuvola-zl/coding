package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.Result;
import com.zhanglin.homepage.dto.LoginDTO;
import com.zhanglin.homepage.dto.RegisterDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.service.AuthService;
import com.zhanglin.homepage.vo.LoginVO;
import com.zhanglin.homepage.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 *
 * @author 张林
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "注册", description = "系统内第一个注册的账号自动成为管理员")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok("注册成功", authService.register(dto));
    }

    @Operation(summary = "登录", description = "成功后拿到 token，后续请求带上请求头 Authorization: Bearer <token>")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok("登录成功", authService.login(dto));
    }

    @Operation(summary = "获取当前登录用户", security = @SecurityRequirement(name = "bearerAuth"))
    @LoginRequired
    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.ok(authService.currentUser());
    }

    @Operation(summary = "退出登录", description = "JWT 是无状态的，前端把 token 删掉即可", security = @SecurityRequirement(name = "bearerAuth"))
    @LoginRequired
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

}
