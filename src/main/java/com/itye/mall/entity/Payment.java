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
public class Payment {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Integer channel;
    private BigDecimal amount;
    private Integer status;
    private String transactionId;
    private LocalDateTime paidAt;
    private String callbackPayload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
