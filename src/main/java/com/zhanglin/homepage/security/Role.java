package com.zhanglin.homepage.security;

import lombok.Getter;

/**
 * 角色。等级越大权限越高，用于 @LoginRequired(minRole = ...) 判断。
 *
 * @author 张林
 */
@Getter
public enum Role {

    /** 普通用户：能登录，能看自己的信息，不能改站点内容 */
    USER(1),

    /** 管理员：可以修改站点内容 */
    ADMIN(2);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    /**
     * 当前角色是否不小于 required
     */
    public boolean atLeast(Role required) {
        return required == null || this.level >= required.level;
    }

    /**
     * 字符串转枚举，无法识别的值按最小权限 USER 处理
     */
    public static Role of(String name) {
        if (name == null) {
            return USER;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return USER;
    }

}
