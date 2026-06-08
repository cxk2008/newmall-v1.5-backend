package com.itye.mall.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mall.jwt")
public record JwtProperties(String secret, Long expirationSeconds) {
}
