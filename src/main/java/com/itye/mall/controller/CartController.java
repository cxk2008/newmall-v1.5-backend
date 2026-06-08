package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.cart.AddCartItemRequest;
import com.itye.mall.dto.cart.UpdateCartItemRequest;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.CartService;
import com.itye.mall.vo.cart.CartVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<CartVO> getCart(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartVO> addItem(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody AddCartItemRequest request) {
        return ApiResponse.ok(cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartVO> updateItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long id,
                                                @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.ok(cartService.updateItem(user.getId(), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        cartService.deleteItem(user.getId(), id);
        return ApiResponse.ok(null);
    }
}
