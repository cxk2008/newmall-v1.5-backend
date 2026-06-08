package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.ProductAttributeValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductAttributeValueMapper extends BaseMapper<ProductAttributeValue> {
    List<ProductAttributeValue> selectByProductId(@Param("productId") Long productId);
}
