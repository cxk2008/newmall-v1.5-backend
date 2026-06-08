package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminCategoryRequest;
import com.itye.mall.entity.ProductCategory;
import com.itye.mall.service.AdminCategoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public ApiResponse<PageResult<ProductCategory>> list(@RequestParam(required = false) Long parentId,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) Integer pageNum,
                                                         @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(adminCategoryService.list(parentId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategory> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminCategoryService.detail(id));
    }

    @PostMapping
    public ApiResponse<ProductCategory> create(@RequestBody AdminCategoryRequest request) {
        return ApiResponse.ok(adminCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategory> update(@PathVariable Long id, @RequestBody AdminCategoryRequest request) {
        return ApiResponse.ok(adminCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCategoryService.delete(id);
        return ApiResponse.ok(null);
    }
}
