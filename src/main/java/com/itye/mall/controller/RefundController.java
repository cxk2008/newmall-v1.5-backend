package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.refund.CreateRefundRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.RefundService;
import com.itye.mall.vo.refund.RefundVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ApiResponse<RefundVO> create(@AuthenticationPrincipal AuthenticatedUser user,
                                        @RequestBody CreateRefundRequest request) {
        return ApiResponse.ok(refundService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<PageResult<RefundVO>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) Integer pageNum,
                                                  @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(refundService.listByUser(user.getId(), status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<RefundVO> detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(refundService.detailByUser(user.getId(), id));
    }
}
