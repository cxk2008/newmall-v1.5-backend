package com.itye.mall.es.document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.itye.mall.entity.Brand;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductAttributeValue;
import com.itye.mall.entity.ProductCategory;
import com.itye.mall.entity.ProductImage;
import com.itye.mall.entity.ProductSku;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEsDocument {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private List<String> attributeValues;
    private List<SkuDocument> skus;

    public static ProductEsDocument from(Product product,
                                         ProductCategory category,
                                         Brand brand,
                                         List<ProductSku> skus,
                                         List<ProductImage> images,
                                         List<ProductAttributeValue> attributes) {
        return ProductEsDocument.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .brandId(product.getBrandId())
                .brandName(brand == null ? null : brand.getName())
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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .imageUrls(images.stream().map(ProductImage::getImageUrl).toList())
                .attributeValues(attributes.stream().map(ProductAttributeValue::getValue).toList())
                .skus(skus.stream().map(SkuDocument::from).toList())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuDocument {
        private Long id;
        private String skuCode;
        private String name;
        private String imageUrl;
        private String specJson;
        private BigDecimal salePrice;
        private Integer availableStock;
        private Integer status;

        public static SkuDocument from(ProductSku sku) {
            int stock = sku.getStock() == null ? 0 : sku.getStock();
            int lockedStock = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
            return SkuDocument.builder()
                    .id(sku.getId())
                    .skuCode(sku.getSkuCode())
                    .name(sku.getName())
                    .imageUrl(sku.getImageUrl())
                    .specJson(sku.getSpecJson())
                    .salePrice(sku.getSalePrice())
                    .availableStock(Math.max(0, stock - lockedStock))
                    .status(sku.getStatus())
                    .build();
        }
    }
}
