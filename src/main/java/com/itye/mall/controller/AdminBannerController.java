package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminBannerRequest;
import com.itye.mall.entity.Banner;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.BannerService;
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
@RequestMapping("/api/admin/banners")
public class AdminBannerController {
    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ApiResponse<PageResult<Banner>> list(@RequestParam(required = false) String position,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer pageNum,
                                                @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(bannerService.adminList(position, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Banner> detail(@PathVariable Long id) {
        return ApiResponse.ok(bannerService.detail(id));
    }

    @PostMapping
    public ApiResponse<Banner> create(@AuthenticationPrincipal AuthenticatedUser user,
                                      @RequestBody AdminBannerRequest request) {
        return ApiResponse.ok(bannerService.adminCreate(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Banner> update(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long id,
                                      @RequestBody AdminBannerRequest request) {
        return ApiResponse.ok(bannerService.adminUpdate(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        bannerService.adminDelete(user.getId(), id);
        return ApiResponse.ok(null);
    }
}
