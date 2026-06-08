package com.itye.mall.vo.shipment;

import com.itye.mall.entity.Shipment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShipmentVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String logisticsCompany;
    private String logisticsNo;
    private Integer status;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;

    public static ShipmentVO from(Shipment shipment) {
        return ShipmentVO.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .orderNo(shipment.getOrderNo())
                .logisticsCompany(shipment.getLogisticsCompany())
                .logisticsNo(shipment.getLogisticsNo())
                .status(shipment.getStatus())
                .shippedAt(shipment.getShippedAt())
                .receivedAt(shipment.getReceivedAt())
                .build();
    }
}
