package com.itye.mall.service;

import com.itye.mall.common.response.PageResult;
import com.itye.mall.common.util.PageUtils;
import com.itye.mall.dto.admin.AdminCouponRequest;
import com.itye.mall.entity.Coupon;
import com.itye.mall.entity.UserCoupon;
import com.itye.mall.mapper.CouponMapper;
import com.itye.mall.mapper.UserCouponMapper;
import com.itye.mall.vo.coupon.UserCouponVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {
    private static final int COUPON_ENABLED = 1;
    private static final int COUPON_DISABLED = 2;
    private static final int USER_COUPON_UNUSED = 10;
    private static final int COUPON_TYPE_AMOUNT_OFF = 1;
    private static final int COUPON_TYPE_PERCENT_OFF = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final OperationLogService operationLogService;

    public CouponService(CouponMapper couponMapper,
                         UserCouponMapper userCouponMapper,
                         OperationLogService operationLogService) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.operationLogService = operationLogService;
    }

    public List<Coupon> available() {
        return couponMapper.selectAvailable();
    }

    public List<UserCouponVO> userCoupons(Long userId, Integer status) {
        return userCouponMapper.selectByUserId(userId, status).stream()
                .map(userCoupon -> UserCouponVO.from(userCoupon, couponMapper.selectById(userCoupon.getCouponId())))
                .toList();
    }

    @Transactional
    public UserCouponVO claim(Long userId, Long couponId) {
        Coupon coupon = requireCoupon(couponId);
        validateClaimable(coupon, userId);
        int updated = couponMapper.increaseClaimedQuantity(couponId);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券已领完或不可领取");
        }
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(USER_COUPON_UNUSED)
                .claimedAt(LocalDateTime.now())
                .build();
        userCouponMapper.insert(userCoupon);
        return UserCouponVO.from(userCoupon, couponMapper.selectById(couponId));
    }

    public CouponUsage calculateUsage(Long userId, Long userCouponId, BigDecimal orderAmount) {
        if (userCouponId == null) {
            return CouponUsage.none();
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额必须大于 0");
        }
        UserCoupon userCoupon = requireUsableUserCoupon(userId, userCouponId);
        Coupon coupon = requireCoupon(userCoupon.getCouponId());
        validateUsable(coupon, orderAmount);
        return new CouponUsage(userCoupon.getId(), coupon.getId(), calculateDiscount(coupon, orderAmount));
    }

    public void markUsed(Long userId, CouponUsage usage, Long orderId) {
        if (usage == null || !usage.used()) {
            return;
        }
        int updated = userCouponMapper.markUsed(usage.userCouponId(), userId, orderId);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不可用或已被使用");
        }
        couponMapper.increaseUsedQuantity(usage.couponId());
    }

    public void releaseByOrder(Long orderId) {
        List<UserCoupon> usedCoupons = userCouponMapper.selectByOrderId(orderId);
        int updated = userCouponMapper.releaseByOrderId(orderId);
        if (updated < 1) {
            return;
        }
        for (UserCoupon userCoupon : usedCoupons) {
            if (Integer.valueOf(20).equals(userCoupon.getStatus())) {
                couponMapper.decreaseUsedQuantity(userCoupon.getCouponId());
            }
        }
    }

    public PageResult<Coupon> adminList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = PageUtils.normalizePageNum(pageNum);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return PageResult.<Coupon>builder()
                .total(couponMapper.countAdmin(status, keyword))
                .pageNum(normalizedPageNum)
                .pageSize(normalizedPageSize)
                .records(couponMapper.selectAdminPage(
                        status,
                        keyword,
                        PageUtils.offset(normalizedPageNum, normalizedPageSize),
                        normalizedPageSize
                ))
                .build();
    }

    public Coupon detail(Long id) {
        return requireCoupon(id);
    }

    @Transactional
    public Coupon adminCreate(Long adminId, AdminCouponRequest request) {
        validateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.builder()
                .name(request.getName().trim())
                .type(defaultInt(request.getType(), 1))
                .faceValue(request.getFaceValue())
                .minOrderAmount(defaultAmount(request.getMinOrderAmount()))
                .totalQuantity(defaultInt(request.getTotalQuantity(), 0))
                .claimedQuantity(0)
                .usedQuantity(0)
                .perUserLimit(defaultInt(request.getPerUserLimit(), 1))
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status(defaultStatus(request.getStatus()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        couponMapper.insert(coupon);
        operationLogService.write(adminId, "coupon.create", "coupon", coupon.getId(), "{\"name\":\"" + coupon.getName() + "\"}");
        return requireCoupon(coupon.getId());
    }

    @Transactional
    public Coupon adminUpdate(Long adminId, Long id, AdminCouponRequest request) {
        requireCoupon(id);
        validateRequest(request);
        Coupon update = Coupon.builder()
                .id(id)
                .name(request.getName().trim())
                .type(defaultInt(request.getType(), 1))
                .faceValue(request.getFaceValue())
                .minOrderAmount(defaultAmount(request.getMinOrderAmount()))
                .totalQuantity(defaultInt(request.getTotalQuantity(), 0))
                .perUserLimit(defaultInt(request.getPerUserLimit(), 1))
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status(defaultStatus(request.getStatus()))
                .updatedAt(LocalDateTime.now())
                .build();
        couponMapper.updateById(update);
        operationLogService.write(adminId, "coupon.update", "coupon", id, "{\"name\":\"" + update.getName() + "\"}");
        return requireCoupon(id);
    }

    @Transactional
    public void adminDelete(Long adminId, Long id) {
        requireCoupon(id);
        couponMapper.deleteById(id);
        operationLogService.write(adminId, "coupon.delete", "coupon", id, null);
    }

    private Coupon requireCoupon(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "优惠券不存在");
        }
        return coupon;
    }

    private UserCoupon requireUsableUserCoupon(Long userId, Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userId.equals(userCoupon.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户优惠券不存在");
        }
        if (!Integer.valueOf(USER_COUPON_UNUSED).equals(userCoupon.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不可用或已被使用");
        }
        return userCoupon;
    }

    private void validateClaimable(Coupon coupon, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(COUPON_ENABLED).equals(coupon.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不可领取");
        }
        if (coupon.getStartsAt() == null || coupon.getEndsAt() == null || now.isBefore(coupon.getStartsAt()) || now.isAfter(coupon.getEndsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不在领取时间内");
        }
        if (coupon.getTotalQuantity() != null && coupon.getTotalQuantity() > 0
                && coupon.getClaimedQuantity() != null
                && coupon.getClaimedQuantity() >= coupon.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券已领完");
        }
        long claimed = userCouponMapper.countByUserIdAndCouponId(userId, coupon.getId());
        int limit = coupon.getPerUserLimit() == null ? 1 : coupon.getPerUserLimit();
        if (claimed >= limit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已达到领取上限");
        }
    }

    private void validateUsable(Coupon coupon, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(COUPON_ENABLED).equals(coupon.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不可用");
        }
        if (coupon.getStartsAt() == null || coupon.getEndsAt() == null || now.isBefore(coupon.getStartsAt()) || now.isAfter(coupon.getEndsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "优惠券不在有效期内");
        }
        BigDecimal minOrderAmount = defaultAmount(coupon.getMinOrderAmount());
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单金额未达到优惠券使用门槛");
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        if (Integer.valueOf(COUPON_TYPE_PERCENT_OFF).equals(coupon.getType())) {
            BigDecimal percent = defaultAmount(coupon.getFaceValue()).min(ONE_HUNDRED).divide(ONE_HUNDRED);
            discount = orderAmount.multiply(percent);
        } else {
            discount = defaultAmount(coupon.getFaceValue());
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return discount.min(orderAmount);
    }

    private void validateRequest(AdminCouponRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("优惠券名称不能为空");
        }
        if (request.getFaceValue() == null || request.getFaceValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("优惠券面值不能为负数");
        }
        if (request.getMinOrderAmount() != null && request.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("最低消费金额不能为负数");
        }
        if (request.getStartsAt() == null || request.getEndsAt() == null || !request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new IllegalArgumentException("优惠券有效期不正确");
        }
        if (request.getType() != null && request.getType() != 1 && request.getType() != 2) {
            throw new IllegalArgumentException("优惠券类型只能是 1 或 2");
        }
        if (Integer.valueOf(COUPON_TYPE_PERCENT_OFF).equals(request.getType())
                && request.getFaceValue().compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("百分比优惠券面值不能超过 100");
        }
        if (request.getStatus() != null && request.getStatus() != COUPON_ENABLED && request.getStatus() != COUPON_DISABLED) {
            throw new IllegalArgumentException("优惠券状态只能是 1 或 2");
        }
        if (request.getTotalQuantity() != null && request.getTotalQuantity() < 0) {
            throw new IllegalArgumentException("优惠券总量不能为负数");
        }
        if (request.getPerUserLimit() != null && request.getPerUserLimit() < 1) {
            throw new IllegalArgumentException("用户领取上限不能小于 1");
        }
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? COUPON_ENABLED : status;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record CouponUsage(Long userCouponId, Long couponId, BigDecimal discountAmount) {
        private static CouponUsage none() {
            return new CouponUsage(null, null, BigDecimal.ZERO);
        }

        public boolean used() {
            return userCouponId != null;
        }
    }
}
