package com.zhanglin.homepage.config;

import com.zhanglin.homepage.security.JwtProperties;
import com.zhanglin.homepage.security.JwtUtils;
import com.zhanglin.homepage.security.LoginInterceptor;
import com.zhanglin.homepage.security.TokenCookieUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 相关配置：登录拦截器 + 跨域 + 本地上传目录的静态映射
 *
 * @author 张林
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final TokenCookieUtils tokenCookieUtils;
    private final StorageProperties storageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截器内部只校验标了 @LoginRequired 的方法，所以这里可以全量拦截
        registry.addInterceptor(new LoginInterceptor(jwtUtils, jwtProperties, tokenCookieUtils))
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/error",
                        "/favicon.ico", "/favicon.svg",
                        "/v3/api-docs/**", "/swagger-ui/**");
    }

    /**
     * 本地存储模式下，把磁盘目录映射成 /uploads/** 的 HTTP 地址。
     *
     * <p>用阿里云 OSS 时这里是空转（图片走 https 完整地址），不影响。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = Paths.get(storageProperties.getLocal().getDir())
                .toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(storageProperties.getLocal().getUrlPrefix() + "/**")
                .addResourceLocations(dir);
        log.debug("本地上传目录映射: {} -> {}", storageProperties.getLocal().getUrlPrefix() + "/**", dir);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 用 allowedOriginPatterns 而不是 allowedOrigins，才能和 allowCredentials 共存
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
