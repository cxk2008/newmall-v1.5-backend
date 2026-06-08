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
public class OrderStatusLog {
    private Long id;
    private Long orderId;
    private Integer oldStatus;
    private Integer newStatus;
    private Integer operatorType;
    private Long operatorId;
    private String note;
    private LocalDateTime createdAt;
}
