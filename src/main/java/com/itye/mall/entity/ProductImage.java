package com.itye.mall.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private Long id;
    private Long productId;
    private Long skuId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
