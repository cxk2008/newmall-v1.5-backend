package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.constant.OrderSourceType;
import com.itye.mall.common.id.BusinessNoGenerator;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.order.CreateOrderRequest;
import com.itye.mall.mq.SeckillOrderMessage;
import com.itye.mall.entity.CartItem;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.OrderItem;
import com.itye.mall.entity.OrderStatusLog;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.entity.UserAddress;
import com.itye.mall.mapper.CartItemMapper;
import com.itye.mall.mapper.OrderItemMapper;
import com.itye.mall.mapper.OrderMapper;
import com.itye.mall.mapper.OrderStatusLogMapper;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.itye.mall.vo.order.OrderItemVO;
import com.itye.mall.vo.order.OrderVO;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserAddressService userAddressService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final BusinessNoGenerator businessNoGenerator;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        OrderStatusLogMapper orderStatusLogMapper,
                        CartItemMapper cartItemMapper,
                        ProductMapper productMapper,
                        ProductSkuMapper productSkuMapper,
                        UserAddressService userAddressService,
                        ProductService productService,
                        InventoryService inventoryService,
                        CouponService couponService,
                        BusinessNoGenerator businessNoGenerator) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.userAddressService = userAddressService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.couponService = couponService;
        this.businessNoGenerator = businessNoGenerator;
    }

    @Transactional
    public OrderVO create(Long userId, CreateOrderRequest request) {
        if (request == null || request.getAddressId() == null) {
            throw new IllegalArgumentException("请选择收货地址");
        }
        UserAddress address = userAddressService.detail(userId, request.getAddressId());
        List<OrderLine> lines = buildOrderLines(userId, request);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("请选择要购买的商品");
        }

        BigDecimal productAmount = lines.stream()
                .map(OrderLine::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freightAmount = BigDecimal.ZERO;
        CouponService.CouponUsage couponUsage = couponService.calculateUsage(userId, request.getUserCouponId(), productAmount);
        BigDecimal discountAmount = couponUsage.discountAmount();
        BigDecimal payableAmount = productAmount.add(freightAmount).subtract(discountAmount);
        String orderNo = businessNoGenerator.generateOrderNo();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .status(OrderStatus.PENDING_PAY)
                .sourceType(OrderSourceType.NORMAL)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverProvince(address.getProvince())
                .receiverCity(address.getCity())
                .receiverDistrict(address.getDistrict())
                .receiverAddress(address.getDetailAddress())
                .productAmount(productAmount)
                .freightAmount(freightAmount)
                .discountAmount(discountAmount)
                .payableAmount(payableAmount)
                .paidAmount(BigDecimal.ZERO)
                .remark(request.getRemark())
                .createdAt(now)
                .updatedAt(now)
                .build();
        orderMapper.insert(order);
        couponService.markUsed(userId, couponUsage, order.getId());

        List<OrderItemVO> itemResponses = new ArrayList<>();
        for (OrderLine line : lines) {
            inventoryService.lockStock(line.sku().getId(), line.quantity(), "order", orderNo);
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .orderNo(orderNo)
                    .userId(userId)
                    .productId(line.product().getId())
                    .skuId(line.sku().getId())
                    .productName(line.product().getName())
                    .skuName(line.sku().getName())
                    .skuSpecJson(line.sku().getSpecJson())
                    .imageUrl(line.sku().getImageUrl() != null ? line.sku().getImageUrl() : line.product().getMainImageUrl())
                    .unitPrice(line.sku().getSalePrice())
                    .quantity(line.quantity())
                    .totalAmount(line.totalAmount())
                    .refundStatus(0)
                    .createdAt(now)
                    .build();
            orderItemMapper.insert(item);
            itemResponses.add(OrderItemVO.from(item));
        }
        if (request.getSkuId() == null) {
            cartItemMapper.deleteSelectedByUserId(userId);
        }
        writeStatusLog(order.getId(), null, OrderStatus.PENDING_PAY, 1, userId, "用户创建订单");
        return OrderVO.from(order, itemResponses);
    }

    @Transactional
    public OrderVO createSeckillOrder(Long seckillOrderId, SeckillOrderMessage message) {
        List<UserAddress> addresses = userAddressService.list(message.getUserId());
        if (addresses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先添加收货地址");
        }
        UserAddress address = addresses.getFirst();
        Product product = productService.requireOnSaleProduct(message.getProductId());
        ProductSku sku = productService.requireEnabledSku(message.getSkuId());
        BigDecimal productAmount = message.getSeckillPrice().multiply(BigDecimal.valueOf(message.getQuantity()));
        String orderNo = businessNoGenerator.generateOrderNo();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(message.getUserId())
                .status(OrderStatus.PENDING_PAY)
                .sourceType(OrderSourceType.SECKILL)
                .sourceId(seckillOrderId)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .receiverProvince(address.getProvince())
                .receiverCity(address.getCity())
                .receiverDistrict(address.getDistrict())
                .receiverAddress(address.getDetailAddress())
                .productAmount(productAmount)
                .freightAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .payableAmount(productAmount)
                .paidAmount(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
        orderMapper.insert(order);

        OrderItem item = OrderItem.builder()
                .orderId(order.getId())
                .orderNo(orderNo)
                .userId(message.getUserId())
                .productId(product.getId())
                .skuId(sku.getId())
                .productName(product.getName())
                .skuName(sku.getName())
                .skuSpecJson(sku.getSpecJson())
                .imageUrl(sku.getImageUrl() != null ? sku.getImageUrl() : product.getMainImageUrl())
                .unitPrice(message.getSeckillPrice())
                .quantity(message.getQuantity())
                .totalAmount(productAmount)
                .refundStatus(0)
                .createdAt(now)
                .build();
        orderItemMapper.insert(item);
        writeStatusLog(order.getId(), null, OrderStatus.PENDING_PAY, 3, null, "秒杀异步创建订单");
        return OrderVO.from(order, List.of(OrderItemVO.from(item)));
    }

    public PageResult<OrderVO> list(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<OrderVO> records = orderMapper.selectByUserIdPage(
                userId,
                status,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        ).stream().map(order -> OrderVO.from(order, loadItems(order.getId()))).toList();
        long total = orderMapper.countByUserId(userId, status);
        return PageResult.<OrderVO>builder()
                .total(total)
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public OrderVO detail(Long userId, Long id) {
        Order order = requireUserOrder(userId, id);
        return OrderVO.from(order, loadItems(order.getId()));
    }

    @Transactional
    public OrderVO cancel(Long userId, Long id) {
        Order order = requireUserOrder(userId, id);
        if (!Integer.valueOf(OrderStatus.PENDING_PAY).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有待支付订单可以取消");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : items) {
            inventoryService.unlockStock(item.getSkuId(), item.getQuantity(), "order_cancel", order.getOrderNo());
        }
        couponService.releaseByOrder(order.getId());
        orderMapper.updateById(Order.builder()
                .id(order.getId())
                .status(OrderStatus.CLOSED)
                .closedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        writeStatusLog(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.CLOSED, 1, userId, "用户取消订单");
        return detail(userId, id);
    }

    public Order requireUserOrder(Long userId, Long id) {
        Order order = orderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    public List<OrderItem> listOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    public void writeStatusLog(Long orderId, Integer oldStatus, Integer newStatus, Integer operatorType, Long operatorId, String note) {
        orderStatusLogMapper.insert(OrderStatusLog.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private List<OrderLine> buildOrderLines(Long userId, CreateOrderRequest request) {
        if (request.getSkuId() != null) {
            int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
            if (quantity < 1) {
                throw new IllegalArgumentException("购买数量不能小于 1");
            }
            ProductSku sku = productService.requireEnabledSku(request.getSkuId());
            Product product = productService.requireOnSaleProduct(sku.getProductId());
            ensureEnoughStock(sku, quantity);
            return List.of(new OrderLine(product, sku, quantity));
        }

        return cartItemMapper.selectSelectedByUserId(userId).stream().map(item -> {
            ProductSku sku = productService.requireEnabledSku(item.getSkuId());
            Product product = productService.requireOnSaleProduct(item.getProductId());
            ensureEnoughStock(sku, item.getQuantity());
            return new OrderLine(product, sku, item.getQuantity());
        }).toList();
    }

    private void ensureEnoughStock(ProductSku sku, Integer quantity) {
        ProductSku latestSku = productSkuMapper.selectById(sku.getId());
        int stock = latestSku.getStock() == null ? 0 : latestSku.getStock();
        int lockedStock = latestSku.getLockedStock() == null ? 0 : latestSku.getLockedStock();
        if (stock - lockedStock < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不足");
        }
    }

    private List<OrderItemVO> loadItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId).stream().map(OrderItemVO::from).toList();
    }

    private record OrderLine(Product product, ProductSku sku, Integer quantity) {
        BigDecimal totalAmount() {
            return sku.getSalePrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
