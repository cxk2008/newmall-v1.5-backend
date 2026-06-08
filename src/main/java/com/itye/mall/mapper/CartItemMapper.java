package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.CartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartItemMapper extends BaseMapper<CartItem> {
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    List<CartItem> selectSelectedByUserId(@Param("userId") Long userId);

    CartItem selectByCartIdAndSkuId(@Param("cartId") Long cartId, @Param("skuId") Long skuId);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteSelectedByUserId(@Param("userId") Long userId);
}
