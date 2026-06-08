package com.itye.mall.service;

import com.itye.mall.common.constant.OrderStatus;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.review.CreateReviewRequest;
import com.itye.mall.entity.Order;
import com.itye.mall.entity.OrderItem;
import com.itye.mall.entity.ProductReview;
import com.itye.mall.mapper.OrderItemMapper;
import com.itye.mall.mapper.ProductReviewMapper;
import com.itye.mall.vo.review.ProductReviewVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductReviewService {
    private final ProductReviewMapper productReviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;

    public ProductReviewService(ProductReviewMapper productReviewMapper,
                                OrderItemMapper orderItemMapper,
                                OrderService orderService) {
        this.productReviewMapper = productReviewMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
    }

    @Transactional
    public ProductReviewVO create(Long userId, CreateReviewRequest request) {
        validate(request);
        OrderItem item = orderItemMapper.selectById(request.getOrderItemId());
        if (item == null || !userId.equals(item.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单商品不存在");
        }
        Order order = orderService.requireUserOrder(userId, item.getOrderId());
        if (!Integer.valueOf(OrderStatus.COMPLETED).equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单完成后才能评价");
        }
        if (productReviewMapper.selectByOrderItemId(item.getId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该商品已评价");
        }

        LocalDateTime now = LocalDateTime.now();
        ProductReview review = ProductReview.builder()
                .userId(userId)
                .orderId(order.getId())
                .orderItemId(item.getId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .rating(request.getRating())
                .content(request.getContent())
                .imagesJson(request.getImagesJson())
                .isAnonymous(Integer.valueOf(1).equals(request.getIsAnonymous()) ? 1 : 0)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        productReviewMapper.insert(review);
        return ProductReviewVO.from(review);
    }

    public PageResult<ProductReviewVO> listByProduct(Long productId, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<ProductReviewVO> records = productReviewMapper.selectByProductIdPage(
                productId,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        ).stream().map(ProductReviewVO::from).toList();
        long total = productReviewMapper.countByProductId(productId);
        return PageResult.<ProductReviewVO>builder()
                .total(total)
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public List<ProductReviewVO> listByOrder(Long userId, Long orderId) {
        orderService.requireUserOrder(userId, orderId);
        return productReviewMapper.selectByOrderId(orderId).stream().map(ProductReviewVO::from).toList();
    }

    private void validate(CreateReviewRequest request) {
        if (request == null || request.getOrderItemId() == null || request.getRating() == null) {
            throw new IllegalArgumentException("评价参数不完整");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在 1 到 5 之间");
        }
    }
}
