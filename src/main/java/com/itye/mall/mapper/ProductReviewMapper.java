package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.ProductReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductReviewMapper extends BaseMapper<ProductReview> {
    ProductReview selectByOrderItemId(@Param("orderItemId") Long orderItemId);

    List<ProductReview> selectByProductIdPage(@Param("productId") Long productId,
                                              @Param("offset") Integer offset,
                                              @Param("pageSize") Integer pageSize);

    long countByProductId(@Param("productId") Long productId);

    List<ProductReview> selectByOrderId(@Param("orderId") Long orderId);
}
