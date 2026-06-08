package com.itye.mall.dto.admin;

import lombok.Data;

@Data
public class AdminCategoryRequest {
    private Long parentId;
    private String name;
    private String iconUrl;
    private String bannerUrl;
    private Integer sortOrder;
    private Integer status;
}
