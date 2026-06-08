package com.itye.mall.dto.admin;

import lombok.Data;

@Data
public class StockAdjustRequest {
    private Integer quantity;
    private String note;
}
