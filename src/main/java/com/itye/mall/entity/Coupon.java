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
public class Coupon {
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal faceValue;
    private BigDecimal minOrderAmount;
    private Integer totalQuantity;
    private Integer claimedQuantity;
    private Integer usedQuantity;
    private Integer perUserLimit;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
