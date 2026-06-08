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
public class ProductReview {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long skuId;
    private Integer rating;
    private String content;
    private String imagesJson;
    private Integer isAnonymous;
    private Integer status;
    private String replyContent;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
