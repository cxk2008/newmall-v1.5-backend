# 下单使用优惠券接口说明

本文档说明本次新增的“下单时使用优惠券抵扣”能力，供前端接入使用。

## 一、整体流程

1. 用户先领取优惠券。
2. 前端查询当前用户优惠券列表，筛选 `status = 10` 的未使用优惠券。
3. 用户在确认订单页选择一张可用优惠券。
4. 创建订单时在请求体中传入 `userCouponId`。
5. 后端校验优惠券可用性，计算抵扣金额，生成订单。
6. 订单创建成功后，该用户优惠券会被标记为已使用，并绑定到订单。
7. 如果用户取消待支付订单，或后台关闭待支付订单，该优惠券会自动退回为未使用状态。

## 二、创建订单接口

### 接口地址

```http
POST /api/orders
```

### 鉴权

需要登录用户 Token。

```http
Authorization: Bearer <token>
```

### 请求参数

```json
{
  "addressId": 1,
  "skuId": 10,
  "quantity": 2,
  "userCouponId": 5,
  "remark": "请尽快发货"
}
```

字段说明：

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `addressId` | number | 是 | 收货地址 ID |
| `skuId` | number | 否 | 直接购买的 SKU ID。不传时表示从购物车已选商品下单 |
| `quantity` | number | 否 | 直接购买数量。不传默认 `1` |
| `userCouponId` | number | 否 | 用户已领取优惠券 ID。不使用优惠券时不传或传 `null` |
| `remark` | string | 否 | 订单备注 |

### 返回示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 100,
    "orderNo": "O202606071650123451234",
    "status": 10,
    "productAmount": 199.00,
    "freightAmount": 0.00,
    "discountAmount": 20.00,
    "payableAmount": 179.00,
    "paidAmount": 0.00,
    "items": []
  }
}
```

金额字段说明：

| 字段 | 说明 |
| --- | --- |
| `productAmount` | 商品总金额 |
| `freightAmount` | 运费金额，目前为 `0` |
| `discountAmount` | 优惠券抵扣金额 |
| `payableAmount` | 应付金额，计算方式为 `productAmount + freightAmount - discountAmount` |
| `paidAmount` | 已支付金额，待支付订单创建后为 `0` |

## 三、优惠券可用规则

后端会在下单时校验以下规则：

| 规则 | 说明 |
| --- | --- |
| 用户归属 | `userCouponId` 必须属于当前登录用户 |
| 使用状态 | 用户优惠券状态必须是 `10`，即未使用 |
| 优惠券状态 | 优惠券必须启用，`status = 1` |
| 有效期 | 当前时间必须在 `startsAt` 和 `endsAt` 之间 |
| 使用门槛 | 订单商品总金额必须大于或等于 `minOrderAmount` |
| 抵扣上限 | 抵扣金额不会超过订单商品总金额 |

优惠券类型：

| `type` | 说明 | 计算方式 |
| --- | --- | --- |
| `1` | 满减券 | `discountAmount = faceValue` |
| `2` | 百分比券 | `discountAmount = productAmount * faceValue / 100` |

示例：

| 商品金额 | 优惠券类型 | `faceValue` | 抵扣金额 |
| --- | --- | --- | --- |
| `199.00` | 满减券 | `20.00` | `20.00` |
| `199.00` | 百分比券 | `10.00` | `19.90` |

## 四、用户优惠券状态

用户优惠券表状态：

| 状态 | 含义 |
| --- | --- |
| `10` | 未使用 |
| `20` | 已使用 |
| `30` | 已过期 |

订单创建成功后：

- 用户券状态从 `10` 变为 `20`。
- 用户券会写入本次订单的 `orderId`。
- 优惠券主表的 `usedQuantity` 会加 `1`。

待支付订单取消或后台关闭后：

- 用户券状态从 `20` 退回 `10`。
- 用户券的 `orderId` 和 `usedAt` 会清空。
- 优惠券主表的 `usedQuantity` 会减 `1`。

## 五、常见错误

接口失败时统一返回：

```json
{
  "code": 400,
  "message": "优惠券不可用或已被使用",
  "data": null
}
```

常见错误信息：

| HTTP 状态 | message | 说明 |
| --- | --- | --- |
| `400` | `优惠券不可用或已被使用` | 用户券不是未使用状态，或并发下单时已被占用 |
| `400` | `优惠券不在有效期内` | 优惠券已过期或未开始 |
| `400` | `订单金额未达到优惠券使用门槛` | 商品金额小于 `minOrderAmount` |
| `400` | `请选择收货地址` | 未传 `addressId` |
| `400` | `请选择要购买的商品` | 购物车下单时没有选中商品 |
| `404` | `用户优惠券不存在` | `userCouponId` 不存在或不属于当前用户 |

## 六、前端接入建议

确认订单页建议展示：

- 商品总金额：`productAmount`
- 优惠券抵扣：根据前端预估显示，最终以后端创建订单返回的 `discountAmount` 为准
- 应付金额：最终以后端返回的 `payableAmount` 为准

提交订单时：

- 不使用优惠券：不要传 `userCouponId`，或传 `null`
- 使用优惠券：传用户优惠券 ID，不是优惠券主表 ID

注意：`userCouponId` 是用户领取记录 ID，来自用户优惠券列表返回的 `id` 字段；不是 `couponId`。
