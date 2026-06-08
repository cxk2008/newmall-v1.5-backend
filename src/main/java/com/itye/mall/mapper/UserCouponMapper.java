package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.UserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    List<UserCoupon> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<UserCoupon> selectByOrderId(@Param("orderId") Long orderId);

    long countByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    int markUsed(@Param("id") Long id,
                 @Param("userId") Long userId,
                 @Param("orderId") Long orderId);

    int releaseByOrderId(@Param("orderId") Long orderId);
}
