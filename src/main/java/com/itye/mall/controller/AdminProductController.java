package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminProductRequest;
import com.itye.mall.dto.admin.AdminSkuRequest;
import com.itye.mall.service.AdminProductService;
import com.itye.mall.vo.admin.AdminProductListItemVO;
import com.itye.mall.vo.product.ProductDetailVO;
import com.itye.mall.vo.product.ProductSkuVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ApiResponse<PageResult<AdminProductListItemVO>> list(@RequestParam(required = false) Long categoryId,
                                                                @RequestParam(required = false) Long brandId,
                                                                @RequestParam(required = false) Integer status,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Integer pageNum,
                                                                @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(adminProductService.list(categoryId, brandId, status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminProductService.detail(id));
    }

    @PostMapping
    public ApiResponse<ProductDetailVO> create(@RequestBody AdminProductRequest request) {
        return ApiResponse.ok(adminProductService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDetailVO> update(@PathVariable Long id, @RequestBody AdminProductRequest request) {
        return ApiResponse.ok(adminProductService.update(id, request));
    }

    @PutMapping("/{id}/on-sale")
    public ApiResponse<ProductDetailVO> onSale(@PathVariable Long id) {
        return ApiResponse.ok(adminProductService.onSale(id));
    }

    @PutMapping("/{id}/off-sale")
    public ApiResponse<ProductDetailVO> offSale(@PathVariable Long id) {
        return ApiResponse.ok(adminProductService.offSale(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminProductService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{productId}/skus")
    public ApiResponse<List<ProductSkuVO>> skus(@PathVariable Long productId) {
        return ApiResponse.ok(adminProductService.listSkus(productId));
    }

    @PostMapping("/{productId}/skus")
    public ApiResponse<ProductSkuVO> createSku(@PathVariable Long productId, @RequestBody AdminSkuRequest request) {
        return ApiResponse.ok(adminProductService.createSku(productId, request));
    }
}
