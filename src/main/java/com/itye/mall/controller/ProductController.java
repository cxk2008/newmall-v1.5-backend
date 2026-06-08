package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.service.ProductService;
import com.itye.mall.vo.product.ProductDetailVO;
import com.itye.mall.vo.product.ProductListItemVO;
import com.itye.mall.vo.product.ProductSkuVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResult<ProductListItemVO>> list(@RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Integer pageNum,
                                                         @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(productService.list(categoryId, keyword, pageNum, pageSize));
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<ProductListItemVO>> search(@RequestParam String keyword,
                                                           @RequestParam(required = false) Integer pageNum,
                                                           @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(productService.list(null, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detail(id));
    }

    @GetMapping("/{id}/skus")
    public ApiResponse<List<ProductSkuVO>> skus(@PathVariable Long id) {
        return ApiResponse.ok(productService.skus(id));
    }
}
