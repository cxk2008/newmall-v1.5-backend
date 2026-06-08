package com.itye.mall.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminCouponRequest {
    private String name;
    private Integer type;
    private BigDecimal faceValue;
    private BigDecimal minOrderAmount;
    private Integer totalQuantity;
    private Integer perUserLimit;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer status;
}
