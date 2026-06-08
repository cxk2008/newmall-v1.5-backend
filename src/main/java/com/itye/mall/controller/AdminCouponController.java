package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminCouponRequest;
import com.itye.mall.entity.Coupon;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.CouponService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ApiResponse<PageResult<Coupon>> list(@RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer pageNum,
                                                @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(couponService.adminList(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Coupon> detail(@PathVariable Long id) {
        return ApiResponse.ok(couponService.detail(id));
    }

    @PostMapping
    public ApiResponse<Coupon> create(@AuthenticationPrincipal AuthenticatedUser user,
                                      @RequestBody AdminCouponRequest request) {
        return ApiResponse.ok(couponService.adminCreate(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Coupon> update(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long id,
                                      @RequestBody AdminCouponRequest request) {
        return ApiResponse.ok(couponService.adminUpdate(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        couponService.adminDelete(user.getId(), id);
        return ApiResponse.ok(null);
    }
}
