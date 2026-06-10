package com.itye.mall.service;

import com.itye.mall.common.constant.SeckillOrderStatus;
import com.itye.mall.entity.SeckillOrder;
import com.itye.mall.mapper.SeckillItemMapper;
import com.itye.mall.mapper.SeckillOrderMapper;
import com.itye.mall.mq.SeckillOrderMessage;
import com.itye.mall.vo.order.OrderVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SeckillOrderService {
    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillItemMapper seckillItemMapper;
    private final OrderService orderService;
    private final SeckillResultService seckillResultService;

    public SeckillOrderService(SeckillOrderMapper seckillOrderMapper,
                               SeckillItemMapper seckillItemMapper,
                               OrderService orderService,
                               SeckillResultService seckillResultService) {
        this.seckillOrderMapper = seckillOrderMapper;
        this.seckillItemMapper = seckillItemMapper;
        this.orderService = orderService;
        this.seckillResultService = seckillResultService;
    }

    @Transactional
    public void createOrder(SeckillOrderMessage message) {
        SeckillOrder existing = seckillOrderMapper.selectByRequestNo(message.getRequestNo());
        if (existing != null) {
            syncExistingResult(existing);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SeckillOrder seckillOrder = SeckillOrder.builder()
                .requestNo(message.getRequestNo())
                .activityId(message.getActivityId())
                .seckillItemId(message.getSeckillItemId())
                .userId(message.getUserId())
                .status(SeckillOrderStatus.PROCESSING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        try {
            seckillOrderMapper.insert(seckillOrder);
        } catch (DuplicateKeyException ex) {
            syncExistingResult(seckillOrderMapper.selectByRequestNo(message.getRequestNo()));
            return;
        }

        int updated = seckillItemMapper.decreaseAvailableStock(message.getSeckillItemId(), message.getQuantity());
        if (updated != 1) {
            seckillOrderMapper.markFailed(message.getRequestNo(), "秒杀库存不足");
            seckillResultService.markFailed(message.getRequestNo(), "秒杀库存不足");
            return;
        }

        try {
            OrderVO order = orderService.createSeckillOrder(seckillOrder.getId(), message);
            seckillOrderMapper.markOrderCreated(message.getRequestNo(), order.getId(), order.getOrderNo());
            seckillResultService.markSuccess(message.getRequestNo(), order.getId(), order.getOrderNo());
        } catch (RuntimeException ex) {
            seckillItemMapper.increaseAvailableStock(message.getSeckillItemId(), message.getQuantity());
            seckillOrderMapper.markFailed(message.getRequestNo(), ex.getMessage());
            seckillResultService.markFailed(message.getRequestNo(), ex.getMessage());
            throw ex;
        }
    }

    private void syncExistingResult(SeckillOrder existing) {
        if (existing == null) {
            return;
        }
        if (Integer.valueOf(SeckillOrderStatus.ORDER_CREATED).equals(existing.getStatus())) {
            seckillResultService.markSuccess(existing.getRequestNo(), existing.getOrderId(), existing.getOrderNo());
        } else if (Integer.valueOf(SeckillOrderStatus.FAILED).equals(existing.getStatus())
                || Integer.valueOf(SeckillOrderStatus.CANCELED).equals(existing.getStatus())) {
            seckillResultService.markFailed(existing.getRequestNo(), existing.getFailureReason());
        }
    }
}
