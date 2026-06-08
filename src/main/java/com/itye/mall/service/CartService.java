package com.itye.mall.service;

import com.itye.mall.dto.cart.AddCartItemRequest;
import com.itye.mall.dto.cart.UpdateCartItemRequest;
import com.itye.mall.entity.Cart;
import com.itye.mall.entity.CartItem;
import com.itye.mall.entity.Product;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.mapper.CartItemMapper;
import com.itye.mall.mapper.CartMapper;
import com.itye.mall.mapper.ProductMapper;
import com.itye.mall.mapper.ProductSkuMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.itye.mall.vo.cart.CartItemVO;
import com.itye.mall.vo.cart.CartVO;

@Service
public class CartService {
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductService productService;

    public CartService(CartMapper cartMapper,
                       CartItemMapper cartItemMapper,
                       ProductMapper productMapper,
                       ProductSkuMapper productSkuMapper,
                       ProductService productService) {
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productService = productService;
    }

    public CartVO getCart(Long userId) {
        List<CartItemVO> items = cartItemMapper.selectByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
        int selectedCount = items.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getSelected()))
                .mapToInt(CartItemVO::getQuantity)
                .sum();
        BigDecimal selectedAmount = items.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getSelected()))
                .map(CartItemVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartVO.builder()
                .items(items)
                .selectedCount(selectedCount)
                .selectedAmount(selectedAmount)
                .build();
    }

    @Transactional
    public CartVO addItem(Long userId, AddCartItemRequest request) {
        if (request == null || request.getSkuId() == null || request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("购物车参数不正确");
        }
        ProductSku sku = productService.requireEnabledSku(request.getSkuId());
        ensureEnoughStock(sku, request.getQuantity());
        Cart cart = getOrCreateCart(userId);
        CartItem existing = cartItemMapper.selectByCartIdAndSkuId(cart.getId(), sku.getId());
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            cartItemMapper.insert(CartItem.builder()
                    .cartId(cart.getId())
                    .userId(userId)
                    .productId(sku.getProductId())
                    .skuId(sku.getId())
                    .quantity(request.getQuantity())
                    .selected(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        } else {
            int quantity = existing.getQuantity() + request.getQuantity();
            ensureEnoughStock(sku, quantity);
            cartItemMapper.updateById(CartItem.builder()
                    .id(existing.getId())
                    .quantity(quantity)
                    .selected(1)
                    .updatedAt(now)
                    .build());
        }
        return getCart(userId);
    }

    @Transactional
    public CartVO updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = requireUserCartItem(userId, itemId);
        CartItem update = CartItem.builder().id(item.getId()).updatedAt(LocalDateTime.now()).build();
        if (request.getQuantity() != null) {
            if (request.getQuantity() < 1) {
                throw new IllegalArgumentException("商品数量不能小于 1");
            }
            ProductSku sku = productService.requireEnabledSku(item.getSkuId());
            ensureEnoughStock(sku, request.getQuantity());
            update.setQuantity(request.getQuantity());
        }
        if (request.getSelected() != null) {
            update.setSelected(Integer.valueOf(1).equals(request.getSelected()) ? 1 : 0);
        }
        cartItemMapper.updateById(update);
        return getCart(userId);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        requireUserCartItem(userId, itemId);
        cartItemMapper.deleteByIdAndUserId(itemId, userId);
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart != null) {
            return cart;
        }
        LocalDateTime now = LocalDateTime.now();
        Cart newCart = Cart.builder().userId(userId).createdAt(now).updatedAt(now).build();
        cartMapper.insert(newCart);
        return newCart;
    }

    private CartItem requireUserCartItem(Long userId, Long itemId) {
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车商品不存在");
        }
        return item;
    }

    private CartItemVO toResponse(CartItem item) {
        Product product = productMapper.selectById(item.getProductId());
        ProductSku sku = productSkuMapper.selectById(item.getSkuId());
        BigDecimal salePrice = sku == null || sku.getSalePrice() == null ? BigDecimal.ZERO : sku.getSalePrice();
        int stock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
        int lockedStock = sku == null || sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        return CartItemVO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .skuId(item.getSkuId())
                .productName(product == null ? null : product.getName())
                .skuName(sku == null ? null : sku.getName())
                .imageUrl(sku == null ? null : sku.getImageUrl())
                .specJson(sku == null ? null : sku.getSpecJson())
                .salePrice(salePrice)
                .quantity(item.getQuantity())
                .selected(item.getSelected())
                .availableStock(Math.max(0, stock - lockedStock))
                .totalAmount(salePrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private void ensureEnoughStock(ProductSku sku, Integer quantity) {
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        int lockedStock = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        if (stock - lockedStock < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不足");
        }
    }
}
