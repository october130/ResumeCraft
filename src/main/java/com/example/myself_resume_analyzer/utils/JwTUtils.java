package com.example.myself_resume_analyzer.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 负责token的生成、解析、验证
 */
@Component
public class JwTUtils {

    /**
     * 签名密钥，从application.yml中读取 jwt.secret
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * token有效期（毫秒），从application.yml中读取 jwt.expiration
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 根据密钥字符串生成HS256签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT token
     *
     * @param userId   用户ID，存入token的subject字段
     * @param username 用户名，存入token的claim中
     * @return 生成的token字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))       // subject存用户ID
                .claim("username", username)            // 自定义claim存用户名
                .issuedAt(now)                          // 签发时间
                .expiration(expireDate)                 // 过期时间
                .signWith(getSigningKey())              // 签名
                .compact();
    }

    /**
     * 解析token，获取Claims
     *
     * @param token JWT token字符串
     * @return Claims对象，包含token中的所有信息
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())            // 验证签名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证token是否有效（未过期且能正常解析）
     *
     * @param token JWT token字符串
     * @return true=有效，false=无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // token解析失败（过期、签名错误、格式错误等）
            return false;
        }
    }
}
