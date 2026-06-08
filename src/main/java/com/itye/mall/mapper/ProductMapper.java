package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {
    Product selectOnSaleById(@Param("id") Long id);

    List<Product> selectOnSalePage(@Param("categoryId") Long categoryId,
                                   @Param("keyword") String keyword,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    long countOnSale(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    int incrementViewCount(@Param("id") Long id);

    List<Product> selectAdminPage(@Param("categoryId") Long categoryId,
                                  @Param("brandId") Long brandId,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword,
                                  @Param("offset") Integer offset,
                                  @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("categoryId") Long categoryId,
                    @Param("brandId") Long brandId,
                    @Param("status") Integer status,
                    @Param("keyword") String keyword);

    int updateAdminById(Product product);
}
