package com.itye.mall.vo.seckill;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillSubmitVO {
    private String requestNo;
    private String status;
}
