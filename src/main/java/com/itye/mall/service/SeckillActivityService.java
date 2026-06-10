package com.itye.mall.service;

import com.itye.mall.common.constant.SeckillActivityStatus;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminSeckillActivityRequest;
import com.itye.mall.entity.SeckillActivity;
import com.itye.mall.entity.SeckillItem;
import com.itye.mall.mapper.SeckillActivityMapper;
import com.itye.mall.mapper.SeckillItemMapper;
import com.itye.mall.vo.seckill.SeckillActivityVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeckillActivityService {
    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillItemMapper seckillItemMapper;
    private final SeckillItemService seckillItemService;
    private final SeckillWarmUpService seckillWarmUpService;
    private final OperationLogService operationLogService;

    public SeckillActivityService(SeckillActivityMapper seckillActivityMapper,
                                  SeckillItemMapper seckillItemMapper,
                                  SeckillItemService seckillItemService,
                                  SeckillWarmUpService seckillWarmUpService,
                                  OperationLogService operationLogService) {
        this.seckillActivityMapper = seckillActivityMapper;
        this.seckillItemMapper = seckillItemMapper;
        this.seckillItemService = seckillItemService;
        this.seckillWarmUpService = seckillWarmUpService;
        this.operationLogService = operationLogService;
    }

    public PageResult<SeckillActivity> adminList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<SeckillActivity>builder()
                .total(seckillActivityMapper.countAdmin(status, keyword))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(seckillActivityMapper.selectAdminPage(
                        status,
                        StringUtils.hasText(keyword) ? keyword.trim() : null,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ))
                .build();
    }

    public SeckillActivityVO detail(Long id) {
        SeckillActivity activity = requireActivity(id);
        return SeckillActivityVO.from(activity, seckillItemService.listByActivity(id));
    }

    public SeckillActivityVO current() {
        SeckillActivity activity = seckillActivityMapper.selectCurrent(LocalDateTime.now());
        if (activity == null) {
            return null;
        }
        return SeckillActivityVO.from(activity, seckillItemService.listEnabledByActivity(activity.getId()));
    }

    @Transactional
    public SeckillActivity create(Long adminId, AdminSeckillActivityRequest request) {
        validateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = SeckillActivity.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status(request.getStatus() == null ? SeckillActivityStatus.DRAFT : request.getStatus())
                .createdBy(adminId)
                .createdAt(now)
                .updatedAt(now)
                .build();
        seckillActivityMapper.insert(activity);
        operationLogService.write(adminId, "seckill.activity.create", "seckill_activity", activity.getId(), "{\"name\":\"" + activity.getName() + "\"}");
        return requireActivity(activity.getId());
    }

    @Transactional
    public SeckillActivity update(Long adminId, Long id, AdminSeckillActivityRequest request) {
        SeckillActivity existing = requireActivity(id);
        if (!Integer.valueOf(SeckillActivityStatus.DRAFT).equals(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有草稿活动可以修改");
        }
        validateRequest(request);
        SeckillActivity update = SeckillActivity.builder()
                .id(id)
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status(request.getStatus() == null ? SeckillActivityStatus.DRAFT : request.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();
        seckillActivityMapper.updateById(update);
        operationLogService.write(adminId, "seckill.activity.update", "seckill_activity", id, "{\"name\":\"" + update.getName() + "\"}");
        return requireActivity(id);
    }

    @Transactional
    public SeckillActivity publish(Long adminId, Long id) {
        SeckillActivity activity = requireActivity(id);
        if (!Integer.valueOf(SeckillActivityStatus.DRAFT).equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有草稿活动可以发布");
        }
        List<SeckillItem> items = seckillItemMapper.selectEnabledByActivityId(id);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先添加启用的秒杀商品");
        }
        LocalDateTime now = LocalDateTime.now();
        for (SeckillItem item : items) {
            seckillItemService.lockSkuStockForPublish(item);
        }
        seckillActivityMapper.updateStatus(id, SeckillActivityStatus.PUBLISHED, now, now);
        SeckillActivity published = requireActivity(id);
        seckillWarmUpService.warmUp(published, seckillItemMapper.selectEnabledByActivityId(id));
        operationLogService.write(adminId, "seckill.activity.publish", "seckill_activity", id, null);
        return published;
    }

    @Transactional
    public SeckillActivity close(Long adminId, Long id) {
        SeckillActivity activity = requireActivity(id);
        List<SeckillItem> items = seckillItemMapper.selectEnabledByActivityId(id);
        seckillActivityMapper.updateStatus(id, SeckillActivityStatus.CLOSED, activity.getWarmUpAt(), LocalDateTime.now());
        seckillWarmUpService.close(id, items);
        operationLogService.write(adminId, "seckill.activity.close", "seckill_activity", id, null);
        return requireActivity(id);
    }

    @Transactional
    public void delete(Long adminId, Long id) {
        SeckillActivity activity = requireActivity(id);
        if (!Integer.valueOf(SeckillActivityStatus.DRAFT).equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有草稿活动可以删除");
        }
        seckillActivityMapper.deleteById(id);
        operationLogService.write(adminId, "seckill.activity.delete", "seckill_activity", id, null);
    }

    public SeckillActivity requireActivity(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null || activity.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "秒杀活动不存在");
        }
        return activity;
    }

    private void validateRequest(AdminSeckillActivityRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("活动名称不能为空");
        }
        if (request.getStartsAt() == null || request.getEndsAt() == null || !request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new IllegalArgumentException("活动时间不正确");
        }
        if (request.getStatus() != null
                && request.getStatus() != SeckillActivityStatus.DRAFT
                && request.getStatus() != SeckillActivityStatus.PUBLISHED) {
            throw new IllegalArgumentException("活动初始状态只能是草稿或已发布");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
