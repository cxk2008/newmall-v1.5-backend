package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.constant.ShipmentStatus;
import com.itye.mall.dto.shipment.ShipOrderRequest;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.Shipment;
import com.itye.mall.mapper.OrderMapper;
import com.itye.mall.mapper.ShipmentMapper;
import com.itye.mall.vo.shipment.ShipmentVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ShipmentService {
    private final ShipmentMapper shipmentMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    public ShipmentService(ShipmentMapper shipmentMapper, OrderMapper orderMapper, OrderService orderService) {
        this.shipmentMapper = shipmentMapper;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
    }

    public ShipmentVO getByUserOrder(Long userId, Long orderId) {
        orderService.requireUserOrder(userId, orderId);
        Shipment shipment = shipmentMapper.selectByOrderId(orderId);
        if (shipment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "物流信息不存在");
        }
        return ShipmentVO.from(shipment);
    }

    @Transactional
    public ShipmentVO ship(Long operatorId, Long orderId, ShipOrderRequest request) {
        if (request == null || !StringUtils.hasText(request.getLogisticsCompany()) || !StringUtils.hasText(request.getLogisticsNo())) {
            throw new IllegalArgumentException("物流信息不完整");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!Integer.valueOf(OrderStatus.PAID).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有已支付订单可以发货");
        }
        if (shipmentMapper.selectByOrderId(orderId) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "订单已发货");
        }

        LocalDateTime now = LocalDateTime.now();
        Shipment shipment = Shipment.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .logisticsCompany(request.getLogisticsCompany().trim())
                .logisticsNo(request.getLogisticsNo().trim())
                .status(ShipmentStatus.SHIPPED)
                .shippedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        shipmentMapper.insert(shipment);
        orderMapper.updateById(Order.builder()
                .id(order.getId())
                .status(OrderStatus.SHIPPED)
                .shippedAt(now)
                .updatedAt(now)
                .build());
        orderService.writeStatusLog(order.getId(), OrderStatus.PAID, OrderStatus.SHIPPED, 2, operatorId, "后台订单发货");
        return ShipmentVO.from(shipment);
    }

    @Transactional
    public ShipmentVO receive(Long userId, Long orderId) {
        Order order = orderService.requireUserOrder(userId, orderId);
        if (!Integer.valueOf(OrderStatus.SHIPPED).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有已发货订单可以确认收货");
        }
        Shipment shipment = shipmentMapper.selectByOrderId(orderId);
        if (shipment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "物流信息不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        shipmentMapper.updateById(Shipment.builder()
                .id(shipment.getId())
                .status(ShipmentStatus.RECEIVED)
                .receivedAt(now)
                .updatedAt(now)
                .build());
        orderMapper.updateById(Order.builder()
                .id(order.getId())
                .status(OrderStatus.COMPLETED)
                .completedAt(now)
                .updatedAt(now)
                .build());
        orderService.writeStatusLog(order.getId(), OrderStatus.SHIPPED, OrderStatus.COMPLETED, 1, userId, "用户确认收货");
        return ShipmentVO.from(shipmentMapper.selectByOrderId(orderId));
    }
}
