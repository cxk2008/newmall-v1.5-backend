package com.itye.mall.service;

import com.itye.mall.entity.ProductCategory;
import com.itye.mall.mapper.ProductCategoryMapper;
import com.itye.mall.vo.product.CategoryTreeNodeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductCategoryService {
    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryService(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    public List<CategoryTreeNodeVO> tree() {
        List<ProductCategory> categories = productCategoryMapper.selectEnabled();
        Map<Long, CategoryTreeNodeVO> nodeMap = new LinkedHashMap<>();
        for (ProductCategory category : categories) {
            nodeMap.put(category.getId(), CategoryTreeNodeVO.builder()
                    .id(category.getId())
                    .parentId(category.getParentId())
                    .name(category.getName())
                    .iconUrl(category.getIconUrl())
                    .bannerUrl(category.getBannerUrl())
                    .sortOrder(category.getSortOrder())
                    .level(category.getLevel())
                    .children(new ArrayList<>())
                    .build());
        }

        List<CategoryTreeNodeVO> roots = new ArrayList<>();
        for (CategoryTreeNodeVO node : nodeMap.values()) {
            if (node.getParentId() == null || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }
}
