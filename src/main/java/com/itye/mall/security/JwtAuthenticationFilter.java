package com.itye.mall.security;

import com.itye.mall.entity.AdminUser;
import com.itye.mall.entity.User;
import com.itye.mall.mapper.AdminUserMapper;
import com.itye.mall.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserMapper userMapper, AdminUserMapper adminUserMapper) {
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.adminUserMapper = adminUserMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.isValid(token)) {
            authenticate(token, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        Long accountId = jwtService.parseUserId(token);
        AuthenticatedUser principal = null;
        if ("admin".equals(jwtService.parseAccountType(token))) {
            AdminUser adminUser = adminUserMapper.selectById(accountId);
            if (adminUser != null && adminUser.getDeletedAt() == null && Integer.valueOf(1).equals(adminUser.getStatus())) {
                principal = new AuthenticatedUser(adminUser);
            }
        } else {
            User user = userMapper.selectById(accountId);
            if (user != null && user.getDeletedAt() == null && Integer.valueOf(1).equals(user.getStatus())) {
                principal = new AuthenticatedUser(user);
            }
        }
        if (principal == null) {
            return;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
