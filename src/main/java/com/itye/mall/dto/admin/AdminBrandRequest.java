package com.itye.mall.dto.admin;

import lombok.Data;

@Data
public class AdminBrandRequest {
    private String name;
    private String logoUrl;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
