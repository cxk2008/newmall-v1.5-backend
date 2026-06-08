package com.itye.mall.vo.refund;

import com.itye.mall.entity.Refund;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RefundVO {
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

    public static RefundVO from(Refund refund) {
        return RefundVO.builder()
                .id(refund.getId())
                .refundNo(refund.getRefundNo())
                .orderId(refund.getOrderId())
                .orderItemId(refund.getOrderItemId())
                .userId(refund.getUserId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .handledBy(refund.getHandledBy())
                .handledAt(refund.getHandledAt())
                .refundedAt(refund.getRefundedAt())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }
}
