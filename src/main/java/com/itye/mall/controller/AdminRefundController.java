package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.refund.AdminRefundReviewRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.RefundService;
import com.itye.mall.vo.refund.RefundVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {
    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    public ApiResponse<PageResult<RefundVO>> list(@RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer pageNum,
                                                  @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(refundService.adminList(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<RefundVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(refundService.adminDetail(id));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<RefundVO> approve(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable Long id,
                                         @RequestBody(required = false) AdminRefundReviewRequest request) {
        return ApiResponse.ok(refundService.approve(user.getId(), id, request));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<RefundVO> reject(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable Long id,
                                        @RequestBody(required = false) AdminRefundReviewRequest request) {
        return ApiResponse.ok(refundService.reject(user.getId(), id, request));
    }
}
