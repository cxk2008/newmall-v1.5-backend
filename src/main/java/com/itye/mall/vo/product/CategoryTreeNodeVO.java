package com.itye.mall.vo.product;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CategoryTreeNodeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String iconUrl;
    private String bannerUrl;
    private Integer sortOrder;
    private Integer level;
    @Builder.Default
    private List<CategoryTreeNodeVO> children = new ArrayList<>();
}
