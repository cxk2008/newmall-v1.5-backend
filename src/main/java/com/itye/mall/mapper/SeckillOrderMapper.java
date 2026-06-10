package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.SeckillOrder;
import org.apache.ibatis.annotations.Param;

public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    SeckillOrder selectByRequestNo(@Param("requestNo") String requestNo);

    SeckillOrder selectByRequestNoAndUserId(@Param("requestNo") String requestNo, @Param("userId") Long userId);

    SeckillOrder selectByUserItem(@Param("activityId") Long activityId,
                                  @Param("seckillItemId") Long seckillItemId,
                                  @Param("userId") Long userId);

    int markOrderCreated(@Param("requestNo") String requestNo,
                         @Param("orderId") Long orderId,
                         @Param("orderNo") String orderNo);

    int markFailed(@Param("requestNo") String requestNo, @Param("failureReason") String failureReason);
}
