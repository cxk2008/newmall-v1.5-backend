package com.itye.mall.dto.refund;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRefundRequest {
    private Long orderId;
    private Long orderItemId;
    private BigDecimal amount;
    private String reason;
}
