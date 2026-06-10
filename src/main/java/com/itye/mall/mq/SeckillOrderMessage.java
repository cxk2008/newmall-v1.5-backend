package com.itye.mall.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage {
    private String requestNo;
    private Long activityId;
    private Long seckillItemId;
    private Long productId;
    private Long skuId;
    private Long userId;
    private Integer quantity;
    private BigDecimal seckillPrice;
    private LocalDateTime createdAt;
}
