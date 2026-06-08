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
public class ProductAttributeValue {
    private Long id;
    private Long productId;
    private Long attributeId;
    private String value;
    private LocalDateTime createdAt;
}
