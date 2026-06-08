package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper extends BaseMapper<Favorite> {
    Favorite selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    List<Favorite> selectByUserIdPage(@Param("userId") Long userId,
                                      @Param("offset") Integer offset,
                                      @Param("pageSize") Integer pageSize);

    long countByUserId(@Param("userId") Long userId);

    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
