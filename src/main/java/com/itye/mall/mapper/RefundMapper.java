package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Refund;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RefundMapper extends BaseMapper<Refund> {
    List<Refund> selectByUserIdPage(@Param("userId") Long userId,
                                    @Param("status") Integer status,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Refund> selectAdminPage(@Param("status") Integer status,
                                 @Param("keyword") String keyword,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    Refund selectByOrderItemId(@Param("orderItemId") Long orderItemId);
}
