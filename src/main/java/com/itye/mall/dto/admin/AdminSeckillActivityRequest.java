package com.itye.mall.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminSeckillActivityRequest {
    private String name;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer status;
}
