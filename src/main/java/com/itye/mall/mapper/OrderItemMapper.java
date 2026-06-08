package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper extends BaseMapper<OrderItem> {
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    OrderItem selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
