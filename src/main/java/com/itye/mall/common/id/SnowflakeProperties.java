package com.itye.mall.common.id;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mall.snowflake")
public record SnowflakeProperties(Long workerId, Long datacenterId) {
    public long normalizedWorkerId() {
        return workerId == null ? 1L : workerId;
    }

    public long normalizedDatacenterId() {
        return datacenterId == null ? 1L : datacenterId;
    }
}
