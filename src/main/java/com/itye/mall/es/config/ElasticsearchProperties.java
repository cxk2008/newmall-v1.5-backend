package com.itye.mall.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mall.elasticsearch")
public record ElasticsearchProperties(
        String host,
        Integer port,
        String scheme,
        String username,
        String password,
        Integer connectTimeoutMillis,
        Integer socketTimeoutMillis
) {
    public String normalizedHost() {
        return host == null || host.isBlank() ? "localhost" : host.trim();
    }

    public int normalizedPort() {
        return port == null ? 9200 : port;
    }

    public String normalizedScheme() {
        return scheme == null || scheme.isBlank() ? "http" : scheme.trim();
    }

    public int normalizedConnectTimeoutMillis() {
        return connectTimeoutMillis == null ? 3000 : connectTimeoutMillis;
    }

    public int normalizedSocketTimeoutMillis() {
        return socketTimeoutMillis == null ? 30000 : socketTimeoutMillis;
    }

    public boolean hasCredentials() {
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
