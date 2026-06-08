package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.entity.Coupon;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.CouponService;
import com.itye.mall.vo.coupon.UserCouponVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/api/coupons/available")
    public ApiResponse<List<Coupon>> available() {
        return ApiResponse.ok(couponService.available());
    }

    @PostMapping("/api/coupons/{id}/claim")
    public ApiResponse<UserCouponVO> claim(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(couponService.claim(user.getId(), id));
    }

    @GetMapping("/api/user/coupons")
    public ApiResponse<List<UserCouponVO>> userCoupons(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(couponService.userCoupons(user.getId(), status));
    }
}
