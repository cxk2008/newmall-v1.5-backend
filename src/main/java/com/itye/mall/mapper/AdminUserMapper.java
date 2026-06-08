package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.AdminUser;
import org.apache.ibatis.annotations.Param;

public interface AdminUserMapper extends BaseMapper<AdminUser> {
    AdminUser selectByUsername(@Param("username") String username);
}
