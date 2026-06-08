package com.itye.mall.vo.admin;

import com.itye.mall.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminProductListItemVO {
    private Long id;
    private Long categoryId;
    private Long brandId;
    private String spuCode;
    private String name;
    private String subtitle;
    private String mainImageUrl;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer salesCount;
    private Integer viewCount;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminProductListItemVO from(Product product) {
        return AdminProductListItemVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .spuCode(product.getSpuCode())
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .mainImageUrl(product.getMainImageUrl())
                .priceMin(product.getPriceMin())
                .priceMax(product.getPriceMax())
                .salesCount(product.getSalesCount())
                .viewCount(product.getViewCount())
                .sortOrder(product.getSortOrder())
                .status(product.getStatus())
                .publishedAt(product.getPublishedAt())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
