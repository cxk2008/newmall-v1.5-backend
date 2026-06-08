package com.itye.mall.security;

import com.itye.mall.entity.AdminUser;
import com.itye.mall.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(getExpirationSeconds());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("accountType", "user")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAdminToken(AdminUser adminUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(getExpirationSeconds());
        return Jwts.builder()
                .subject(String.valueOf(adminUser.getId()))
                .claim("username", adminUser.getUsername())
                .claim("accountType", "admin")
                .claim("role", adminUser.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    public Long parseUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String parseAccountType(String token) {
        Object accountType = parseClaims(token).get("accountType");
        return accountType == null ? "user" : accountType.toString();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public Long getExpirationSeconds() {
        Long configured = jwtProperties.expirationSeconds();
        return configured == null ? 604800L : configured;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.secret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret 至少需要 32 个字符");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
