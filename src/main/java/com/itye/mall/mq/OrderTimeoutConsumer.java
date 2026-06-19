package com.itye.mall.mq;

import com.itye.mall.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = OrderTimeoutProducer.TOPIC,
        selectorExpression = OrderTimeoutProducer.TAG_CANCEL,
        consumerGroup = "mall-order-timeout-consumer"
)
public class OrderTimeoutConsumer implements RocketMQListener<OrderTimeoutMessage> {
    private final OrderService orderService;

    public OrderTimeoutConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void onMessage(OrderTimeoutMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        orderService.cancelTimeoutOrder(message.getOrderId(), message.getOrderNo());
    }
}
