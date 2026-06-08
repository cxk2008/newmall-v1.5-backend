package com.itye.mall.vo.favorite;

import com.itye.mall.entity.Favorite;
import com.itye.mall.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FavoriteVO {
    private Long id;
    private Long productId;
    private String productName;
    private String subtitle;
    private String mainImageUrl;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer productStatus;
    private LocalDateTime createdAt;

    public static FavoriteVO from(Favorite favorite, Product product) {
        return FavoriteVO.builder()
                .id(favorite.getId())
                .productId(favorite.getProductId())
                .productName(product == null ? null : product.getName())
                .subtitle(product == null ? null : product.getSubtitle())
                .mainImageUrl(product == null ? null : product.getMainImageUrl())
                .priceMin(product == null ? null : product.getPriceMin())
                .priceMax(product == null ? null : product.getPriceMax())
                .productStatus(product == null ? null : product.getStatus())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
