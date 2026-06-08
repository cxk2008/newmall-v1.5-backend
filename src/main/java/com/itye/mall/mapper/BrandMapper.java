package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Brand;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    List<Brand> selectAdminPage(@Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("keyword") String keyword, @Param("status") Integer status);
}
