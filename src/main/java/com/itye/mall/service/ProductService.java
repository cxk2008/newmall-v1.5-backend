package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductAttributeValue;
import com.itye.mall.entity.ProductImage;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.es.service.ProductSearchEsService;
import com.itye.mall.mapper.ProductAttributeValueMapper;
import com.itye.mall.mapper.ProductImageMapper;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import com.itye.mall.vo.product.ProductDetailVO;
import com.itye.mall.vo.product.ProductListItemVO;
import com.itye.mall.vo.product.ProductSkuVO;

@Service
public class ProductService {
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductAttributeValueMapper productAttributeValueMapper;
    private final ProductSearchEsService productSearchEsService;

    public ProductService(ProductMapper productMapper,
                          ProductSkuMapper productSkuMapper,
                          ProductImageMapper productImageMapper,
                          ProductAttributeValueMapper productAttributeValueMapper,
                          ProductSearchEsService productSearchEsService) {
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productImageMapper = productImageMapper;
        this.productAttributeValueMapper = productAttributeValueMapper;
        this.productSearchEsService = productSearchEsService;
    }

    public PageResult<ProductListItemVO> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize) {
        return productSearchEsService.list(categoryId, keyword, pageNum, pageSize);
    }

    public PageResult<ProductListItemVO> search(String keyword, Integer pageNum, Integer pageSize) {
        return productSearchEsService.list(null, keyword, pageNum, pageSize);
    }

    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectOnSaleById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在或已下架");
        }
        productMapper.incrementViewCount(id);
        List<ProductSkuVO> skus = productSkuMapper.selectEnabledByProductId(id).stream()
                .map(ProductSkuVO::from)
                .toList();
        List<ProductImage> images = productImageMapper.selectByProductId(id);
        List<ProductAttributeValue> attributes = productAttributeValueMapper.selectByProductId(id);
        return ProductDetailVO.from(product, skus, images, attributes);
    }

    public List<ProductSkuVO> skus(Long productId) {
        Product product = productMapper.selectOnSaleById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在或已下架");
        }
        return productSkuMapper.selectEnabledByProductId(productId).stream().map(ProductSkuVO::from).toList();
    }

    public Product requireOnSaleProduct(Long productId) {
        Product product = productMapper.selectOnSaleById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品不存在或已下架");
        }
        return product;
    }

    public ProductSku requireEnabledSku(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null || sku.getDeletedAt() != null || !Integer.valueOf(1).equals(sku.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SKU 不存在或已禁用");
        }
        requireOnSaleProduct(sku.getProductId());
        return sku;
    }
}
