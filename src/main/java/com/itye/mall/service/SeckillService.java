package com.itye.mall.service;

import com.itye.mall.common.id.BusinessNoGenerator;
import com.itye.mall.mq.SeckillMessageProducer;
import com.itye.mall.mq.SeckillOrderMessage;
import com.itye.mall.vo.seckill.SeckillSubmitVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SeckillService {
    private static final String SECKILL_SCRIPT = """
            if redis.call('exists', KEYS[1]) == 0 then
                return 3
            end
            if redis.call('exists', KEYS[2]) == 1 then
                return 2
            end
            local stock = tonumber(redis.call('get', KEYS[1]) or '0')
            local quantity = tonumber(ARGV[1])
            if stock < quantity then
                return 1
            end
            redis.call('decrby', KEYS[1], quantity)
            redis.call('set', KEYS[2], ARGV[2], 'EX', tonumber(ARGV[3]))
            return 0
            """;

    private final StringRedisTemplate stringRedisTemplate;
    private final BusinessNoGenerator businessNoGenerator;
    private final SeckillResultService seckillResultService;
    private final SeckillMessageProducer seckillMessageProducer;
    private final Duration userMarkExpire;

    public SeckillService(StringRedisTemplate stringRedisTemplate,
                          BusinessNoGenerator businessNoGenerator,
                          SeckillResultService seckillResultService,
                          SeckillMessageProducer seckillMessageProducer,
                          @Value("${mall.seckill.user-mark-expire-seconds:86400}") Long userMarkExpireSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.businessNoGenerator = businessNoGenerator;
        this.seckillResultService = seckillResultService;
        this.seckillMessageProducer = seckillMessageProducer;
        this.userMarkExpire = Duration.ofSeconds(userMarkExpireSeconds == null ? 86400L : userMarkExpireSeconds);
    }

    public SeckillSubmitVO submit(Long userId, Long seckillItemId) {
        CachedSeckillItem item = loadCachedItem(seckillItemId);
        CachedSeckillActivity activity = loadCachedActivity(item.activityId());
        validateActivity(activity, item);
        String requestNo = businessNoGenerator.generateSeckillRequestNo();
        int quantity = 1;
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(SECKILL_SCRIPT, Long.class),
                List.of(SeckillRedisKeys.stock(item.id()), SeckillRedisKeys.user(activity.id(), item.id(), userId)),
                String.valueOf(quantity),
                requestNo,
                String.valueOf(userMarkExpire.toSeconds())
        );
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "秒杀请求处理失败");
        }
        if (result == 1L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀库存不足");
        }
        if (result == 2L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已参与过本次秒杀");
        }
        if (result == 3L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动尚未准备好");
        }

        seckillResultService.initProcessing(requestNo);
        try {
            seckillMessageProducer.sendCreateOrder(SeckillOrderMessage.builder()
                    .requestNo(requestNo)
                    .activityId(activity.id())
                    .seckillItemId(item.id())
                    .productId(item.productId())
                    .skuId(item.skuId())
                    .userId(userId)
                    .quantity(quantity)
                    .seckillPrice(item.seckillPrice())
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (RuntimeException ex) {
            stringRedisTemplate.opsForValue().increment(SeckillRedisKeys.stock(item.id()), quantity);
            stringRedisTemplate.delete(SeckillRedisKeys.user(activity.id(), item.id(), userId));
            seckillResultService.markFailed(requestNo, "秒杀请求发送失败");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "秒杀请求发送失败");
        }
        return SeckillSubmitVO.builder()
                .requestNo(requestNo)
                .status(SeckillResultService.PROCESSING)
                .build();
    }

    private CachedSeckillItem loadCachedItem(Long seckillItemId) {
        Map<Object, Object> item = stringRedisTemplate.opsForHash().entries(SeckillRedisKeys.item(seckillItemId));
        if (item.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动尚未准备好");
        }
        return new CachedSeckillItem(
                requiredLong(item, "id"),
                requiredLong(item, "activityId"),
                requiredLong(item, "productId"),
                requiredLong(item, "skuId"),
                requiredBigDecimal(item, "seckillPrice"),
                requiredInteger(item, "status")
        );
    }

    private CachedSeckillActivity loadCachedActivity(Long activityId) {
        Map<Object, Object> activity = stringRedisTemplate.opsForHash().entries(SeckillRedisKeys.activity(activityId));
        if (activity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动尚未准备好");
        }
        return new CachedSeckillActivity(
                requiredLong(activity, "id"),
                requiredDateTime(activity, "startsAt"),
                requiredDateTime(activity, "endsAt"),
                requiredInteger(activity, "status")
        );
    }

    private void validateActivity(CachedSeckillActivity activity, CachedSeckillItem item) {
        if (!Integer.valueOf(1).equals(item.status())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "秒杀商品不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.startsAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动未开始");
        }
        if (activity.endsAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动已结束");
        }
        if (Integer.valueOf(5).equals(activity.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动已关闭");
        }
    }

    private Long requiredLong(Map<Object, Object> values, String field) {
        return Long.valueOf(requiredString(values, field));
    }

    private Integer requiredInteger(Map<Object, Object> values, String field) {
        return Integer.valueOf(requiredString(values, field));
    }

    private BigDecimal requiredBigDecimal(Map<Object, Object> values, String field) {
        return new BigDecimal(requiredString(values, field));
    }

    private LocalDateTime requiredDateTime(Map<Object, Object> values, String field) {
        return LocalDateTime.parse(requiredString(values, field));
    }

    private String requiredString(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "秒杀活动尚未准备好");
        }
        return String.valueOf(value);
    }

    private record CachedSeckillActivity(Long id, LocalDateTime startsAt, LocalDateTime endsAt, Integer status) {
    }

    private record CachedSeckillItem(Long id, Long activityId, Long productId, Long skuId, BigDecimal seckillPrice, Integer status) {
    }
}
