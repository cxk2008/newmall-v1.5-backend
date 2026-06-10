package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.SeckillItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SeckillItemMapper extends BaseMapper<SeckillItem> {
    List<SeckillItem> selectByActivityId(@Param("activityId") Long activityId);

    List<SeckillItem> selectEnabledByActivityId(@Param("activityId") Long activityId);

    SeckillItem selectByActivityIdAndSkuId(@Param("activityId") Long activityId, @Param("skuId") Long skuId);

    int updateAdminById(SeckillItem item);

    int decreaseAvailableStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    int increaseAvailableStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
