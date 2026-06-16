package com.itye.mall;

import com.itye.mall.es.service.ProductEsSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class MallApplicationTests {
    @Autowired
    private ProductEsSyncService productEsSyncService;

    @Test
    void contextLoads() {
    }

    @Test
    void syncProductsToElasticsearch() throws Exception {
        productEsSyncService.syncAllOnSaleProducts();
    }

}
