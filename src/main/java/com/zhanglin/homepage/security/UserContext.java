package com.zhanglin.homepage.security;

import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;

/**
 * 当前登录用户的上下文，靠 ThreadLocal 在请求内传递。
 *
 * <p>由 LoginInterceptor 在 preHandle 里写入、afterCompletion 里清理，
 * 业务代码直接 {@code UserContext.requireUserId()} 拿当前用户即可，不用层层传参。</p>
 *
 * <p><b>注意：</b>ThreadLocal 一定要在请求结束时 clear，否则线程池复用会造成用户信息串号。</p>
 *
 * @author 张林
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户，未登录返回 null
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录直接抛 401
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long requireUserId() {
        return require().getId();
    }

    public static void clear() {
        HOLDER.remove();
    }

}
