package com.itye.mall.vo.order;

import com.itye.mall.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private Integer sourceType;
    private Long sourceId;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private BigDecimal productAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;

    public static OrderVO from(Order order, List<OrderItemVO> items) {
        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .sourceType(order.getSourceType())
                .sourceId(order.getSourceId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverProvince(order.getReceiverProvince())
                .receiverCity(order.getReceiverCity())
                .receiverDistrict(order.getReceiverDistrict())
                .receiverAddress(order.getReceiverAddress())
                .productAmount(order.getProductAmount())
                .freightAmount(order.getFreightAmount())
                .discountAmount(order.getDiscountAmount())
                .payableAmount(order.getPayableAmount())
                .paidAmount(order.getPaidAmount())
                .remark(order.getRemark())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .closedAt(order.getClosedAt())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
