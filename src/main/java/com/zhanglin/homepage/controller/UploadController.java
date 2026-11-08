package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.Result;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.vo.UploadResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传接口（仅管理员）
 *
 * <p>注意是 multipart/form-data，前端要用 FormData 而不是 JSON。
 *
 * @author 张林
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@LoginRequired(minRole = Role.ADMIN)
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "上传图片", description = "只接受图片，单张上限 5MB；需要管理员登录",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public Result<UploadResultVO> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务目录，如 avatar / gallery") @RequestParam(value = "dir", defaultValue = "common") String dir) {
        return Result.ok("上传成功", uploadService.uploadImage(file, dir));
    }

}
