package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminProductRequest;
import com.itye.mall.dto.admin.AdminSkuRequest;
import com.itye.mall.entity.Brand;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductAttributeValue;
import com.itye.mall.entity.ProductCategory;
import com.itye.mall.entity.ProductImage;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.mapper.BrandMapper;
import com.itye.mall.mapper.ProductAttributeValueMapper;
import com.itye.mall.mapper.ProductCategoryMapper;
import com.itye.mall.mapper.ProductImageMapper;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import com.itye.mall.vo.admin.AdminProductListItemVO;
import com.itye.mall.vo.product.ProductDetailVO;
import com.itye.mall.vo.product.ProductSkuVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminProductService {
    private static final int PRODUCT_DRAFT = 1;
    private static final int PRODUCT_ON_SALE = 2;
    private static final int PRODUCT_OFF_SALE = 3;
    private static final int SKU_ENABLED = 1;
    private static final int SKU_DISABLED = 2;

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final BrandMapper brandMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    public AdminProductService(ProductMapper productMapper,
                               ProductSkuMapper productSkuMapper,
                               ProductCategoryMapper productCategoryMapper,
                               BrandMapper brandMapper,
                               ProductImageMapper productImageMapper,
                               ProductAttributeValueMapper productAttributeValueMapper) {
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.brandMapper = brandMapper;
        this.productImageMapper = productImageMapper;
        this.productAttributeValueMapper = productAttributeValueMapper;
    }

    public PageResult<AdminProductListItemVO> list(Long categoryId,
                                                   Long brandId,
                                                   Integer status,
                                                   String keyword,
                                                   Integer pageNum,
                                                   Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        List<AdminProductListItemVO> records = productMapper.selectAdminPage(
                categoryId,
                brandId,
                status,
                keyword,
                PageUtils.offset(normalizedPageNum, normalizedPageSize),
                normalizedPageSize
        ).stream().map(AdminProductListItemVO::from).toList();
        long total = productMapper.countAdmin(categoryId, brandId, status, keyword);
        return PageResult.<AdminProductListItemVO>builder()
                .total(total)
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(records)
                .build();
    }

    public ProductDetailVO detail(Long id) {
        Product product = requireProduct(id);
        List<ProductSkuVO> skus = productSkuMapper.selectByProductId(id).stream()
                .map(ProductSkuVO::from)
                .toList();
        List<ProductImage> images = productImageMapper.selectByProductId(id);
        List<ProductAttributeValue> attributes = productAttributeValueMapper.selectByProductId(id);
        return ProductDetailVO.from(product, skus, images, attributes);
    }

    @Transactional
    public ProductDetailVO create(AdminProductRequest request) {
        validateProductRequest(request);
        if (isOnSale(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先创建至少一个启用 SKU 后再上架商品");
        }
        requireCategory(request.getCategoryId());
        requireBrandIfPresent(request.getBrandId());
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .spuCode(request.getSpuCode().trim())
                .name(request.getName().trim())
                .subtitle(trimToNull(request.getSubtitle()))
                .mainImageUrl(trimToNull(request.getMainImageUrl()))
                .detailHtml(trimToNull(request.getDetailHtml()))
                .unit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : "件")
                .priceMin(BigDecimal.ZERO)
                .priceMax(BigDecimal.ZERO)
                .salesCount(0)
                .viewCount(0)
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .status(defaultProductStatus(request.getStatus()))
                .publishedAt(isOnSale(request.getStatus()) ? now : null)
                .createdAt(now)
                .updatedAt(now)
                .build();
        productMapper.insert(product);
        return detail(product.getId());
    }

    @Transactional
    public ProductDetailVO update(Long id, AdminProductRequest request) {
        Product existing = requireProduct(id);
        validateProductRequest(request);
        requireCategory(request.getCategoryId());
        requireBrandIfPresent(request.getBrandId());
        Integer nextStatus = defaultProductStatus(request.getStatus());
        if (isOnSale(nextStatus)) {
            ensureCanPublish(id);
        }
        Product update = Product.builder()
                .id(id)
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .spuCode(request.getSpuCode().trim())
                .name(request.getName().trim())
                .subtitle(trimToNull(request.getSubtitle()))
                .mainImageUrl(trimToNull(request.getMainImageUrl()))
                .detailHtml(trimToNull(request.getDetailHtml()))
                .unit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : "件")
                .sortOrder(defaultInt(request.getSortOrder(), 0))
                .status(nextStatus)
                .publishedAt(isOnSale(nextStatus) && existing.getPublishedAt() == null ? LocalDateTime.now() : existing.getPublishedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        productMapper.updateAdminById(update);
        refreshPriceRange(id);
        return detail(id);
    }

    @Transactional
    public ProductDetailVO onSale(Long id) {
        requireProduct(id);
        ensureCanPublish(id);
        productMapper.updateById(Product.builder()
                .id(id)
                .status(PRODUCT_ON_SALE)
                .publishedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        refreshPriceRange(id);
        return detail(id);
    }

    @Transactional
    public ProductDetailVO offSale(Long id) {
        requireProduct(id);
        productMapper.updateById(Product.builder()
                .id(id)
                .status(PRODUCT_OFF_SALE)
                .updatedAt(LocalDateTime.now())
                .build());
        return detail(id);
    }

    public void delete(Long id) {
        requireProduct(id);
        productMapper.deleteById(id);
    }

    public List<ProductSkuVO> listSkus(Long productId) {
        requireProduct(productId);
        return productSkuMapper.selectByProductId(productId).stream().map(ProductSkuVO::from).toList();
    }

    @Transactional
    public ProductSkuVO createSku(Long productId, AdminSkuRequest request) {
        requireProduct(productId);
        validateSkuRequest(request, true);
        LocalDateTime now = LocalDateTime.now();
        ProductSku sku = ProductSku.builder()
                .productId(productId)
                .skuCode(request.getSkuCode().trim())
                .name(request.getName().trim())
                .imageUrl(trimToNull(request.getImageUrl()))
                .specJson(trimToNull(request.getSpecJson()))
                .salePrice(request.getSalePrice())
                .marketPrice(request.getMarketPrice())
                .costPrice(request.getCostPrice())
                .weightGram(defaultInt(request.getWeightGram(), 0))
                .stock(defaultInt(request.getStock(), 0))
                .lockedStock(0)
                .lowStockThreshold(defaultInt(request.getLowStockThreshold(), 0))
                .status(defaultSkuStatus(request.getStatus()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        productSkuMapper.insert(sku);
        refreshPriceRange(productId);
        return ProductSkuVO.from(requireSku(sku.getId()));
    }

    @Transactional
    public ProductSkuVO updateSku(Long id, AdminSkuRequest request) {
        ProductSku existing = requireSku(id);
        validateSkuRequest(request, true);
        if (request.getStock() != null && !request.getStock().equals(existing.getStock())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请使用库存调整接口修改 SKU 库存");
        }
        ProductSku update = ProductSku.builder()
                .id(id)
                .skuCode(request.getSkuCode().trim())
                .name(request.getName().trim())
                .imageUrl(trimToNull(request.getImageUrl()))
                .specJson(trimToNull(request.getSpecJson()))
                .salePrice(request.getSalePrice())
                .marketPrice(request.getMarketPrice())
                .costPrice(request.getCostPrice())
                .weightGram(defaultInt(request.getWeightGram(), 0))
                .lowStockThreshold(defaultInt(request.getLowStockThreshold(), 0))
                .status(defaultSkuStatus(request.getStatus()))
                .updatedAt(LocalDateTime.now())
                .build();
        productSkuMapper.updateById(update);
        refreshPriceRange(existing.getProductId());
        return ProductSkuVO.from(requireSku(id));
    }

    public void deleteSku(Long id) {
        ProductSku sku = requireSku(id);
        productSkuMapper.deleteById(id);
        refreshPriceRange(sku.getProductId());
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private ProductSku requireSku(Long id) {
        ProductSku sku = productSkuMapper.selectById(id);
        if (sku == null || sku.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU 不存在");
        }
        return sku;
    }

    private void requireCategory(Long categoryId) {
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null || category.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分类不存在");
        }
    }

    private void requireBrandIfPresent(Long brandId) {
        if (brandId == null) {
            return;
        }
        Brand brand = brandMapper.selectById(brandId);
        if (brand == null || brand.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "品牌不存在");
        }
    }

    private void ensureCanPublish(Long productId) {
        if (productSkuMapper.countEnabledByProductId(productId) < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品上架前必须至少有一个启用 SKU");
        }
    }

    private void refreshPriceRange(Long productId) {
        List<ProductSku> enabledSkus = productSkuMapper.selectEnabledByProductId(productId);
        BigDecimal min = enabledSkus.stream()
                .map(ProductSku::getSalePrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal max = enabledSkus.stream()
                .map(ProductSku::getSalePrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        productMapper.updateById(Product.builder()
                .id(productId)
                .priceMin(min)
                .priceMax(max)
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private void validateProductRequest(AdminProductRequest request) {
        if (request == null || request.getCategoryId() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (!StringUtils.hasText(request.getSpuCode())) {
            throw new IllegalArgumentException("商品 SPU 编码不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        Integer status = request.getStatus();
        if (status != null && status != PRODUCT_DRAFT && status != PRODUCT_ON_SALE && status != PRODUCT_OFF_SALE) {
            throw new IllegalArgumentException("商品状态只能是 1、2 或 3");
        }
    }

    private void validateSkuRequest(AdminSkuRequest request, boolean requireAll) {
        if (request == null) {
            throw new IllegalArgumentException("SKU 参数不能为空");
        }
        if (requireAll && !StringUtils.hasText(request.getSkuCode())) {
            throw new IllegalArgumentException("SKU 编码不能为空");
        }
        if (requireAll && !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("SKU 名称不能为空");
        }
        if (request.getSalePrice() == null || request.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("SKU 价格不能为负数");
        }
        if (request.getMarketPrice() != null && request.getMarketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("SKU 市场价不能为负数");
        }
        if (request.getCostPrice() != null && request.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("SKU 成本价不能为负数");
        }
        if (request.getStock() != null && request.getStock() < 0) {
            throw new IllegalArgumentException("SKU 库存不能为负数");
        }
        if (request.getLowStockThreshold() != null && request.getLowStockThreshold() < 0) {
            throw new IllegalArgumentException("SKU 低库存阈值不能为负数");
        }
        if (request.getWeightGram() != null && request.getWeightGram() < 0) {
            throw new IllegalArgumentException("SKU 重量不能为负数");
        }
        Integer status = request.getStatus();
        if (status != null && status != SKU_ENABLED && status != SKU_DISABLED) {
            throw new IllegalArgumentException("SKU 状态只能是 1 或 2");
        }
    }

    private boolean isOnSale(Integer status) {
        return Integer.valueOf(PRODUCT_ON_SALE).equals(status);
    }

    private Integer defaultProductStatus(Integer status) {
        return status == null ? PRODUCT_DRAFT : status;
    }

    private Integer defaultSkuStatus(Integer status) {
        return status == null ? SKU_ENABLED : status;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
