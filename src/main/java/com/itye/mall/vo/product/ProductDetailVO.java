package com.itye.mall.vo.product;

import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductAttributeValue;
import com.itye.mall.entity.ProductImage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductDetailVO {
    private Long id;
    private Long categoryId;
    private Long brandId;
    private String spuCode;
    private String name;
    private String subtitle;
    private String mainImageUrl;
    private String detailHtml;
    private String unit;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Integer salesCount;
    private Integer viewCount;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime publishedAt;
    private List<ProductSkuVO> skus;
    private List<ProductImage> images;
    private List<ProductAttributeValue> attributes;

    public static ProductDetailVO from(Product product,
                                       List<ProductSkuVO> skus,
                                       List<ProductImage> images,
                                       List<ProductAttributeValue> attributes) {
        return ProductDetailVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .spuCode(product.getSpuCode())
                .name(product.getName())
                .subtitle(product.getSubtitle())
                .mainImageUrl(product.getMainImageUrl())
                .detailHtml(product.getDetailHtml())
                .unit(product.getUnit())
                .priceMin(product.getPriceMin())
                .priceMax(product.getPriceMax())
                .salesCount(product.getSalesCount())
                .viewCount(product.getViewCount())
                .sortOrder(product.getSortOrder())
                .status(product.getStatus())
                .publishedAt(product.getPublishedAt())
                .skus(skus)
                .images(images)
                .attributes(attributes)
                .build();
    }
}
