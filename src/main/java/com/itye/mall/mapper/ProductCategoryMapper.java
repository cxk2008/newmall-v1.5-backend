package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.ProductCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    List<ProductCategory> selectEnabled();

    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId);

    List<ProductCategory> selectAdminPage(@Param("parentId") Long parentId,
                                          @Param("status") Integer status,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("parentId") Long parentId, @Param("status") Integer status);

    int updateAdminById(ProductCategory category);
}
