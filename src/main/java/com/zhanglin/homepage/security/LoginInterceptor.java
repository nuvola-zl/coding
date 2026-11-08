package com.zhanglin.homepage.security;

import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器。
 *
 * <p>处理顺序：<br>
 * 1. 先尝试解析 token（请求头或 Cookie），解析成功就把用户放进 UserContext —— 这一步不强制，
 *    所以公开页面也能知道"当前是谁"，用来决定要不要显示"退出登录"；<br>
 * 2. 再看方法上有没有 @LoginRequired，没有就直接放行；<br>
 * 3. 需要登录但没登录 / 角色不够，按请求类型分别处理：
 *    接口返回 JSON（401/403），页面跳转到登录页。</p>
 *
 * @author 张林
 */
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final TokenCookieUtils tokenCookieUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 非 Controller 方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 跨域预检请求不带 token，必须放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 1. 能解析出用户就放进上下文，解析失败（过期 / 伪造）静默忽略
        LoginUser loginUser = tryParse(request);
        if (loginUser != null) {
            UserContext.set(loginUser);
        }

        // 2. 没有 @LoginRequired 就不管
        LoginRequired required = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (required == null) {
            required = handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
        }
        if (required == null) {
            return true;
        }

        // 3. 需要登录
        if (loginUser == null) {
            return reject(request, response, ResultCode.UNAUTHORIZED, "请先登录后再操作");
        }

        // 4. 角色不够
        if (!loginUser.getRole().atLeast(required.minRole())) {
            return reject(request, response, ResultCode.FORBIDDEN, "当前账号没有修改权限");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程池会复用线程，不清理会把上一个用户的信息带到下一个请求里
        UserContext.clear();
    }

    /**
     * 解析 token，失败返回 null（不抛异常）
     */
    private LoginUser tryParse(HttpServletRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return jwtUtils.parse(token);
        } catch (Exception e) {
            log.debug("token 无效: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 先从 Authorization 头取，取不到再从 Cookie 取 —— 这样同一个拦截器
     * 既能服务前后端分离的接口调用，也能服务服务端渲染的页面请求。
     */
    private String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(headerValue)) {
            String prefix = jwtProperties.getPrefix();
            if (StringUtils.hasText(prefix) && headerValue.startsWith(prefix)) {
                return headerValue.substring(prefix.length()).trim();
            }
            return headerValue.trim();
        }
        return tokenCookieUtils.read(request);
    }

    /**
     * 拒绝访问：接口给 JSON，页面跳登录页
     */
    private boolean reject(HttpServletRequest request, HttpServletResponse response,
                           ResultCode code, String message) throws IOException {
        if (isApiRequest(request)) {
            throw new BusinessException(code, message);
        }

        if (code == ResultCode.FORBIDDEN) {
            response.sendRedirect(request.getContextPath() + "/?denied=1");
            return false;
        }

        // 记住原本想访问的地址，登录成功后直接跳回去
        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            target = target + "?" + query;
        }
        response.sendRedirect(request.getContextPath() + "/login?redirect="
                + URLEncoder.encode(target, StandardCharsets.UTF_8));
        return false;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

}
