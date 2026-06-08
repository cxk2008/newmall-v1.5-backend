package com.itye.mall.dto.admin;

import lombok.Data;

@Data
public class AdminProductRequest {
    private Long categoryId;
    private Long brandId;
    private String spuCode;
    private String name;
    private String subtitle;
    private String mainImageUrl;
    private String detailHtml;
    private String unit;
    private Integer sortOrder;
    private Integer status;
}
