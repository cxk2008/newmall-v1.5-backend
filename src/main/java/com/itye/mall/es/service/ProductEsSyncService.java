package com.itye.mall.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.itye.mall.es.document.ProductEsDocument;
import com.itye.mall.es.index.ProductIndexInitializer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductEsSyncService {
    private static final Logger log = LoggerFactory.getLogger(ProductEsSyncService.class);
    private static final int DEFAULT_BATCH_SIZE = 200;

    private final ElasticsearchClient elasticsearchClient;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductAttributeValueMapper productAttributeValueMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final BrandMapper brandMapper;
    private final String productIndexName;

    public ProductEsSyncService(ElasticsearchClient elasticsearchClient,
                                ProductMapper productMapper,
                                ProductSkuMapper productSkuMapper,
                                ProductImageMapper productImageMapper,
                                ProductAttributeValueMapper productAttributeValueMapper,
                                ProductCategoryMapper productCategoryMapper,
                                BrandMapper brandMapper,
                                @Value("${mall.elasticsearch.product-index:" + ProductIndexInitializer.DEFAULT_PRODUCT_INDEX + "}") String productIndexName) {
        this.elasticsearchClient = elasticsearchClient;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productImageMapper = productImageMapper;
        this.productAttributeValueMapper = productAttributeValueMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.brandMapper = brandMapper;
        this.productIndexName = productIndexName;
    }

    public long syncAllOnSaleProducts() throws IOException {
        return syncAllOnSaleProducts(DEFAULT_BATCH_SIZE);
    }

    public long syncAllOnSaleProducts(int batchSize) throws IOException {
        int normalizedBatchSize = batchSize < 1 ? DEFAULT_BATCH_SIZE : batchSize;
        long total = productMapper.countOnSale(null, null);
        long synced = 0;
        for (int offset = 0; offset < total; offset += normalizedBatchSize) {
            List<Product> products = productMapper.selectOnSalePage(null, null, offset, normalizedBatchSize);
            if (products.isEmpty()) {
                break;
            }
            bulkIndex(products);
            synced += products.size();
        }
        log.info("Synced {} on-sale products to Elasticsearch index {}", synced, productIndexName);
        return synced;
    }

    public void syncProduct(Long productId) throws IOException {
        Product product = productMapper.selectOnSaleById(productId);
        if (product == null) {
            deleteProduct(productId);
            return;
        }
        ProductEsDocument document = buildDocument(product);
        elasticsearchClient.index(request -> request
                .index(productIndexName)
                .id(String.valueOf(document.getId()))
                .document(document)
                .refresh(Refresh.True)
        );
    }

    public void deleteProduct(Long productId) throws IOException {
        boolean exists = elasticsearchClient.exists(request -> request
                .index(productIndexName)
                .id(String.valueOf(productId))
        ).value();
        if (!exists) {
            return;
        }
        elasticsearchClient.delete(request -> request
                .index(productIndexName)
                .id(String.valueOf(productId))
                .refresh(Refresh.True)
        );
    }

    private void bulkIndex(List<Product> products) throws IOException {
        List<BulkOperation> operations = new ArrayList<>(products.size());
        for (Product product : products) {
            ProductEsDocument document = buildDocument(product);
            operations.add(BulkOperation.of(operation -> operation
                    .index(index -> index
                            .index(productIndexName)
                            .id(String.valueOf(document.getId()))
                            .document(document)
                    )
            ));
        }
        BulkResponse response = elasticsearchClient.bulk(request -> request
                .index(productIndexName)
                .refresh(Refresh.False)
                .operations(operations)
        );
        if (response.errors()) {
            String reason = response.items().stream()
                    .filter(item -> item.error() != null)
                    .findFirst()
                    .map(item -> item.error().reason())
                    .orElse("unknown bulk error");
            throw new IllegalStateException("同步商品到 Elasticsearch 失败: " + reason);
        }
    }

    private ProductEsDocument buildDocument(Product product) {
        ProductCategory category = product.getCategoryId() == null ? null : productCategoryMapper.selectById(product.getCategoryId());
        Brand brand = product.getBrandId() == null ? null : brandMapper.selectById(product.getBrandId());
        List<ProductSku> skus = productSkuMapper.selectEnabledByProductId(product.getId());
        List<ProductImage> images = productImageMapper.selectByProductId(product.getId());
        List<ProductAttributeValue> attributes = productAttributeValueMapper.selectByProductId(product.getId());
        return ProductEsDocument.from(product, category, brand, skus, images, attributes);
    }
}
