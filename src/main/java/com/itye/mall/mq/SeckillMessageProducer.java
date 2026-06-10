package com.itye.mall.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SeckillMessageProducer {
    public static final String TOPIC = "mall_seckill_order";
    public static final String TAG_CREATE_ORDER = "create_order";

    private final RocketMQTemplate rocketMQTemplate;

    public SeckillMessageProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendCreateOrder(SeckillOrderMessage message) {
        rocketMQTemplate.syncSend(TOPIC + ":" + TAG_CREATE_ORDER, MessageBuilder.withPayload(message).build());
    }
}
