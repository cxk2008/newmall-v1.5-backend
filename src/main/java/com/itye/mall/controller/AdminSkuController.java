package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.admin.AdminSkuRequest;
import com.itye.mall.service.AdminProductService;
import com.itye.mall.vo.product.ProductSkuVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/skus")
public class AdminSkuController {
    private final AdminProductService adminProductService;

    public AdminSkuController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductSkuVO> update(@PathVariable Long id, @RequestBody AdminSkuRequest request) {
        return ApiResponse.ok(adminProductService.updateSku(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminProductService.deleteSku(id);
        return ApiResponse.ok(null);
    }
}
