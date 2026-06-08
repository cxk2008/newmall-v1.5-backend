package com.itye.mall.service;

import com.itye.mall.dto.address.UserAddressRequest;
import com.itye.mall.entity.UserAddress;
import com.itye.mall.mapper.UserAddressMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressService {
    private final UserAddressMapper userAddressMapper;

    public UserAddressService(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    public List<UserAddress> list(Long userId) {
        return userAddressMapper.selectByUserId(userId);
    }

    public UserAddress detail(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !userId.equals(address.getUserId()) || address.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "收货地址不存在");
        }
        return address;
    }

    @Transactional
    public UserAddress create(Long userId, UserAddressRequest request) {
        validate(request);
        LocalDateTime now = LocalDateTime.now();
        boolean defaultAddress = Integer.valueOf(1).equals(request.getIsDefault()) || userAddressMapper.selectByUserId(userId).isEmpty();
        if (defaultAddress) {
            userAddressMapper.clearDefaultByUserId(userId);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .receiverName(request.getReceiverName().trim())
                .receiverPhone(request.getReceiverPhone().trim())
                .province(request.getProvince().trim())
                .city(request.getCity().trim())
                .district(request.getDistrict().trim())
                .detailAddress(request.getDetailAddress().trim())
                .postalCode(trimToNull(request.getPostalCode()))
                .isDefault(defaultAddress ? 1 : 0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userAddressMapper.insert(address);
        return address;
    }

    @Transactional
    public UserAddress update(Long userId, Long id, UserAddressRequest request) {
        validate(request);
        detail(userId, id);
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            userAddressMapper.clearDefaultByUserId(userId);
        }

        UserAddress update = UserAddress.builder()
                .id(id)
                .userId(userId)
                .receiverName(request.getReceiverName().trim())
                .receiverPhone(request.getReceiverPhone().trim())
                .province(request.getProvince().trim())
                .city(request.getCity().trim())
                .district(request.getDistrict().trim())
                .detailAddress(request.getDetailAddress().trim())
                .postalCode(trimToNull(request.getPostalCode()))
                .isDefault(Integer.valueOf(1).equals(request.getIsDefault()) ? 1 : 0)
                .updatedAt(LocalDateTime.now())
                .build();
        userAddressMapper.updateById(update);
        return detail(userId, id);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        detail(userId, id);
        userAddressMapper.softDeleteByIdAndUserId(id, userId);
    }

    @Transactional
    public UserAddress setDefault(Long userId, Long id) {
        detail(userId, id);
        userAddressMapper.clearDefaultByUserId(userId);
        userAddressMapper.updateById(UserAddress.builder().id(id).isDefault(1).updatedAt(LocalDateTime.now()).build());
        return detail(userId, id);
    }

    private void validate(UserAddressRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getReceiverName())
                || !StringUtils.hasText(request.getReceiverPhone())
                || !StringUtils.hasText(request.getProvince())
                || !StringUtils.hasText(request.getCity())
                || !StringUtils.hasText(request.getDistrict())
                || !StringUtils.hasText(request.getDetailAddress())) {
            throw new IllegalArgumentException("收货地址信息不完整");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
