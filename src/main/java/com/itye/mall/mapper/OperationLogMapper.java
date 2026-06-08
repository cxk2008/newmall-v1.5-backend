package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperationLogMapper extends BaseMapper<OperationLog> {
    List<OperationLog> selectPage(@Param("adminId") Long adminId,
                                  @Param("action") String action,
                                  @Param("offset") Integer offset,
                                  @Param("pageSize") Integer pageSize);

    long count(@Param("adminId") Long adminId, @Param("action") String action);
}
