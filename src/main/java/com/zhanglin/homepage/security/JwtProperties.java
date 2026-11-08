package com.zhanglin.homepage.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置，对应 application.yml 里的 jwt.*
 *
 * @author 张林
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥，HS256 要求至少 32 字节，上线务必换成随机串 */
    private String secret;

    /** token 有效期（天） */
    private long expireDays = 7;

    /** 存放 token 的请求头名称 */
    private String header = "Authorization";

    /** token 前缀 */
    private String prefix = "Bearer ";

}
