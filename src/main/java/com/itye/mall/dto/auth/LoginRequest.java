package com.itye.mall.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String account;
    private String password;
}
