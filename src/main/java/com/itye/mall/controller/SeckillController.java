package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.SeckillActivityService;
import com.itye.mall.service.SeckillItemService;
import com.itye.mall.service.SeckillResultService;
import com.itye.mall.service.SeckillService;
import com.itye.mall.vo.seckill.SeckillActivityVO;
import com.itye.mall.vo.seckill.SeckillItemVO;
import com.itye.mall.vo.seckill.SeckillResultVO;
import com.itye.mall.vo.seckill.SeckillSubmitVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    private final SeckillActivityService seckillActivityService;
    private final SeckillItemService seckillItemService;
    private final SeckillService seckillService;
    private final SeckillResultService seckillResultService;

    public SeckillController(SeckillActivityService seckillActivityService,
                             SeckillItemService seckillItemService,
                             SeckillService seckillService,
                             SeckillResultService seckillResultService) {
        this.seckillActivityService = seckillActivityService;
        this.seckillItemService = seckillItemService;
        this.seckillService = seckillService;
        this.seckillResultService = seckillResultService;
    }

    @GetMapping("/activities/current")
    public ApiResponse<SeckillActivityVO> current() {
        return ApiResponse.ok(seckillActivityService.current());
    }

    @GetMapping("/activities/{activityId}/items")
    public ApiResponse<List<SeckillItemVO>> items(@PathVariable Long activityId) {
        seckillActivityService.requireActivity(activityId);
        return ApiResponse.ok(seckillItemService.listEnabledByActivity(activityId));
    }

    @GetMapping("/items/{seckillItemId}")
    public ApiResponse<SeckillItemVO> item(@PathVariable Long seckillItemId) {
        return ApiResponse.ok(seckillItemService.detail(seckillItemId));
    }

    @PostMapping("/items/{seckillItemId}/orders")
    public ApiResponse<SeckillSubmitVO> submit(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable Long seckillItemId) {
        return ApiResponse.ok(seckillService.submit(user.getId(), seckillItemId));
    }

    @GetMapping("/results/{requestNo}")
    public ApiResponse<SeckillResultVO> result(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable String requestNo) {
        return ApiResponse.ok(seckillResultService.getResult(user.getId(), requestNo));
    }
}
