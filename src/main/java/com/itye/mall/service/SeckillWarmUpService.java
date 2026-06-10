package com.itye.mall.service;

import com.itye.mall.entity.SeckillActivity;
import com.itye.mall.entity.SeckillItem;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillWarmUpService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final long stockKeyExpireSeconds;

    public SeckillWarmUpService(StringRedisTemplate stringRedisTemplate,
                                RedissonClient redissonClient,
                                @Value("${mall.seckill.stock-key-expire-seconds:86400}") Long stockKeyExpireSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.stockKeyExpireSeconds = stockKeyExpireSeconds == null ? 86400L : stockKeyExpireSeconds;
    }

    public void warmUp(SeckillActivity activity, List<SeckillItem> items) {
        RLock lock = redissonClient.getLock("seckill:warmup:lock:" + activity.getId());
        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("秒杀活动正在预热，请稍后再试");
            }
            String activityKey = SeckillRedisKeys.activity(activity.getId());
            stringRedisTemplate.opsForHash().putAll(activityKey, Map.of(
                    "id", String.valueOf(activity.getId()),
                    "name", activity.getName(),
                    "startsAt", activity.getStartsAt().toString(),
                    "endsAt", activity.getEndsAt().toString(),
                    "status", String.valueOf(activity.getStatus())
            ));
            stringRedisTemplate.expire(activityKey, stockKeyExpireSeconds, TimeUnit.SECONDS);
            stringRedisTemplate.delete(SeckillRedisKeys.activityItems(activity.getId()));

            for (SeckillItem item : items) {
                String itemKey = SeckillRedisKeys.item(item.getId());
                stringRedisTemplate.opsForHash().putAll(itemKey, Map.of(
                        "id", String.valueOf(item.getId()),
                        "activityId", String.valueOf(item.getActivityId()),
                        "productId", String.valueOf(item.getProductId()),
                        "skuId", String.valueOf(item.getSkuId()),
                        "seckillPrice", item.getSeckillPrice().toPlainString(),
                        "limitPerUser", String.valueOf(item.getLimitPerUser()),
                        "status", String.valueOf(item.getStatus())
                ));
                stringRedisTemplate.opsForValue().set(SeckillRedisKeys.stock(item.getId()), String.valueOf(item.getAvailableStock()), stockKeyExpireSeconds, TimeUnit.SECONDS);
                stringRedisTemplate.opsForList().rightPush(SeckillRedisKeys.activityItems(activity.getId()), String.valueOf(item.getId()));
                stringRedisTemplate.expire(itemKey, stockKeyExpireSeconds, TimeUnit.SECONDS);
            }
            stringRedisTemplate.expire(SeckillRedisKeys.activityItems(activity.getId()), stockKeyExpireSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("秒杀活动预热被中断", ex);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void close(Long activityId, List<SeckillItem> items) {
        stringRedisTemplate.delete(SeckillRedisKeys.activity(activityId));
        stringRedisTemplate.delete(SeckillRedisKeys.activityItems(activityId));
        for (SeckillItem item : items) {
            stringRedisTemplate.delete(SeckillRedisKeys.item(item.getId()));
            stringRedisTemplate.delete(SeckillRedisKeys.stock(item.getId()));
        }
    }
}
