package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.service.ProductCategoryService;
import com.itye.mall.vo.product.CategoryTreeNodeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeNodeVO>> tree() {
        return ApiResponse.ok(productCategoryService.tree());
    }
}
