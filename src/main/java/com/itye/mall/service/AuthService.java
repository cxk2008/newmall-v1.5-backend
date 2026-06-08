package com.itye.mall.service;

import com.itye.mall.dto.auth.LoginRequest;
import com.itye.mall.dto.auth.RegisterRequest;
import com.itye.mall.entity.User;
import com.itye.mall.mapper.UserMapper;
import com.itye.mall.security.JwtService;
import com.itye.mall.vo.auth.AuthVO;
import com.itye.mall.vo.auth.UserProfileVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthVO register(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        if (StringUtils.hasText(request.getPhone()) && userMapper.selectByPhone(request.getPhone()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "手机号已存在");
        }
        if (StringUtils.hasText(request.getEmail()) && userMapper.selectByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "邮箱已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(request.getUsername().trim())
                .phone(trimToNull(request.getPhone()))
                .email(trimToNull(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : request.getUsername().trim())
                .gender(0)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insert(user);
        return buildAuthResponse(user);
    }

    public AuthVO login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getAccount()) || !StringUtils.hasText(request.getPassword())) {
            throw new BadCredentialsException("账号或密码错误");
        }

        User user = userMapper.selectByAccount(request.getAccount().trim());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus()) || user.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已被禁用");
        }

        User update = User.builder()
                .id(user.getId())
                .lastLoginAt(LocalDateTime.now())
                .build();
        userMapper.updateById(update);
        user.setLastLoginAt(update.getLastLoginAt());
        return buildAuthResponse(user);
    }

    private AuthVO buildAuthResponse(User user) {
        return AuthVO.builder()
                .tokenType("Bearer")
                .token(jwtService.generateToken(user))
                .expiresIn(jwtService.getExpirationSeconds())
                .user(UserProfileVO.from(user))
                .build();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("注册参数不能为空");
        }
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (request.getUsername().trim().length() < 3 || request.getUsername().trim().length() > 64) {
            throw new IllegalArgumentException("用户名长度需要在 3 到 64 个字符之间");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
