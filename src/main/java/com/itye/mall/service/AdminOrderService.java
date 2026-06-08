package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.CloseOrderRequest;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.OrderItem;
import com.itye.mall.mapper.OrderItemMapper;
import com.itye.mall.mapper.OrderMapper;
import com.itye.mall.vo.order.OrderItemVO;
import com.itye.mall.vo.order.OrderVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final CouponService couponService;

    public AdminOrderService(OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper,
                             OrderService orderService,
                             InventoryService inventoryService,
                             CouponService couponService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.couponService = couponService;
    }

    public PageResult<OrderVO> list(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<OrderVO> records = orderMapper.selectAdminPage(
                status,
                keyword,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        ).stream().map(order -> OrderVO.from(order, loadItems(order.getId()))).toList();
        return PageResult.<OrderVO>builder()
                .total(orderMapper.countAdmin(status, keyword))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public OrderVO detail(Long id) {
        Order order = requireOrder(id);
        return OrderVO.from(order, loadItems(order.getId()));
    }

    @Transactional
    public OrderVO close(Long adminId, Long id, CloseOrderRequest request) {
        Order order = requireOrder(id);
        if (Integer.valueOf(OrderStatus.CLOSED).equals(order.getStatus())) {
            return detail(id);
        }
        if (Integer.valueOf(OrderStatus.SHIPPED).equals(order.getStatus())
                || Integer.valueOf(OrderStatus.COMPLETED).equals(order.getStatus())
                || Integer.valueOf(OrderStatus.REFUNDED).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前订单状态不能关闭");
        }
        if (Integer.valueOf(OrderStatus.PENDING_PAY).equals(order.getStatus())) {
            for (OrderItem item : orderItemMapper.selectByOrderId(order.getId())) {
                inventoryService.unlockStock(item.getSkuId(), item.getQuantity(), "admin_order_close", order.getOrderNo());
            }
            couponService.releaseByOrder(order.getId());
        }
        String reason = request == null || !StringUtils.hasText(request.getReason())
                ? "后台关闭订单"
                : request.getReason().trim();
        Integer oldStatus = order.getStatus();
        orderMapper.updateById(Order.builder()
                .id(order.getId())
                .status(OrderStatus.CLOSED)
                .closedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        orderService.writeStatusLog(order.getId(), oldStatus, OrderStatus.CLOSED, 2, adminId, reason);
        return detail(id);
    }

    private Order requireOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private List<OrderItemVO> loadItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId).stream().map(OrderItemVO::from).toList();
    }
}
