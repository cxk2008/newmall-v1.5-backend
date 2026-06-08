package com.itye.mall.dto.review;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private Long orderItemId;
    private Integer rating;
    private String content;
    private String imagesJson;
    private Integer isAnonymous;
}
