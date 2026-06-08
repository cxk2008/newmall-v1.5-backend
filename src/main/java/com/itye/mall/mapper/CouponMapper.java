package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Coupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponMapper extends BaseMapper<Coupon> {
    List<Coupon> selectAvailable();

    List<Coupon> selectAdminPage(@Param("status") Integer status,
                                 @Param("keyword") String keyword,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    int increaseClaimedQuantity(@Param("id") Long id);

    int increaseUsedQuantity(@Param("id") Long id);

    int decreaseUsedQuantity(@Param("id") Long id);
}
