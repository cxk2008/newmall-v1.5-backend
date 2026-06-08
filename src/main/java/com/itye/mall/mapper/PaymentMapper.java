package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper extends BaseMapper<Payment> {
    Payment selectLatestByOrderId(@Param("orderId") Long orderId);
}
