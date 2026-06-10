package com.itye.mall.vo.seckill;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillResultVO {
    private String requestNo;
    private String status;
    private Long orderId;
    private String orderNo;
    private String failureReason;
}
