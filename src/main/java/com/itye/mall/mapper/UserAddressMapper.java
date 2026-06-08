package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.UserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAddressMapper extends BaseMapper<UserAddress> {
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    int clearDefaultByUserId(@Param("userId") Long userId);

    int softDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
