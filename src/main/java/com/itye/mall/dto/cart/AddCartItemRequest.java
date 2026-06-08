package com.itye.mall.dto.cart;

import lombok.Data;

@Data
public class AddCartItemRequest {
    private Long skuId;
    private Integer quantity;
}
