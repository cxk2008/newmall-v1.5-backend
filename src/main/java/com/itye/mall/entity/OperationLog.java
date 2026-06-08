package com.itye.mall.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    private Long id;
    private Long adminId;
    private String action;
    private String targetType;
    private String targetId;
    private String ipAddress;
    private String userAgent;
    private String detailJson;
    private LocalDateTime createdAt;
}
