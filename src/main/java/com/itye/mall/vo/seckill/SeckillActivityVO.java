package com.itye.mall.vo.seckill;

import com.itye.mall.entity.SeckillActivity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SeckillActivityVO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer status;
    private LocalDateTime warmUpAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<SeckillItemVO> items;

    public static SeckillActivityVO from(SeckillActivity activity, List<SeckillItemVO> items) {
        if (activity == null) {
            return null;
        }
        return SeckillActivityVO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .startsAt(activity.getStartsAt())
                .endsAt(activity.getEndsAt())
                .status(activity.getStatus())
                .warmUpAt(activity.getWarmUpAt())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .items(items)
                .build();
    }
}
