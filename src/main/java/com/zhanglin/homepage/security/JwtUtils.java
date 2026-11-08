package com.zhanglin.homepage.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 生成与解析
 *
 * @author 张林
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    /**
     * 生成 token
     */
    public String generate(LoginUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getExpireDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                // subject 放用户 id，一个 token 只认一个用户
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_NICKNAME, user.getNickname())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析 token，签名不对或已过期都会抛异常
     */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return LoginUser.builder()
                .id(Long.valueOf(claims.getSubject()))
                .username(claims.get(CLAIM_USERNAME, String.class))
                .nickname(claims.get(CLAIM_NICKNAME, String.class))
                .role(Role.of(claims.get(CLAIM_ROLE, String.class)))
                .build();
    }

    /**
     * 有效期（秒），返回给前端方便它提前刷新
     */
    public long getExpireSeconds() {
        return jwtProperties.getExpireDays() * 24 * 60 * 60;
    }

    private SecretKey secretKey() {
        // 密钥太短会抛 WeakKeyException，这是 HS256 的安全要求
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

}
