package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.entity.OperationLog;
import com.itye.mall.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogService {
    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public PageResult<OperationLog> list(Long adminId, String action, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<OperationLog>builder()
                .total(operationLogMapper.count(adminId, action))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(operationLogMapper.selectPage(
                        adminId,
                        action,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ))
                .build();
    }

    public void write(Long adminId, String action, String targetType, Object targetId, String detailJson) {
        operationLogMapper.insert(OperationLog.builder()
                .adminId(adminId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId == null ? null : String.valueOf(targetId))
                .detailJson(detailJson)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
