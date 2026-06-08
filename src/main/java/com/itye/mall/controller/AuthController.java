package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.auth.LoginRequest;
import com.itye.mall.dto.auth.RegisterRequest;
import com.itye.mall.entity.User;
import com.itye.mall.mapper.UserMapper;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.AuthService;
import com.itye.mall.vo.auth.AuthVO;
import com.itye.mall.vo.auth.UserProfileVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ApiResponse<AuthVO> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthVO> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = userMapper.selectById(principal.getId());
        if (user == null || user.getDeletedAt() != null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return ApiResponse.ok(UserProfileVO.from(user));
    }
}
