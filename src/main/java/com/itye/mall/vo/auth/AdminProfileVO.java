package com.itye.mall.vo.auth;

import com.itye.mall.entity.AdminUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private Integer status;

    public static AdminProfileVO from(AdminUser adminUser) {
        return AdminProfileVO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .phone(adminUser.getPhone())
                .email(adminUser.getEmail())
                .role(adminUser.getRole())
                .status(adminUser.getStatus())
                .build();
    }
}
