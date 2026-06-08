package com.itye.mall.vo.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartVO {
    private List<CartItemVO> items;
    private Integer selectedCount;
    private BigDecimal selectedAmount;
}
