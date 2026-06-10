package com.itye.mall.common.id;

import cn.hutool.core.lang.Snowflake;
import org.springframework.stereotype.Component;

@Component
public class BusinessNoGenerator {
    private static final String ORDER_NO_PREFIX = "O";
    private static final String SECKILL_REQUEST_NO_PREFIX = "S";

    private final Snowflake snowflake;

    public BusinessNoGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    public String generateOrderNo() {
        return ORDER_NO_PREFIX + snowflake.nextIdStr();
    }

    public String generateSeckillRequestNo() {
        return SECKILL_REQUEST_NO_PREFIX + snowflake.nextIdStr();
    }
}
