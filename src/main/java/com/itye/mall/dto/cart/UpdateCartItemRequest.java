package com.itye.mall.dto.cart;

import lombok.Data;

@Data
public class UpdateCartItemRequest {
    private Integer quantity;
    private Integer selected;
}
