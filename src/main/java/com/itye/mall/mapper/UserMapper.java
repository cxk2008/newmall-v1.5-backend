package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(@Param("username") String username);

    User selectByPhone(@Param("phone") String phone);

    User selectByEmail(@Param("email") String email);

    User selectByAccount(@Param("account") String account);
}
