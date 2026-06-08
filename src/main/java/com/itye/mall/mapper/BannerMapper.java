package com.itye.mall.mapper;

import com.itye.mall.common.mybatis.BaseMapper;
import com.itye.mall.entity.Banner;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BannerMapper extends BaseMapper<Banner> {
    List<Banner> selectVisibleByPosition(@Param("position") String position);

    List<Banner> selectAdminPage(@Param("position") String position,
                                 @Param("status") Integer status,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    long countAdmin(@Param("position") String position, @Param("status") Integer status);

    int updateAdminById(Banner banner);
}
