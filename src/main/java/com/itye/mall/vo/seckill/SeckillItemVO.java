package com.itye.mall.vo.seckill;

import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.entity.SeckillItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SeckillItemVO {
    private Long id;
    private Long activityId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String imageUrl;
    private String specJson;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer availableStock;
    private Integer limitPerUser;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;

    public static SeckillItemVO from(SeckillItem item, Product product, ProductSku sku) {
        if (item == null) {
            return null;
        }
        return SeckillItemVO.builder()
                .id(item.getId())
                .activityId(item.getActivityId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .productName(product == null ? null : product.getName())
                .skuName(sku == null ? null : sku.getName())
                .imageUrl(sku != null && sku.getImageUrl() != null ? sku.getImageUrl() : product == null ? null : product.getMainImageUrl())
                .specJson(sku == null ? null : sku.getSpecJson())
                .originalPrice(sku == null ? null : sku.getSalePrice())
                .seckillPrice(item.getSeckillPrice())
                .seckillStock(item.getSeckillStock())
                .availableStock(item.getAvailableStock())
                .limitPerUser(item.getLimitPerUser())
                .sortOrder(item.getSortOrder())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
