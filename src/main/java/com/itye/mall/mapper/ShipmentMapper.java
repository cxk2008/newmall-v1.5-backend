package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Shipment;
import org.apache.ibatis.annotations.Param;

public interface ShipmentMapper extends BaseMapper<Shipment> {
    Shipment selectByOrderId(@Param("orderId") Long orderId);
}
