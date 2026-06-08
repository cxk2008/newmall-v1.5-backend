package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.constant.PaymentStatus;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.OrderItem;
import com.itye.mall.entity.Payment;
import com.itye.mall.mapper.OrderMapper;
import com.itye.mall.mapper.PaymentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import com.itye.mall.vo.payment.PaymentVO;

@Service
public class PaymentService {
    private static final DateTimeFormatter PAYMENT_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public PaymentService(PaymentMapper paymentMapper,
                          OrderMapper orderMapper,
                          OrderService orderService,
                          InventoryService inventoryService) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public PaymentVO mockPay(Long userId, Long orderId) {
        Order order = orderService.requireUserOrder(userId, orderId);
        if (!Integer.valueOf(OrderStatus.PENDING_PAY).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前订单不能支付");
        }
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .paymentNo(generatePaymentNo())
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(userId)
                .channel(4)
                .amount(order.getPayableAmount())
                .status(PaymentStatus.SUCCESS)
                .transactionId("MOCK-" + order.getOrderNo())
                .paidAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        paymentMapper.insert(payment);
        for (OrderItem item : orderService.listOrderItems(order.getId())) {
            inventoryService.deductLockedStock(item.getSkuId(), item.getQuantity(), "payment", payment.getPaymentNo());
        }
        orderMapper.updateById(Order.builder()
                .id(order.getId())
                .status(OrderStatus.PAID)
                .paymentMethod(4)
                .paidAmount(order.getPayableAmount())
                .paidAt(now)
                .updatedAt(now)
                .build());
        orderService.writeStatusLog(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.PAID, 1, userId, "模拟支付成功");
        return PaymentVO.from(payment);
    }

    public PaymentVO latestByOrder(Long userId, Long orderId) {
        orderService.requireUserOrder(userId, orderId);
        Payment payment = paymentMapper.selectLatestByOrderId(orderId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "支付记录不存在");
        }
        return PaymentVO.from(payment);
    }

    private String generatePaymentNo() {
        return "P" + LocalDateTime.now().format(PAYMENT_NO_TIME_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
