package com.itye.mall.service;

import com.itye.mall.dto.auth.LoginRequest;
import com.itye.mall.entity.AdminUser;
import com.itye.mall.mapper.AdminUserMapper;
import com.itye.mall.security.JwtService;
import com.itye.mall.vo.auth.AdminAuthVO;
import com.itye.mall.vo.auth.AdminProfileVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AdminAuthVO login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getAccount()) || !StringUtils.hasText(request.getPassword())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        AdminUser adminUser = adminUserMapper.selectByUsername(request.getAccount().trim());
        if (adminUser == null || !passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(adminUser.getStatus()) || adminUser.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员账号已被禁用");
        }
        AdminUser update = AdminUser.builder()
                .id(adminUser.getId())
                .lastLoginAt(LocalDateTime.now())
                .build();
        adminUserMapper.updateById(update);
        adminUser.setLastLoginAt(update.getLastLoginAt());
        return AdminAuthVO.builder()
                .tokenType("Bearer")
                .token(jwtService.generateAdminToken(adminUser))
                .expiresIn(jwtService.getExpirationSeconds())
                .admin(AdminProfileVO.from(adminUser))
                .build();
    }
}
