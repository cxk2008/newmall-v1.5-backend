package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.refund.AdminRefundReviewRequest;
import com.itye.mall.dto.refund.CreateRefundRequest;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.OrderItem;
import com.itye.mall.entity.Refund;
import com.itye.mall.mapper.OrderItemMapper;
import com.itye.mall.mapper.OrderMapper;
import com.itye.mall.mapper.RefundMapper;
import com.itye.mall.vo.refund.RefundVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RefundService {
    private static final int REFUND_APPLYING = 10;
    private static final int REFUND_APPROVED = 20;
    private static final int REFUND_REJECTED = 30;
    private static final int REFUND_COMPLETED = 40;
    private static final DateTimeFormatter REFUND_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final OperationLogService operationLogService;

    public RefundService(RefundMapper refundMapper,
                         OrderMapper orderMapper,
                         OrderItemMapper orderItemMapper,
                         OrderService orderService,
                         InventoryService inventoryService,
                         OperationLogService operationLogService) {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public RefundVO create(Long userId, CreateRefundRequest request) {
        if (request == null || request.getOrderId() == null || request.getOrderItemId() == null) {
            throw new IllegalArgumentException("退款参数不完整");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于 0");
        }
        if (!StringUtils.hasText(request.getReason())) {
            throw new IllegalArgumentException("退款原因不能为空");
        }

        Order order = orderService.requireUserOrder(userId, request.getOrderId());
        if (!canRefund(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前订单状态不支持退款");
        }
        OrderItem orderItem = requireOrderItem(userId, request.getOrderItemId());
        if (!order.getId().equals(orderItem.getOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "退款明细不属于该订单");
        }
        if (request.getAmount().compareTo(orderItem.getTotalAmount()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "退款金额不能超过可退金额");
        }
        Refund existing = refundMapper.selectByOrderItemId(orderItem.getId());
        if (existing != null && existing.getStatus() != REFUND_REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该订单明细已申请退款");
        }

        Refund refund = Refund.builder()
                .refundNo(generateRefundNo())
                .orderId(order.getId())
                .orderItemId(orderItem.getId())
                .userId(userId)
                .amount(request.getAmount())
                .reason(request.getReason().trim())
                .status(REFUND_APPLYING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        refundMapper.insert(refund);
        orderItemMapper.updateById(OrderItem.builder()
                .id(orderItem.getId())
                .refundStatus(1)
                .build());
        orderService.writeStatusLog(order.getId(), order.getStatus(), order.getStatus(), 1, userId, "用户申请退款");
        return RefundVO.from(refund);
    }

    public PageResult<RefundVO> listByUser(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<RefundVO>builder()
                .total(refundMapper.countByUserId(userId, status))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(refundMapper.selectByUserIdPage(
                        userId,
                        status,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ).stream().map(RefundVO::from).toList())
                .build();
    }

    public RefundVO detailByUser(Long userId, Long id) {
        Refund refund = requireRefund(id);
        if (!refund.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "退款记录不存在");
        }
        return RefundVO.from(refund);
    }

    public PageResult<RefundVO> adminList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<RefundVO>builder()
                .total(refundMapper.countAdmin(status, keyword))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(refundMapper.selectAdminPage(
                        status,
                        keyword,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ).stream().map(RefundVO::from).toList())
                .build();
    }

    public RefundVO adminDetail(Long id) {
        return RefundVO.from(requireRefund(id));
    }

    @Transactional
    public RefundVO approve(Long adminId, Long id, AdminRefundReviewRequest request) {
        Refund refund = requireRefund(id);
        if (refund.getStatus() != REFUND_APPLYING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前退款状态不能审核通过");
        }
        Order order = orderMapper.selectById(refund.getOrderId());
        OrderItem orderItem = orderItemMapper.selectById(refund.getOrderItemId());
        refundMapper.updateById(Refund.builder()
                .id(id)
                .status(REFUND_COMPLETED)
                .handledBy(adminId)
                .handledAt(LocalDateTime.now())
                .refundedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        orderItemMapper.updateById(OrderItem.builder()
                .id(orderItem.getId())
                .refundStatus(2)
                .build());
        inventoryService.increaseStock(orderItem.getSkuId(), orderItem.getQuantity(), "退款完成退回库存");
        if (order != null && order.getStatus() != OrderStatus.REFUNDED) {
            orderService.writeStatusLog(order.getId(), order.getStatus(), order.getStatus(), 2, adminId, noteOrDefault(request, "后台审核退款通过"));
        }
        operationLogService.write(adminId, "refund.approve", "refund", id, "{\"refundNo\":\"" + refund.getRefundNo() + "\"}");
        return RefundVO.from(requireRefund(id));
    }

    @Transactional
    public RefundVO reject(Long adminId, Long id, AdminRefundReviewRequest request) {
        Refund refund = requireRefund(id);
        if (refund.getStatus() != REFUND_APPLYING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前退款状态不能驳回");
        }
        refundMapper.updateById(Refund.builder()
                .id(id)
                .status(REFUND_REJECTED)
                .handledBy(adminId)
                .handledAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        orderItemMapper.updateById(OrderItem.builder()
                .id(refund.getOrderItemId())
                .refundStatus(3)
                .build());
        operationLogService.write(adminId, "refund.reject", "refund", id, "{\"note\":\"" + noteOrDefault(request, "后台驳回退款") + "\"}");
        return RefundVO.from(requireRefund(id));
    }

    private OrderItem requireOrderItem(Long userId, Long orderItemId) {
        OrderItem orderItem = orderItemMapper.selectByIdAndUserId(orderItemId, userId);
        if (orderItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单明细不存在");
        }
        return orderItem;
    }

    private Refund requireRefund(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "退款记录不存在");
        }
        return refund;
    }

    private boolean canRefund(Integer orderStatus) {
        return Integer.valueOf(OrderStatus.PAID).equals(orderStatus)
                || Integer.valueOf(OrderStatus.SHIPPED).equals(orderStatus)
                || Integer.valueOf(OrderStatus.COMPLETED).equals(orderStatus);
    }

    private String generateRefundNo() {
        return "R" + LocalDateTime.now().format(REFUND_NO_TIME_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String noteOrDefault(AdminRefundReviewRequest request, String defaultNote) {
        return request == null || !StringUtils.hasText(request.getNote()) ? defaultNote : request.getNote().trim();
    }
}
