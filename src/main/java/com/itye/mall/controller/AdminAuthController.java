package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.auth.LoginRequest;
import com.itye.mall.entity.AdminUser;
import com.itye.mall.mapper.AdminUserMapper;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.AdminAuthService;
import com.itye.mall.vo.auth.AdminAuthVO;
import com.itye.mall.vo.auth.AdminProfileVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService adminAuthService;
    private final AdminUserMapper adminUserMapper;

    public AdminAuthController(AdminAuthService adminAuthService, AdminUserMapper adminUserMapper) {
        this.adminAuthService = adminAuthService;
        this.adminUserMapper = adminUserMapper;
    }

    @PostMapping("/login")
    public ApiResponse<AdminAuthVO> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AdminProfileVO> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        AdminUser adminUser = adminUserMapper.selectById(principal.getId());
        if (adminUser == null || adminUser.getDeletedAt() != null || !Integer.valueOf(1).equals(adminUser.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后台");
        }
        return ApiResponse.ok(AdminProfileVO.from(adminUser));
    }
}
