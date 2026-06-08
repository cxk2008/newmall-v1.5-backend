package com.itye.mall.vo.payment;

import com.itye.mall.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentVO {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Integer channel;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime paidAt;

    public static PaymentVO from(Payment payment) {
        return PaymentVO.builder()
                .id(payment.getId())
                .paymentNo(payment.getPaymentNo())
                .orderId(payment.getOrderId())
                .orderNo(payment.getOrderNo())
                .channel(payment.getChannel())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
