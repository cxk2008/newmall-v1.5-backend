package com.itye.mall.security;

import com.itye.mall.entity.AdminUser;
import com.itye.mall.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthenticatedUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final Integer status;
    private final String accountType;
    private final String role;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.nickname = user.getNickname();
        this.status = user.getStatus();
        this.accountType = "user";
        this.role = "user";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public AuthenticatedUser(AdminUser adminUser) {
        this.id = adminUser.getId();
        this.username = adminUser.getUsername();
        this.password = adminUser.getPasswordHash();
        this.nickname = adminUser.getRealName();
        this.status = adminUser.getStatus();
        this.accountType = "admin";
        this.role = adminUser.getRole();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public boolean isAdmin() {
        return "admin".equals(accountType);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }
}
