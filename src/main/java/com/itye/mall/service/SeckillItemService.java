package com.itye.mall.service;

import com.itye.mall.common.constant.SeckillActivityStatus;
import com.itye.mall.dto.admin.AdminSeckillItemRequest;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.entity.SeckillActivity;
import com.itye.mall.entity.SeckillItem;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import com.itye.mall.mapper.SeckillItemMapper;
import com.itye.mall.vo.seckill.SeckillItemVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeckillItemService {
    private static final int ENABLED = 1;
    private static final int DISABLED = 2;

    private final SeckillItemMapper seckillItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OperationLogService operationLogService;

    public SeckillItemService(SeckillItemMapper seckillItemMapper,
                              ProductMapper productMapper,
                              ProductSkuMapper productSkuMapper,
                              ProductService productService,
                              InventoryService inventoryService,
                              OperationLogService operationLogService) {
        this.seckillItemMapper = seckillItemMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.operationLogService = operationLogService;
    }

    public List<SeckillItemVO> listByActivity(Long activityId) {
        return seckillItemMapper.selectByActivityId(activityId).stream().map(this::toVO).toList();
    }

    public List<SeckillItemVO> listEnabledByActivity(Long activityId) {
        return seckillItemMapper.selectEnabledByActivityId(activityId).stream().map(this::toVO).toList();
    }

    public SeckillItemVO detail(Long id) {
        return toVO(requireItem(id));
    }

    @Transactional
    public SeckillItem create(Long adminId, SeckillActivity activity, AdminSeckillItemRequest request) {
        ensureDraft(activity);
        validateRequest(request, true);
        ProductSku sku = productService.requireEnabledSku(request.getSkuId());
        Product product = productService.requireOnSaleProduct(sku.getProductId());
        if (seckillItemMapper.selectByActivityIdAndSkuId(activity.getId(), sku.getId()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该 SKU 已加入当前秒杀活动");
        }
        LocalDateTime now = LocalDateTime.now();
        SeckillItem item = SeckillItem.builder()
                .activityId(activity.getId())
                .productId(product.getId())
                .skuId(sku.getId())
                .seckillPrice(request.getSeckillPrice())
                .seckillStock(request.getSeckillStock())
                .availableStock(request.getSeckillStock())
                .limitPerUser(request.getLimitPerUser() == null ? 1 : request.getLimitPerUser())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus() == null ? ENABLED : request.getStatus())
                .createdAt(now)
                .updatedAt(now)
                .build();
        seckillItemMapper.insert(item);
        operationLogService.write(adminId, "seckill.item.create", "seckill_item", item.getId(), "{\"skuId\":\"" + sku.getId() + "\"}");
        return requireItem(item.getId());
    }

    @Transactional
    public SeckillItem update(Long adminId, SeckillActivity activity, Long id, AdminSeckillItemRequest request) {
        ensureDraft(activity);
        SeckillItem existing = requireItem(id);
        if (!activity.getId().equals(existing.getActivityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动商品不属于当前活动");
        }
        validateRequest(request, false);
        SeckillItem update = SeckillItem.builder()
                .id(id)
                .seckillPrice(request.getSeckillPrice())
                .seckillStock(request.getSeckillStock())
                .availableStock(request.getSeckillStock())
                .limitPerUser(request.getLimitPerUser() == null ? 1 : request.getLimitPerUser())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus() == null ? ENABLED : request.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();
        seckillItemMapper.updateAdminById(update);
        operationLogService.write(adminId, "seckill.item.update", "seckill_item", id, null);
        return requireItem(id);
    }

    @Transactional
    public void delete(Long adminId, SeckillActivity activity, Long id) {
        ensureDraft(activity);
        SeckillItem existing = requireItem(id);
        if (!activity.getId().equals(existing.getActivityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动商品不属于当前活动");
        }
        seckillItemMapper.deleteById(id);
        operationLogService.write(adminId, "seckill.item.delete", "seckill_item", id, null);
    }

    public void lockSkuStockForPublish(SeckillItem item) {
        inventoryService.lockStock(item.getSkuId(), item.getSeckillStock(), "seckill_publish", String.valueOf(item.getId()));
    }

    public SeckillItem requireItem(Long id) {
        SeckillItem item = seckillItemMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "秒杀商品不存在");
        }
        return item;
    }

    private SeckillItemVO toVO(SeckillItem item) {
        Product product = item.getProductId() == null ? null : productMapper.selectById(item.getProductId());
        ProductSku sku = item.getSkuId() == null ? null : productSkuMapper.selectById(item.getSkuId());
        return SeckillItemVO.from(item, product, sku);
    }

    private void ensureDraft(SeckillActivity activity) {
        if (!Integer.valueOf(SeckillActivityStatus.DRAFT).equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有草稿活动可以维护商品");
        }
    }

    private void validateRequest(AdminSeckillItemRequest request, boolean requireSku) {
        if (request == null || (requireSku && request.getSkuId() == null)) {
            throw new IllegalArgumentException("请选择 SKU");
        }
        if (request.getSeckillPrice() == null || request.getSeckillPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("秒杀价不能小于 0");
        }
        if (request.getSeckillStock() == null || request.getSeckillStock() < 1) {
            throw new IllegalArgumentException("秒杀库存必须大于 0");
        }
        if (request.getLimitPerUser() != null && request.getLimitPerUser() < 1) {
            throw new IllegalArgumentException("限购数量必须大于 0");
        }
        if (request.getStatus() != null && request.getStatus() != ENABLED && request.getStatus() != DISABLED) {
            throw new IllegalArgumentException("秒杀商品状态只能是 1 或 2");
        }
    }
}
