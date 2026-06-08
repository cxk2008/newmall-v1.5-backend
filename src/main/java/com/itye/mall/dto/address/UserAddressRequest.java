package com.itye.mall.dto.address;

import lombok.Data;

@Data
public class UserAddressRequest {
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String postalCode;
    private Integer isDefault;
}
