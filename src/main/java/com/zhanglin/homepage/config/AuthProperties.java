package com.zhanglin.homepage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证相关配置，对应 application.yml 里的 app.auth.*
 *
 * @author 张林
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * 是否开放注册。
     *
     * <p>个人站点建议关掉：整个站点只需要站长一个账号，别人注册了也没有任何权限，
     * 开放注册只会白送攻击面（撞库、刷账号）。
     *
     * <p>关掉后 {@code POST /api/auth/register} 会直接返回 403「注册功能已关闭」，
     * 但**登录不受影响**；站长账号是在关掉之前就建好的。
     * 想临时开放（比如要加个协作者），把开关改回 true 重启即可。
     */
    private boolean registerEnabled = true;

}
