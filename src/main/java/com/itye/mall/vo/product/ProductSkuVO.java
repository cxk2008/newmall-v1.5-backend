package com.itye.mall.vo.product;

import com.itye.mall.entity.ProductSku;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSkuVO {
    private Long id;
    private Long productId;
    private String skuCode;
    private String name;
    private String imageUrl;
    private String specJson;
    private BigDecimal salePrice;
    private BigDecimal marketPrice;
    private BigDecimal costPrice;
    private Integer weightGram;
    private Integer stock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer lowStockThreshold;
    private Integer status;

    public static ProductSkuVO from(ProductSku sku) {
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        int lockedStock = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        return ProductSkuVO.builder()
                .id(sku.getId())
                .productId(sku.getProductId())
                .skuCode(sku.getSkuCode())
                .name(sku.getName())
                .imageUrl(sku.getImageUrl())
                .specJson(sku.getSpecJson())
                .salePrice(sku.getSalePrice())
                .marketPrice(sku.getMarketPrice())
                .costPrice(sku.getCostPrice())
                .weightGram(sku.getWeightGram())
                .stock(stock)
                .lockedStock(lockedStock)
                .availableStock(Math.max(0, stock - lockedStock))
                .lowStockThreshold(sku.getLowStockThreshold())
                .status(sku.getStatus())
                .build();
    }
}
