package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminBannerRequest;
import com.itye.mall.entity.Banner;
import com.itye.mall.mapper.BannerMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerService {
    private static final int ENABLED = 1;
    private static final int DISABLED = 2;

    private final BannerMapper bannerMapper;
    private final OperationLogService operationLogService;

    public BannerService(BannerMapper bannerMapper, OperationLogService operationLogService) {
        this.bannerMapper = bannerMapper;
        this.operationLogService = operationLogService;
    }

    public List<Banner> listVisible(String position) {
        return bannerMapper.selectVisibleByPosition(StringUtils.hasText(position) ? position.trim() : "home");
    }

    public PageResult<Banner> adminList(String position, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<Banner>builder()
                .total(bannerMapper.countAdmin(position, status))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(bannerMapper.selectAdminPage(
                        position,
                        status,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ))
                .build();
    }

    public Banner detail(Long id) {
        return requireBanner(id);
    }

    @Transactional
    public Banner adminCreate(Long adminId, AdminBannerRequest request) {
        validateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        Banner banner = Banner.builder()
                .title(request.getTitle().trim())
                .imageUrl(request.getImageUrl().trim())
                .linkUrl(trimToNull(request.getLinkUrl()))
                .position(StringUtils.hasText(request.getPosition()) ? request.getPosition().trim() : "home")
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus() == null ? ENABLED : request.getStatus())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .createdAt(now)
                .updatedAt(now)
                .build();
        bannerMapper.insert(banner);
        operationLogService.write(adminId, "banner.create", "banner", banner.getId(), "{\"title\":\"" + banner.getTitle() + "\"}");
        return requireBanner(banner.getId());
    }

    @Transactional
    public Banner adminUpdate(Long adminId, Long id, AdminBannerRequest request) {
        requireBanner(id);
        validateRequest(request);
        Banner update = Banner.builder()
                .id(id)
                .title(request.getTitle().trim())
                .imageUrl(request.getImageUrl().trim())
                .linkUrl(trimToNull(request.getLinkUrl()))
                .position(StringUtils.hasText(request.getPosition()) ? request.getPosition().trim() : "home")
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus() == null ? ENABLED : request.getStatus())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .updatedAt(LocalDateTime.now())
                .build();
        bannerMapper.updateAdminById(update);
        operationLogService.write(adminId, "banner.update", "banner", id, "{\"title\":\"" + update.getTitle() + "\"}");
        return requireBanner(id);
    }

    @Transactional
    public void adminDelete(Long adminId, Long id) {
        requireBanner(id);
        bannerMapper.deleteById(id);
        operationLogService.write(adminId, "banner.delete", "banner", id, null);
    }

    private Banner requireBanner(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Banner 不存在");
        }
        return banner;
    }

    private void validateRequest(AdminBannerRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("Banner 标题不能为空");
        }
        if (!StringUtils.hasText(request.getImageUrl())) {
            throw new IllegalArgumentException("Banner 图片不能为空");
        }
        if (request.getStatus() != null && request.getStatus() != ENABLED && request.getStatus() != DISABLED) {
            throw new IllegalArgumentException("Banner 状态只能是 1 或 2");
        }
        if (request.getStartsAt() != null && request.getEndsAt() != null && request.getStartsAt().isAfter(request.getEndsAt())) {
            throw new IllegalArgumentException("Banner 展示时间不正确");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
