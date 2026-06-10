package com.itye.mall.service;

import com.itye.mall.entity.SeckillOrder;
import com.itye.mall.mapper.SeckillOrderMapper;
import com.itye.mall.vo.seckill.SeckillResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
public class SeckillResultService {
    public static final String PROCESSING = "PROCESSING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillOrderMapper seckillOrderMapper;
    private final long resultExpireSeconds;

    public SeckillResultService(StringRedisTemplate stringRedisTemplate,
                                SeckillOrderMapper seckillOrderMapper,
                                @Value("${mall.seckill.result-expire-seconds:1800}") Long resultExpireSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillOrderMapper = seckillOrderMapper;
        this.resultExpireSeconds = resultExpireSeconds == null ? 1800L : resultExpireSeconds;
    }

    public void initProcessing(String requestNo) {
        stringRedisTemplate.opsForHash().put(SeckillRedisKeys.result(requestNo), "status", PROCESSING);
        stringRedisTemplate.expire(SeckillRedisKeys.result(requestNo), resultExpireSeconds, TimeUnit.SECONDS);
    }

    public void markSuccess(String requestNo, Long orderId, String orderNo) {
        String key = SeckillRedisKeys.result(requestNo);
        stringRedisTemplate.opsForHash().put(key, "status", SUCCESS);
        stringRedisTemplate.opsForHash().put(key, "orderId", String.valueOf(orderId));
        stringRedisTemplate.opsForHash().put(key, "orderNo", orderNo);
        stringRedisTemplate.expire(key, resultExpireSeconds, TimeUnit.SECONDS);
    }

    public void markFailed(String requestNo, String reason) {
        String key = SeckillRedisKeys.result(requestNo);
        stringRedisTemplate.opsForHash().put(key, "status", FAILED);
        stringRedisTemplate.opsForHash().put(key, "failureReason", reason == null ? "秒杀失败" : reason);
        stringRedisTemplate.expire(key, resultExpireSeconds, TimeUnit.SECONDS);
    }

    public SeckillResultVO getResult(Long userId, String requestNo) {
        SeckillOrder order = seckillOrderMapper.selectByRequestNoAndUserId(requestNo, userId);
        String key = SeckillRedisKeys.result(requestNo);
        Object status = stringRedisTemplate.opsForHash().get(key, "status");
        if (order == null && status == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "秒杀请求不存在");
        }
        if (order != null) {
            return SeckillResultVO.builder()
                    .requestNo(requestNo)
                    .status(resolveStatus(order.getStatus()))
                    .orderId(order.getOrderId())
                    .orderNo(order.getOrderNo())
                    .failureReason(order.getFailureReason())
                    .build();
        }
        return SeckillResultVO.builder()
                .requestNo(requestNo)
                .status(String.valueOf(status))
                .orderId(parseLong(stringRedisTemplate.opsForHash().get(key, "orderId")))
                .orderNo(parseString(stringRedisTemplate.opsForHash().get(key, "orderNo")))
                .failureReason(parseString(stringRedisTemplate.opsForHash().get(key, "failureReason")))
                .build();
    }

    private String resolveStatus(Integer status) {
        if (Integer.valueOf(20).equals(status)) {
            return SUCCESS;
        }
        if (Integer.valueOf(30).equals(status) || Integer.valueOf(40).equals(status)) {
            return FAILED;
        }
        return PROCESSING;
    }

    private Long parseLong(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private String parseString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
