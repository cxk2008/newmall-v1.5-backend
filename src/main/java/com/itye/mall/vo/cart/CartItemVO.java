package com.itye.mall.vo.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemVO {
    private Long id;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String imageUrl;
    private String specJson;
    private BigDecimal salePrice;
    private Integer quantity;
    private Integer selected;
    private Integer availableStock;
    private BigDecimal totalAmount;
}
