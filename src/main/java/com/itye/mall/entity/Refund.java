package com.itye.mall.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
