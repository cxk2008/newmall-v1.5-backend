package com.itye.mall.common.id;

import cn.hutool.core.lang.Snowflake;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
public class SnowflakeConfig {
    private static final long MIN_ID_PART = 0L;
    private static final long MAX_ID_PART = 31L;

    @Bean
    public Snowflake snowflake(SnowflakeProperties properties) {
        long workerId = properties.normalizedWorkerId();
        long datacenterId = properties.normalizedDatacenterId();
        validate("workerId", workerId);
        validate("datacenterId", datacenterId);
        return new Snowflake(workerId, datacenterId);
    }

    private void validate(String name, long value) {
        if (value < MIN_ID_PART || value > MAX_ID_PART) {
            throw new IllegalArgumentException("mall.snowflake." + name + " 必须在 0 到 31 之间");
        }
    }
}
