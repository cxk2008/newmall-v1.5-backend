package com.itye.mall.es.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.es.api.ProductSearchApi;
import com.itye.mall.vo.product.ProductListItemVO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProductSearchEsService {
    private final ProductSearchApi productSearchApi;

    public ProductSearchEsService(ProductSearchApi productSearchApi) {
        this.productSearchApi = productSearchApi;
    }

    public PageResult<ProductListItemVO> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        try {
            ProductSearchApi.SearchPage searchPage = productSearchApi.search(
                    categoryId,
                    keyword,
                    PageUtils.offset(normalizedPageNum, normalizedPageSize),
                    normalizedPageSize
            );
            return PageResult.<ProductListItemVO>builder()
                    .total(searchPage.total())
                    .pageNum(normalizedPageNum)
                    .pageSize(normalizedPageSize)
                    .records(searchPage.records().stream().map(ProductListItemVO::from).toList())
                    .build();
        } catch (IOException ex) {
            throw new IllegalStateException("ES 商品搜索失败", ex);
        }
    }
}
