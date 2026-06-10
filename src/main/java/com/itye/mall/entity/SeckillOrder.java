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
public class SeckillOrder {
    private Long id;
    private String requestNo;
    private Long activityId;
    private Long seckillItemId;
    private Long userId;
    private Long orderId;
    private String orderNo;
    private Integer status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
