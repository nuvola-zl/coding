package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.Result;
import com.zhanglin.homepage.dto.ProfileUpdateDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.service.ProfileService;
import com.zhanglin.homepage.vo.ProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息接口
 *
 * @author 张林
 */
@Tag(name = "个人信息")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "获取个人信息")
    @GetMapping
    public Result<ProfileVO> getProfile() {
        return Result.ok(profileService.getProfile());
    }

    @Operation(summary = "修改个人信息", description = "需要管理员登录", security = @SecurityRequirement(name = "bearerAuth"))
    @LoginRequired(minRole = Role.ADMIN)
    @PutMapping
    public Result<ProfileVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        return Result.ok("修改成功", profileService.updateProfile(dto));
    }

    @Operation(summary = "上传头像", description = "multipart/form-data，字段名 file；上传成功后自动写回个人信息",
            security = @SecurityRequirement(name = "bearerAuth"))
    @LoginRequired(minRole = Role.ADMIN)
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public Result<ProfileVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.ok("头像已更新", profileService.updateAvatar(file));
    }

}
