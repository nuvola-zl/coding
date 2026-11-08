package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.security.LoginUser;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.security.UserContext;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 页面控制器基类：把当前登录用户放进 model，模板里直接用 ${currentUser}。
 *
 * <p>只给返回 HTML 的 Controller 继承，纯接口（@RestController）不需要，避免多余开销。</p>
 *
 * @author 张林
 */
public abstract class BasePageController {

    @ModelAttribute("currentUser")
    public LoginUser currentUser() {
        return UserContext.get();
    }

    /**
     * 模板里判断是不是管理员用。
     *
     * <p>比在 Thymeleaf 里写 {@code currentUser.role.name() == 'ADMIN'} 稳妥 ——
     * 枚举比较在模板里容易写错，而且这里改一个地方全站生效。
     */
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        LoginUser user = UserContext.get();
        return user != null && user.getRole() == Role.ADMIN;
    }

}
