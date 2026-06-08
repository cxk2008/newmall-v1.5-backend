package com.itye.mall.vo.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthVO {
    private String tokenType;
    private String token;
    private Long expiresIn;
    private UserProfileVO user;
}
