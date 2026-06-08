package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.StockAdjustRequest;
import com.itye.mall.entity.InventoryLog;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/skus")
public class AdminInventoryController {
    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{skuId}/inventory-logs")
    public ApiResponse<PageResult<InventoryLog>> logs(@PathVariable Long skuId,
                                                      @RequestParam(required = false) Integer pageNum,
                                                      @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(inventoryService.listLogs(skuId, pageNum, pageSize));
    }

    @PostMapping("/{skuId}/stock/increase")
    public ApiResponse<ProductSku> increase(@PathVariable Long skuId, @RequestBody StockAdjustRequest request) {
        return ApiResponse.ok(inventoryService.increaseStock(skuId, request == null ? null : request.getQuantity(), request == null ? null : request.getNote()));
    }

    @PostMapping("/{skuId}/stock/decrease")
    public ApiResponse<ProductSku> decrease(@PathVariable Long skuId, @RequestBody StockAdjustRequest request) {
        return ApiResponse.ok(inventoryService.decreaseStock(skuId, request == null ? null : request.getQuantity(), request == null ? null : request.getNote()));
    }
}
