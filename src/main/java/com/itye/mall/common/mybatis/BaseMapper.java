package com.itye.mall.common.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T> {
    T selectById(@Param("id") Long id);

    List<T> selectAll();

    int insert(T entity);

    int updateById(T entity);

    int deleteById(@Param("id") Long id);
}
