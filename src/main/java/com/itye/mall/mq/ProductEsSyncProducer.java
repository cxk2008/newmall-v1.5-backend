package com.itye.mall.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProductEsSyncProducer {
    public static final String TOPIC = "mall_product_es_sync";
    public static final String TAG_SYNC = "sync";

    private final RocketMQTemplate rocketMQTemplate;

    public ProductEsSyncProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendSync(Long productId) {
        rocketMQTemplate.syncSend(TOPIC + ":" + TAG_SYNC,
                MessageBuilder.withPayload(ProductEsSyncMessage.builder()
                        .productId(productId)
                        .build()).build());
    }
}
