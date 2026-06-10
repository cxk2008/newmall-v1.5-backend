package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.admin.AdminSeckillItemRequest;
import com.itye.mall.entity.SeckillActivity;
import com.itye.mall.entity.SeckillItem;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.SeckillActivityService;
import com.itye.mall.service.SeckillItemService;
import com.itye.mall.vo.seckill.SeckillItemVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seckill")
public class AdminSeckillItemController {
    private final SeckillActivityService seckillActivityService;
    private final SeckillItemService seckillItemService;

    public AdminSeckillItemController(SeckillActivityService seckillActivityService, SeckillItemService seckillItemService) {
        this.seckillActivityService = seckillActivityService;
        this.seckillItemService = seckillItemService;
    }

    @GetMapping("/activities/{activityId}/items")
    public ApiResponse<List<SeckillItemVO>> list(@PathVariable Long activityId) {
        seckillActivityService.requireActivity(activityId);
        return ApiResponse.ok(seckillItemService.listByActivity(activityId));
    }

    @PostMapping("/activities/{activityId}/items")
    public ApiResponse<SeckillItem> create(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long activityId,
                                           @RequestBody AdminSeckillItemRequest request) {
        SeckillActivity activity = seckillActivityService.requireActivity(activityId);
        return ApiResponse.ok(seckillItemService.create(user.getId(), activity, request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<SeckillItem> update(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long id,
                                           @RequestBody AdminSeckillItemRequest request) {
        SeckillItem item = seckillItemService.requireItem(id);
        SeckillActivity activity = seckillActivityService.requireActivity(item.getActivityId());
        return ApiResponse.ok(seckillItemService.update(user.getId(), activity, id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        SeckillItem item = seckillItemService.requireItem(id);
        SeckillActivity activity = seckillActivityService.requireActivity(item.getActivityId());
        seckillItemService.delete(user.getId(), activity, id);
        return ApiResponse.ok(null);
    }
}
