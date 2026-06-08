package com.itye.mall.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminBannerRequest {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
