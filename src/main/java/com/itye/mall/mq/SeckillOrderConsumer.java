package com.itye.mall.mq;

import com.itye.mall.service.SeckillOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = SeckillMessageProducer.TOPIC,
        selectorExpression = SeckillMessageProducer.TAG_CREATE_ORDER,
        consumerGroup = "mall-seckill-order-consumer"
)
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {
    private final SeckillOrderService seckillOrderService;

    public SeckillOrderConsumer(SeckillOrderService seckillOrderService) {
        this.seckillOrderService = seckillOrderService;
    }

    @Override
    public void onMessage(SeckillOrderMessage message) {
        seckillOrderService.createOrder(message);
    }
}
