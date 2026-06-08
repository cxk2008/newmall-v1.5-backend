package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.entity.Favorite;
import com.itye.mall.entity.Product;
import com.itye.mall.mapper.FavoriteMapper;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.vo.favorite.FavoriteVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, ProductMapper productMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
    }

    public PageResult<FavoriteVO> list(Long userId, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<FavoriteVO>builder()
                .total(favoriteMapper.countByUserId(userId))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(favoriteMapper.selectByUserIdPage(
                        userId,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ).stream().map(this::toVO).toList())
                .build();
    }

    public FavoriteVO add(Long userId, Long productId) {
        Product product = productMapper.selectOnSaleById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在或已下架");
        }
        Favorite existing = favoriteMapper.selectByUserIdAndProductId(userId, productId);
        if (existing != null) {
            return toVO(existing);
        }
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .productId(productId)
                .createdAt(LocalDateTime.now())
                .build();
        favoriteMapper.insert(favorite);
        return toVO(favorite);
    }

    public void delete(Long userId, Long productId) {
        favoriteMapper.deleteByUserIdAndProductId(userId, productId);
    }

    private FavoriteVO toVO(Favorite favorite) {
        return FavoriteVO.from(favorite, productMapper.selectById(favorite.getProductId()));
    }
}
