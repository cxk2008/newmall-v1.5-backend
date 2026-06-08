package com.itye.mall.vo.review;

import com.itye.mall.entity.ProductReview;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductReviewVO {
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
    private String replyContent;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;

    public static ProductReviewVO from(ProductReview review) {
        return ProductReviewVO.builder()
                .id(review.getId())
                .userId(Integer.valueOf(1).equals(review.getIsAnonymous()) ? null : review.getUserId())
                .orderId(review.getOrderId())
                .orderItemId(review.getOrderItemId())
                .productId(review.getProductId())
                .skuId(review.getSkuId())
                .rating(review.getRating())
                .content(review.getContent())
                .imagesJson(review.getImagesJson())
                .isAnonymous(review.getIsAnonymous())
                .replyContent(review.getReplyContent())
                .repliedAt(review.getRepliedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
