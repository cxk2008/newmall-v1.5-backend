package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.review.CreateReviewRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.ProductReviewService;
import com.itye.mall.vo.review.ProductReviewVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @PostMapping("/api/reviews")
    public ApiResponse<ProductReviewVO> create(@AuthenticationPrincipal AuthenticatedUser user,
                                               @RequestBody CreateReviewRequest request) {
        return ApiResponse.ok(productReviewService.create(user.getId(), request));
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ApiResponse<PageResult<ProductReviewVO>> listByProduct(@PathVariable Long productId,
                                                                  @RequestParam(required = false) Integer pageNum,
                                                                  @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(productReviewService.listByProduct(productId, pageNum, pageSize));
    }

    @GetMapping("/api/orders/{orderId}/reviews")
    public ApiResponse<List<ProductReviewVO>> listByOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @PathVariable Long orderId) {
        return ApiResponse.ok(productReviewService.listByOrder(user.getId(), orderId));
    }
}
