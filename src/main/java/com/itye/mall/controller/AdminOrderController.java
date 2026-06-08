package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.CloseOrderRequest;
import com.itye.mall.dto.shipment.ShipOrderRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.AdminOrderService;
import com.itye.mall.service.ShipmentService;
import com.itye.mall.vo.order.OrderVO;
import com.itye.mall.vo.shipment.ShipmentVO;
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
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;
    private final ShipmentService shipmentService;

    public AdminOrderController(AdminOrderService adminOrderService, ShipmentService shipmentService) {
        this.adminOrderService = adminOrderService;
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ApiResponse<PageResult<OrderVO>> list(@RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer pageNum,
                                                 @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(adminOrderService.list(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminOrderService.detail(id));
    }

    @PutMapping("/{id}/close")
    public ApiResponse<OrderVO> close(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long id,
                                      @RequestBody(required = false) CloseOrderRequest request) {
        return ApiResponse.ok(adminOrderService.close(user.getId(), id, request));
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<ShipmentVO> ship(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable Long id,
                                        @RequestBody ShipOrderRequest request) {
        return ApiResponse.ok(shipmentService.ship(user.getId(), id, request));
    }
}
