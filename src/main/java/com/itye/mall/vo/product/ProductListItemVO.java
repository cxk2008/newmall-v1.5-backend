package com.itye.mall.vo.product;

import com.itye.mall.es.document.ProductEsDocument;
import com.itye.mall.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListItemVO {
    private Long id;
    private Long categoryId;
    private Long brandId;
    private String name;
    private String subtitle;
    private String mainImageUrl;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer salesCount;

    public static ProductListItemVO from(Product product) {
        return ProductListItemVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .mainImageUrl(product.getMainImageUrl())
                .priceMin(product.getPriceMin())
                .priceMax(product.getPriceMax())
                .salesCount(product.getSalesCount())
                .build();
    }

    public static ProductListItemVO from(ProductEsDocument document) {
        return ProductListItemVO.builder()
                .id(document.getId())
                .categoryId(document.getCategoryId())
                .brandId(document.getBrandId())
                .name(document.getName())
                .subtitle(document.getSubtitle())
                .mainImageUrl(document.getMainImageUrl())
                .priceMin(document.getPriceMin())
                .priceMax(document.getPriceMax())
                .salesCount(document.getSalesCount())
                .build();
    }
}
