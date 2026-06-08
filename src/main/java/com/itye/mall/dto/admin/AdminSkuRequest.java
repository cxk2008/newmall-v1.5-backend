package com.itye.mall.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminSkuRequest {
    private String skuCode;
    private String name;
    private String imageUrl;
    private String specJson;
    private BigDecimal salePrice;
    private BigDecimal marketPrice;
    private BigDecimal costPrice;
    private Integer weightGram;
    private Integer stock;
    private Integer lowStockThreshold;
    private Integer status;
}
