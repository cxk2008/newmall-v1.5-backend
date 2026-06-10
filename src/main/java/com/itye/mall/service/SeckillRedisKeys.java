package com.itye.mall.service;

public final class SeckillRedisKeys {
    private static final String PREFIX = "seckill:";

    private SeckillRedisKeys() {
    }

    public static String activity(Long activityId) {
        return PREFIX + "activity:" + activityId;
    }

    public static String item(Long itemId) {
        return PREFIX + "item:" + itemId;
    }

    public static String stock(Long itemId) {
        return PREFIX + "stock:" + itemId;
    }

    public static String user(Long activityId, Long itemId, Long userId) {
        return PREFIX + "user:" + activityId + ":" + itemId + ":" + userId;
    }

    public static String result(String requestNo) {
        return PREFIX + "result:" + requestNo;
    }

    public static String activityItems(Long activityId) {
        return PREFIX + "activity:items:" + activityId;
    }
}
