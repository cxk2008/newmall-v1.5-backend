package com.itye.mall.vo.auth;

import com.itye.mall.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer status;

    public static UserProfileVO from(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
