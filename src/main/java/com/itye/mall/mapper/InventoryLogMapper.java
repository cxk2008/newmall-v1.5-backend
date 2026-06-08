package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.InventoryLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
    List<InventoryLog> selectBySkuIdPage(@Param("skuId") Long skuId,
                                         @Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize);

    long countBySkuId(@Param("skuId") Long skuId);
}
