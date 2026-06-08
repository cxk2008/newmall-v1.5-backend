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
public class InventoryLog {
    private Long id;
    private Long skuId;
    private Integer changeType;
    private Integer quantityChange;
    private Integer stockAfter;
    private Integer lockedStockAfter;
    private String bizType;
    private String bizId;
    private String note;
    private LocalDateTime createdAt;
}
