package com.zhanglin.homepage.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 把 JWT 写进 HttpOnly Cookie。
 *
 * <p>前后端不分离时，页面跳转不方便带 Authorization 头，用 Cookie 让浏览器自动携带；
 * HttpOnly 表示 JS 读不到，能挡掉大部分 XSS 偷 token 的场景。</p>
 *
 * <p>以后改成前后端分离，前端直接用 Authorization 头即可，token 本身格式不变。</p>
 *
 * @author 张林
 */
@Component
@RequiredArgsConstructor
public class TokenCookieUtils {

    /** Cookie 名称，改这里要同步检查前端有没有硬编码 */
    public static final String COOKIE_NAME = "HOMEPAGE_TOKEN";

    private final JwtProperties jwtProperties;

    /**
     * 登录成功后写入 token
     */
    public void write(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                // 本地 http 调试必须为 false；上线走 https 时改成 true
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(jwtProperties.getExpireDays()))
                // Lax：正常的页面跳转和同站请求会带上 Cookie，跨站 POST 不带，能防一部分 CSRF
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 退出登录：让浏览器立刻丢掉 Cookie
     */
    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 从请求里读取 token，没有返回 null
     */
    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
