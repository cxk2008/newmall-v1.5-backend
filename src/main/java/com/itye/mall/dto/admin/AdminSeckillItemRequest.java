package com.itye.mall.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminSeckillItemRequest {
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
    private Integer sortOrder;
    private Integer status;
}
