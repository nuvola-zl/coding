package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查，用来确认服务是否正常启动
 *
 * @author 张林
 */
@Tag(name = "健康检查")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name:homepage}")
    private String appName;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("app", appName);
        data.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("javaVersion", System.getProperty("java.version"));
        return Result.ok(data);
    }

}
