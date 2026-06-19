package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {
    Order selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Order selectByIdAndOrderNo(@Param("id") Long id, @Param("orderNo") String orderNo);

    List<Order> selectByUserIdPage(@Param("userId") Long userId,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Order> selectAdminPage(@Param("status") Integer status,
                                @Param("keyword") String keyword,
                                @Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    int closePendingPayById(@Param("id") Long id,
                            @Param("closedAt") java.time.LocalDateTime closedAt,
                            @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
