package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminBrandRequest;
import com.itye.mall.entity.Brand;
import com.itye.mall.service.AdminBrandService;
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
@RequestMapping("/api/admin/brands")
public class AdminBrandController {
    private final AdminBrandService adminBrandService;

    public AdminBrandController(AdminBrandService adminBrandService) {
        this.adminBrandService = adminBrandService;
    }

    @GetMapping
    public ApiResponse<PageResult<Brand>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Integer pageNum,
                                               @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(adminBrandService.list(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Brand> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminBrandService.detail(id));
    }

    @PostMapping
    public ApiResponse<Brand> create(@RequestBody AdminBrandRequest request) {
        return ApiResponse.ok(adminBrandService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Brand> update(@PathVariable Long id, @RequestBody AdminBrandRequest request) {
        return ApiResponse.ok(adminBrandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminBrandService.delete(id);
        return ApiResponse.ok(null);
    }
}
