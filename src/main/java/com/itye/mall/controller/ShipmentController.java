package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.ShipmentService;
import com.itye.mall.vo.shipment.ShipmentVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ShipmentController {
    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{orderId}/shipment")
    public ApiResponse<ShipmentVO> getShipment(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable Long orderId) {
        return ApiResponse.ok(shipmentService.getByUserOrder(user.getId(), orderId));
    }

    @PutMapping("/{orderId}/receive")
    public ApiResponse<ShipmentVO> receive(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long orderId) {
        return ApiResponse.ok(shipmentService.receive(user.getId(), orderId));
    }
}
