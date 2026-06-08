package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.entity.OperationLog;
import com.itye.mall.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {
    private final OperationLogService operationLogService;

    public AdminOperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<PageResult<OperationLog>> list(@RequestParam(required = false) Long adminId,
                                                      @RequestParam(required = false) String action,
                                                      @RequestParam(required = false) Integer pageNum,
                                                      @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(operationLogService.list(adminId, action, pageNum, pageSize));
    }
}
