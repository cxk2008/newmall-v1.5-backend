package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.ProductSku;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    List<ProductSku> selectEnabledByProductId(@Param("productId") Long productId);

    List<ProductSku> selectByProductId(@Param("productId") Long productId);

    long countEnabledByProductId(@Param("productId") Long productId);

    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int unlockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int deductLockedStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
