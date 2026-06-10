package com.itye.mall.entity;

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
public class SeckillItem {
    private Long id;
    private Long activityId;
    private Long productId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer availableStock;
    private Integer limitPerUser;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
