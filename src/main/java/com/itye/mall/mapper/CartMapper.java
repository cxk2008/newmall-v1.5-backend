package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Cart;
import org.apache.ibatis.annotations.Param;

public interface CartMapper extends BaseMapper<Cart> {
    Cart selectByUserId(@Param("userId") Long userId);
}
