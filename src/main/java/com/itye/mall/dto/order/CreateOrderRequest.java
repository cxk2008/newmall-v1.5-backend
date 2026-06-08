package com.itye.mall.dto.order;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private Long skuId;
    private Integer quantity;
    private Long userCouponId;
    private String remark;
}
