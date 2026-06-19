package com.itye.mall.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutProducer {
    public static final String TOPIC = "mall_order_timeout";
    public static final String TAG_CANCEL = "cancel";

    private final RocketMQTemplate rocketMQTemplate;
    private final long delayMillis;

    public OrderTimeoutProducer(RocketMQTemplate rocketMQTemplate,
                                @Value("${mall.order.timeout-delay-millis:900000}") Long delayMillis) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.delayMillis = delayMillis == null ? 900000L : delayMillis;
    }

    public void sendCancelMessage(Long orderId, String orderNo) {
        rocketMQTemplate.syncSendDelayTimeMills(TOPIC + ":" + TAG_CANCEL,
                MessageBuilder.withPayload(OrderTimeoutMessage.builder()
                        .orderId(orderId)
                        .orderNo(orderNo)
                        .build()).build(),
                delayMillis);
    }
}
