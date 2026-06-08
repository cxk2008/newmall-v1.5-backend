package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminBrandRequest;
import com.itye.mall.entity.Brand;
import com.itye.mall.mapper.BrandMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminBrandService {
    private final BrandMapper brandMapper;

    public AdminBrandService(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    public PageResult<Brand> list(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<Brand> records = brandMapper.selectAdminPage(
                keyword,
                status,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        );
        long total = brandMapper.countAdmin(keyword, status);
        return PageResult.<Brand>builder()
                .total(total)
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public Brand detail(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null || brand.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "品牌不存在");
        }
        return brand;
    }

    public Brand create(AdminBrandRequest request) {
        validateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .logoUrl(trimToNull(request.getLogoUrl()))
                .description(trimToNull(request.getDescription()))
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .status(defaultStatus(request.getStatus()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        brandMapper.insert(brand);
        return detail(brand.getId());
    }

    public Brand update(Long id, AdminBrandRequest request) {
        detail(id);
        validateRequest(request);
        Brand update = Brand.builder()
                .id(id)
                .name(request.getName().trim())
                .logoUrl(trimToNull(request.getLogoUrl()))
                .description(trimToNull(request.getDescription()))
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .status(defaultStatus(request.getStatus()))
                .updatedAt(LocalDateTime.now())
                .build();
        brandMapper.updateById(update);
        return detail(id);
    }

    public void delete(Long id) {
        detail(id);
        brandMapper.deleteById(id);
    }

    private void validateRequest(AdminBrandRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("品牌名称不能为空");
        }
        if (request.getStatus() != null && request.getStatus() != 1 && request.getStatus() != 2) {
            throw new IllegalArgumentException("品牌状态只能是 1 或 2");
        }
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
