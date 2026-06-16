package com.itye.mall.es.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnBean(ElasticsearchClient.class)
@ConditionalOnProperty(prefix = "mall.elasticsearch", name = "index-init-enabled", havingValue = "true", matchIfMissing = true)
public class ProductIndexInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProductIndexInitializer.class);

    public static final String DEFAULT_PRODUCT_INDEX = "mall_products";

    private final ElasticsearchClient elasticsearchClient;
    private final String productIndexName;

    public ProductIndexInitializer(ElasticsearchClient elasticsearchClient,
                                   @Value("${mall.elasticsearch.product-index:" + DEFAULT_PRODUCT_INDEX + "}") String productIndexName) {
        this.elasticsearchClient = elasticsearchClient;
        this.productIndexName = productIndexName;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initProductIndex();
    }

    private void initProductIndex() throws IOException {
        boolean exists = elasticsearchClient.indices()
                .exists(ExistsRequest.of(request -> request.index(productIndexName)))
                .value();
        if (exists) {
            log.info("Elasticsearch product index already exists: {}", productIndexName);
            return;
        }

        elasticsearchClient.indices().create(request -> request
                .index(productIndexName)
                .settings(settings -> settings
                        .numberOfShards("1")
                        .numberOfReplicas("0")
                )
                .mappings(mapping -> mapping
                        .dynamic(DynamicMapping.False)
                        .properties("id", property -> property.long_(field -> field))
                        .properties("categoryId", property -> property.long_(field -> field))
                        .properties("categoryName", property -> property.keyword(field -> field.ignoreAbove(128)))
                        .properties("brandId", property -> property.long_(field -> field))
                        .properties("brandName", property -> property.keyword(field -> field.ignoreAbove(128)))
                        .properties("spuCode", property -> property.keyword(field -> field.ignoreAbove(64)))
                        .properties("name", property -> property.text(field -> field
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart")
                                .fields("keyword", keyword -> keyword.keyword(keywordField -> keywordField.ignoreAbove(256)))
                        ))
                        .properties("subtitle", property -> property.text(field -> field
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart")
                        ))
                        .properties("mainImageUrl", property -> property.keyword(field -> field.index(false)))
                        .properties("detailHtml", property -> property.text(field -> field
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart")
                                .index(false)
                        ))
                        .properties("unit", property -> property.keyword(field -> field.ignoreAbove(32)))
                        .properties("priceMin", property -> property.scaledFloat(field -> field.scalingFactor(100.0)))
                        .properties("priceMax", property -> property.scaledFloat(field -> field.scalingFactor(100.0)))
                        .properties("salesCount", property -> property.integer(field -> field))
                        .properties("viewCount", property -> property.integer(field -> field))
                        .properties("sortOrder", property -> property.integer(field -> field))
                        .properties("status", property -> property.integer(field -> field))
                        .properties("publishedAt", property -> property.date(field -> field.format("strict_date_optional_time||yyyy-MM-dd HH:mm:ss")))
                        .properties("createdAt", property -> property.date(field -> field.format("strict_date_optional_time||yyyy-MM-dd HH:mm:ss")))
                        .properties("updatedAt", property -> property.date(field -> field.format("strict_date_optional_time||yyyy-MM-dd HH:mm:ss")))
                        .properties("imageUrls", property -> property.keyword(field -> field.index(false)))
                        .properties("attributeValues", property -> property.text(field -> field
                                .analyzer("ik_max_word")
                                .searchAnalyzer("ik_smart")
                        ))
                        .properties("skus", property -> property.nested(nested -> nested
                                .properties("id", skuProperty -> skuProperty.long_(field -> field))
                                .properties("skuCode", skuProperty -> skuProperty.keyword(field -> field.ignoreAbove(64)))
                                .properties("name", skuProperty -> skuProperty.text(field -> field
                                        .analyzer("ik_max_word")
                                        .searchAnalyzer("ik_smart")
                                ))
                                .properties("imageUrl", skuProperty -> skuProperty.keyword(field -> field.index(false)))
                                .properties("specJson", skuProperty -> skuProperty.keyword(field -> field.ignoreAbove(1024)))
                                .properties("salePrice", skuProperty -> skuProperty.scaledFloat(field -> field.scalingFactor(100.0)))
                                .properties("availableStock", skuProperty -> skuProperty.integer(field -> field))
                                .properties("status", skuProperty -> skuProperty.integer(field -> field))
                        ))
                )
        );
        log.info("Elasticsearch product index created: {}", productIndexName);
    }
}
