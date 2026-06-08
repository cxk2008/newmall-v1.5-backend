package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.order.CreateOrderRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.OrderService;
import com.itye.mall.vo.order.OrderVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderVO> create(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<PageResult<OrderVO>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) Integer pageNum,
                                                       @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(orderService.list(user.getId(), status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVO> detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(user.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderVO> cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(orderService.cancel(user.getId(), id));
    }
}
