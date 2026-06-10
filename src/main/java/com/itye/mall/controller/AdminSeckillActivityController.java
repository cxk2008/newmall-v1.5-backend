package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.dto.admin.AdminSeckillActivityRequest;
import com.itye.mall.entity.SeckillActivity;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.SeckillActivityService;
import com.itye.mall.vo.seckill.SeckillActivityVO;
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
@RequestMapping("/api/admin/seckill/activities")
public class AdminSeckillActivityController {
    private final SeckillActivityService seckillActivityService;

    public AdminSeckillActivityController(SeckillActivityService seckillActivityService) {
        this.seckillActivityService = seckillActivityService;
    }

    @GetMapping
    public ApiResponse<PageResult<SeckillActivity>> list(@RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Integer pageNum,
                                                         @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(seckillActivityService.adminList(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<SeckillActivityVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(seckillActivityService.detail(id));
    }

    @PostMapping
    public ApiResponse<SeckillActivity> create(@AuthenticationPrincipal AuthenticatedUser user,
                                               @RequestBody AdminSeckillActivityRequest request) {
        return ApiResponse.ok(seckillActivityService.create(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SeckillActivity> update(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable Long id,
                                               @RequestBody AdminSeckillActivityRequest request) {
        return ApiResponse.ok(seckillActivityService.update(user.getId(), id, request));
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<SeckillActivity> publish(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(seckillActivityService.publish(user.getId(), id));
    }

    @PutMapping("/{id}/close")
    public ApiResponse<SeckillActivity> close(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(seckillActivityService.close(user.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        seckillActivityService.delete(user.getId(), id);
        return ApiResponse.ok(null);
    }
}
