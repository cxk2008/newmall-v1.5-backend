package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.FavoriteService;
import com.itye.mall.vo.favorite.FavoriteVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<PageResult<FavoriteVO>> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @RequestParam(required = false) Integer pageNum,
                                                    @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(favoriteService.list(user.getId(), pageNum, pageSize));
    }

    @PostMapping("/{productId}")
    public ApiResponse<FavoriteVO> add(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
        return ApiResponse.ok(favoriteService.add(user.getId(), productId));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
        favoriteService.delete(user.getId(), productId);
        return ApiResponse.ok(null);
    }
}
