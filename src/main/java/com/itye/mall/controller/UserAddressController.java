package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.dto.address.UserAddressRequest;
import com.itye.mall.entity.UserAddress;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.UserAddressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {
    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @GetMapping
    public ApiResponse<List<UserAddress>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(userAddressService.list(user.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAddress> detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(userAddressService.detail(user.getId(), id));
    }

    @PostMapping
    public ApiResponse<UserAddress> create(@AuthenticationPrincipal AuthenticatedUser user,
                                           @RequestBody UserAddressRequest request) {
        return ApiResponse.ok(userAddressService.create(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAddress> update(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long id,
                                           @RequestBody UserAddressRequest request) {
        return ApiResponse.ok(userAddressService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        userAddressService.delete(user.getId(), id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<UserAddress> setDefault(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return ApiResponse.ok(userAddressService.setDefault(user.getId(), id));
    }
}
