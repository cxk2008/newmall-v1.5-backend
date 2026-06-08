package com.itye.mall.vo.order;

import com.itye.mall.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemVO {
    private Long id;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String skuSpecJson;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAmount;

    public static OrderItemVO from(OrderItem item) {
        return OrderItemVO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .productName(item.getProductName())
                .skuName(item.getSkuName())
                .skuSpecJson(item.getSkuSpecJson())
                .imageUrl(item.getImageUrl())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalAmount(item.getTotalAmount())
                .build();
    }
}
