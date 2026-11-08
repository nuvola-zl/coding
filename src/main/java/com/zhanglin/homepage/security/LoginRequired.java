package com.zhanglin.homepage.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口需要登录才能访问。
 *
 * <pre>
 * 用法：
 *   &#64;LoginRequired                     // 登录即可
 *   &#64;LoginRequired(minRole = Role.ADMIN) // 必须是管理员
 * </pre>
 *
 * 加在类上表示该 Controller 所有接口都需要登录。
 *
 * @author 张林
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    /** 访问该接口所需的最低角色 */
    Role minRole() default Role.USER;

}
