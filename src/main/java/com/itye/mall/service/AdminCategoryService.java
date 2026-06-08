package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminCategoryRequest;
import com.itye.mall.entity.ProductCategory;
import com.itye.mall.mapper.ProductCategoryMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminCategoryService {
    private final ProductCategoryMapper productCategoryMapper;

    public AdminCategoryService(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    public PageResult<ProductCategory> list(Long parentId, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<ProductCategory> records = productCategoryMapper.selectAdminPage(
                parentId,
                status,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        );
        long total = productCategoryMapper.countAdmin(parentId, status);
        return PageResult.<ProductCategory>builder()
                .total(total)
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public ProductCategory detail(Long id) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null || category.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    public ProductCategory create(AdminCategoryRequest request) {
        validateRequest(request);
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = detail(request.getParentId());
        }
        LocalDateTime now = LocalDateTime.now();
        ProductCategory category = ProductCategory.builder()
                .parentId(request.getParentId())
                .name(request.getName().trim())
                .iconUrl(trimToNull(request.getIconUrl()))
                .bannerUrl(trimToNull(request.getBannerUrl()))
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .level(parent == null ? 1 : defaultInt(parent.getLevel(), 1) + 1)
                .status(defaultStatus(request.getStatus()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        productCategoryMapper.insert(category);
        return detail(category.getId());
    }

    public ProductCategory update(Long id, AdminCategoryRequest request) {
        detail(id);
        validateRequest(request);
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父级分类不能是自己");
            }
            parent = detail(request.getParentId());
        }
        ProductCategory update = ProductCategory.builder()
                .id(id)
                .parentId(request.getParentId())
                .name(request.getName().trim())
                .iconUrl(trimToNull(request.getIconUrl()))
                .bannerUrl(trimToNull(request.getBannerUrl()))
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .level(parent == null ? 1 : defaultInt(parent.getLevel(), 1) + 1)
                .status(defaultStatus(request.getStatus()))
                .updatedAt(LocalDateTime.now())
                .build();
        productCategoryMapper.updateAdminById(update);
        return detail(id);
    }

    public void delete(Long id) {
        detail(id);
        productCategoryMapper.deleteById(id);
    }

    private void validateRequest(AdminCategoryRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        validateStatus(request.getStatus());
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 1 && status != 2) {
            throw new IllegalArgumentException("分类状态只能是 1 或 2");
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
