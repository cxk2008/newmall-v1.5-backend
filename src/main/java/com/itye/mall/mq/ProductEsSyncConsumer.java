package com.itye.mall.mq;

import com.itye.mall.es.service.ProductEsSyncService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = ProductEsSyncProducer.TOPIC,
        selectorExpression = ProductEsSyncProducer.TAG_SYNC,
        consumerGroup = "mall-product-es-sync-consumer"
)
public class ProductEsSyncConsumer implements RocketMQListener<ProductEsSyncMessage> {
    private final ProductEsSyncService productEsSyncService;

    public ProductEsSyncConsumer(ProductEsSyncService productEsSyncService) {
        this.productEsSyncService = productEsSyncService;
    }

    @Override
    public void onMessage(ProductEsSyncMessage message) {
        if (message == null || message.getProductId() == null) {
            return;
        }
        try {
            productEsSyncService.syncProduct(message.getProductId());
        } catch (Exception ex) {
            throw new IllegalStateException("同步商品到 Elasticsearch 失败", ex);
        }
    }
}
