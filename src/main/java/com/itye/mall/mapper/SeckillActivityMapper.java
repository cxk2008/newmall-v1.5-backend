package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.SeckillActivity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {
    List<SeckillActivity> selectAdminPage(@Param("status") Integer status,
                                          @Param("keyword") String keyword,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    SeckillActivity selectCurrent(@Param("now") LocalDateTime now);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("warmUpAt") LocalDateTime warmUpAt,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
