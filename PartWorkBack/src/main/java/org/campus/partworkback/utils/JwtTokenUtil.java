package org.campus.partworkback.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil {

    // 从配置文件中读取密钥和过期时间
    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 生成安全的密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }

    /**
     * 生成JWT令牌
     * @param claims 自定义负载信息
     * @return JWT令牌字符串
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims) // 设置负载信息
                .issuedAt(new Date()) // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 设置过期时间
                .signWith(getSigningKey(), Jwts.SIG.HS512) // 使用安全的签名算法和密钥
                .compact();
    }

    /**
     * 从令牌中解析负载Claims
     * @param token 令牌
     * @return 负载信息
     */
    public Claims getClaimsFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build();

        return parser.parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌是否有效
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expirationDate = claims.getExpiration();
            // 检查令牌是否过期
            return !expirationDate.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取用户名（假设用户名存储在负载的"sub"字段）
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取过期时间
     * @param token 令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
}
