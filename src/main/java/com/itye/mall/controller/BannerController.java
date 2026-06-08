package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.entity.Banner;
import com.itye.mall.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ApiResponse<List<Banner>> list(@RequestParam(required = false) String position) {
        return ApiResponse.ok(bannerService.listVisible(position));
    }
}
