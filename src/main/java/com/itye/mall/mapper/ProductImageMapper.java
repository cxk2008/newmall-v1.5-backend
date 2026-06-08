package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.ProductImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductImageMapper extends BaseMapper<ProductImage> {
    List<ProductImage> selectByProductId(@Param("productId") Long productId);
}
