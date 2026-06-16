package com.itye.mall.es.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.itye.mall.es.document.ProductEsDocument;
import com.itye.mall.es.index.ProductIndexInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Component
public class ProductSearchApi {
    private final ElasticsearchClient elasticsearchClient;
    private final String productIndexName;

    public ProductSearchApi(ElasticsearchClient elasticsearchClient,
                            @Value("${mall.elasticsearch.product-index:" + ProductIndexInitializer.DEFAULT_PRODUCT_INDEX + "}") String productIndexName) {
        this.elasticsearchClient = elasticsearchClient;
        this.productIndexName = productIndexName;
    }

    public SearchPage search(Long categoryId, String keyword, int offset, int pageSize) throws IOException {
        Query query = buildQuery(categoryId, keyword);
        SearchResponse<ProductEsDocument> response = elasticsearchClient.search(search -> search
                        .index(productIndexName)
                        .from(offset)
                        .size(pageSize)
                        .query(query)
                        .sort(sort -> sort.field(field -> field.field("sortOrder").order(SortOrder.Asc)))
                        .sort(sort -> sort.field(field -> field.field("id").order(SortOrder.Desc))),
                ProductEsDocument.class);
        long total = response.hits().total() == null ? 0L : response.hits().total().value();
        List<ProductEsDocument> records = response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(source -> source != null)
                .toList();
        return new SearchPage(total, records);
    }

    private Query buildQuery(Long categoryId, String keyword) {
        return Query.of(query -> query.bool(bool -> {
            bool.filter(filter -> filter.term(term -> term.field("status").value(2)));
            if (categoryId != null) {
                bool.filter(filter -> filter.term(term -> term.field("categoryId").value(categoryId)));
            }
            if (StringUtils.hasText(keyword)) {
                bool.must(must -> must.multiMatch(multiMatch -> multiMatch
                        .query(keyword)
                        .fields("name^4", "subtitle^2", "spuCode^5", "categoryName^1", "brandName^1", "attributeValues")
                ));
            }
            return bool;
        }));
    }

    public record SearchPage(long total, List<ProductEsDocument> records) {
    }
}
