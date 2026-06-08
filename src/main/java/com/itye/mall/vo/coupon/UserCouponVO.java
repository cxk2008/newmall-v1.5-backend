package com.itye.mall.vo.coupon;

import com.itye.mall.entity.Coupon;
import com.itye.mall.entity.UserCoupon;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserCouponVO {
    private Long id;
    private Long couponId;
    private String name;
    private Integer type;
    private BigDecimal faceValue;
    private BigDecimal minOrderAmount;
    private Integer status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime claimedAt;
    private LocalDateTime usedAt;

    public static UserCouponVO from(UserCoupon userCoupon, Coupon coupon) {
        return UserCouponVO.builder()
                .id(userCoupon.getId())
                .couponId(userCoupon.getCouponId())
                .name(coupon == null ? null : coupon.getName())
                .type(coupon == null ? null : coupon.getType())
                .faceValue(coupon == null ? null : coupon.getFaceValue())
                .minOrderAmount(coupon == null ? null : coupon.getMinOrderAmount())
                .status(userCoupon.getStatus())
                .startsAt(coupon == null ? null : coupon.getStartsAt())
                .endsAt(coupon == null ? null : coupon.getEndsAt())
                .claimedAt(userCoupon.getClaimedAt())
                .usedAt(userCoupon.getUsedAt())
                .build();
    }
}
