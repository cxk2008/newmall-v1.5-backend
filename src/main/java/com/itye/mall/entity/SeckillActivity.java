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
public class SeckillActivity {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer status;
    private LocalDateTime warmUpAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
