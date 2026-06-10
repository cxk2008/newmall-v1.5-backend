package com.itye.mall.service;

import com.itye.mall.entity.InventoryLog;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.mapper.InventoryLogMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private final ProductSkuMapper productSkuMapper;
    private final InventoryLogMapper inventoryLogMapper;

    public InventoryService(ProductSkuMapper productSkuMapper, InventoryLogMapper inventoryLogMapper) {
        this.productSkuMapper = productSkuMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }

    public void lockStock(Long skuId, Integer quantity, String bizType, String bizId) {
        int updated = productSkuMapper.lockStock(skuId, quantity);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不足");
        }
        writeLog(skuId, 3, quantity, bizType, bizId, "订单锁定库存");
    }

    public void unlockStock(Long skuId, Integer quantity, String bizType, String bizId) {
        int updated = productSkuMapper.unlockStock(skuId, quantity);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "释放库存失败");
        }
        writeLog(skuId, 4, -quantity, bizType, bizId, "订单释放锁定库存");
    }

    public void deductLockedStock(Long skuId, Integer quantity, String bizType, String bizId) {
        int updated = productSkuMapper.deductLockedStock(skuId, quantity);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "扣减库存失败");
        }
        writeLog(skuId, 5, -quantity, bizType, bizId, "支付成功扣减库存");
    }

    public PageResult<InventoryLog> listLogs(Long skuId, Integer pageNum, Integer pageSize) {
        requireSku(skuId);
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<InventoryLog>builder()
                .total(inventoryLogMapper.countBySkuId(skuId))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(inventoryLogMapper.selectBySkuIdPage(
                        skuId,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ))
                .build();
    }

    @Transactional
    public ProductSku increaseStock(Long skuId, Integer quantity, String note) {
        validateAdjustQuantity(quantity);
        int updated = productSkuMapper.increaseStock(skuId, quantity);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU 不存在");
        }
        writeLog(skuId, 1, quantity, "admin", String.valueOf(skuId), note == null ? "后台增加库存" : note);
        return requireSku(skuId);
    }

    @Transactional
    public ProductSku decreaseStock(Long skuId, Integer quantity, String note) {
        validateAdjustQuantity(quantity);
        int updated = productSkuMapper.decreaseStock(skuId, quantity);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存不足或 SKU 不存在");
        }
        writeLog(skuId, 2, -quantity, "admin", String.valueOf(skuId), note == null ? "后台减少库存" : note);
        return requireSku(skuId);
    }

    private void validateAdjustQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("调整数量必须大于 0");
        }
    }

    private ProductSku requireSku(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null || sku.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU 不存在");
        }
        return sku;
    }

    private void writeLog(Long skuId, Integer changeType, Integer quantityChange, String bizType, String bizId, String note) {
        ProductSku sku = requireSku(skuId);
        inventoryLogMapper.insert(InventoryLog.builder()
                .skuId(skuId)
                .changeType(changeType)
                .quantityChange(quantityChange)
                .stockAfter(sku.getStock())
                .lockedStockAfter(sku.getLockedStock())
                .bizType(bizType)
                .bizId(bizId)
                .note(note)
                .createdAt(java.time.LocalDateTime.now())
                .build());
    }
}
