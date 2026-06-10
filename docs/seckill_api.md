# 秒杀接口文档

本文档说明秒杀模块新增接口，供前端和后台管理端对接使用。所有接口统一返回 `ApiResponse`：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

非 `0` 表示失败，错误信息以 `message` 为准。

## 一、权限说明

### 公开接口

- `GET /api/seckill/activities/current`
- `GET /api/seckill/activities/{activityId}/items`
- `GET /api/seckill/items/{seckillItemId}`

### 登录用户接口

需要请求头：

```http
Authorization: Bearer <token>
```

- `POST /api/seckill/items/{seckillItemId}/orders`
- `GET /api/seckill/results/{requestNo}`

### 后台管理员接口

需要管理员 Token：

```http
Authorization: Bearer <admin-token>
```

- `/api/admin/seckill/**`

## 二、状态枚举

### 秒杀活动状态

- `1`：草稿
- `2`：已发布
- `3`：进行中
- `4`：已结束
- `5`：已关闭

### 秒杀商品状态

- `1`：启用
- `2`：禁用

### 秒杀请求结果状态

- `PROCESSING`：处理中
- `SUCCESS`：下单成功
- `FAILED`：下单失败

### 秒杀订单状态

- `10`：处理中
- `20`：下单成功
- `30`：下单失败
- `40`：已取消

## 三、前台秒杀接口

### 1. 查询当前秒杀活动

```http
GET /api/seckill/activities/current
```

说明：查询当前时间范围内的秒杀活动，包含启用的秒杀商品列表。没有当前活动时，`data` 为 `null`。

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "618 限时秒杀",
    "description": "限量抢购",
    "startsAt": "2026-06-18T10:00:00",
    "endsAt": "2026-06-18T12:00:00",
    "status": 2,
    "warmUpAt": "2026-06-18T09:55:00",
    "createdBy": 1,
    "createdAt": "2026-06-09T15:00:00",
    "items": [
      {
        "id": 1,
        "activityId": 1,
        "productId": 100,
        "skuId": 200,
        "productName": "示例商品",
        "skuName": "黑色 256G",
        "imageUrl": "https://example.com/sku.jpg",
        "specJson": "{\"color\":\"黑色\",\"storage\":\"256G\"}",
        "originalPrice": 2999.00,
        "seckillPrice": 1999.00,
        "seckillStock": 100,
        "availableStock": 100,
        "limitPerUser": 1,
        "sortOrder": 0,
        "status": 1,
        "createdAt": "2026-06-09T15:00:00"
      }
    ]
  }
}
```

### 2. 查询活动商品列表

```http
GET /api/seckill/activities/{activityId}/items
```

路径参数：

- `activityId`：秒杀活动 ID。

说明：查询指定活动下启用的秒杀商品列表。

响应 `data`：`SeckillItemVO[]`，字段同上。

### 3. 查询秒杀商品详情

```http
GET /api/seckill/items/{seckillItemId}
```

路径参数：

- `seckillItemId`：秒杀活动商品 ID。

响应 `data`：`SeckillItemVO`。

### 4. 提交秒杀请求

```http
POST /api/seckill/items/{seckillItemId}/orders
```

路径参数：

- `seckillItemId`：秒杀活动商品 ID。

请求体：无。

成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requestNo": "S1888848000000000000",
    "status": "PROCESSING"
  }
}
```

说明：

- 接口返回成功只表示用户抢到下单资格，并已进入异步下单流程。
- 前端拿到 `requestNo` 后应调用“查询秒杀结果”接口轮询。
- 建议前端轮询间隔为 500ms 到 1000ms，避免过高频率请求。

常见失败：

- 秒杀活动未开始
- 秒杀活动已结束
- 秒杀活动已关闭
- 秒杀商品不存在
- 秒杀库存不足
- 用户已参与过本次秒杀
- 秒杀请求发送失败

### 5. 查询秒杀结果

```http
GET /api/seckill/results/{requestNo}
```

路径参数：

- `requestNo`：提交秒杀请求返回的请求号。

成功响应示例，处理中：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requestNo": "S1888848000000000000",
    "status": "PROCESSING",
    "orderId": null,
    "orderNo": null,
    "failureReason": null
  }
}
```

成功响应示例，下单成功：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requestNo": "S1888848000000000000",
    "status": "SUCCESS",
    "orderId": 101,
    "orderNo": "O1888848000000000001",
    "failureReason": null
  }
}
```

成功响应示例，下单失败：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "requestNo": "S1888848000000000000",
    "status": "FAILED",
    "orderId": null,
    "orderNo": null,
    "failureReason": "请先添加收货地址"
  }
}
```

前端处理建议：

- `PROCESSING`：继续轮询。
- `SUCCESS`：跳转订单详情或支付页。
- `FAILED`：停止轮询并展示失败原因。

## 四、后台秒杀活动接口

### 1. 分页查询秒杀活动

```http
GET /api/admin/seckill/activities
```

查询参数：

- `status`：活动状态，可选。
- `keyword`：活动名称关键词，可选。
- `pageNum`：页码，可选，默认 `1`。
- `pageSize`：每页数量，可选，默认 `10`。

响应 `data`：

```json
{
  "total": 1,
  "pageNum": 1,
  "pageSize": 10,
  "records": [
    {
      "id": 1,
      "name": "618 限时秒杀",
      "description": "限量抢购",
      "startsAt": "2026-06-18T10:00:00",
      "endsAt": "2026-06-18T12:00:00",
      "status": 1,
      "warmUpAt": null,
      "createdBy": 1,
      "createdAt": "2026-06-09T15:00:00",
      "updatedAt": "2026-06-09T15:00:00",
      "deletedAt": null
    }
  ]
}
```

### 2. 查询秒杀活动详情

```http
GET /api/admin/seckill/activities/{id}
```

路径参数：

- `id`：活动 ID。

说明：返回活动详情和该活动下的秒杀商品列表。

响应 `data`：`SeckillActivityVO`。

### 3. 创建秒杀活动

```http
POST /api/admin/seckill/activities
```

请求体：

```json
{
  "name": "618 限时秒杀",
  "description": "限量抢购",
  "startsAt": "2026-06-18T10:00:00",
  "endsAt": "2026-06-18T12:00:00",
  "status": 1
}
```

字段说明：

- `name`：必填。
- `description`：可选。
- `startsAt`：必填，活动开始时间。
- `endsAt`：必填，活动结束时间，必须晚于 `startsAt`。
- `status`：可选，建议创建时传 `1` 草稿。不传默认草稿。

响应 `data`：`SeckillActivity`。

### 4. 修改秒杀活动

```http
PUT /api/admin/seckill/activities/{id}
```

说明：只有草稿状态活动可以修改。

请求体同“创建秒杀活动”。

响应 `data`：`SeckillActivity`。

### 5. 发布秒杀活动

```http
PUT /api/admin/seckill/activities/{id}/publish
```

说明：

- 只有草稿活动可以发布。
- 发布时会校验至少存在一个启用的秒杀商品。
- 发布时会锁定普通 SKU 库存。
- 发布时会将活动、商品和库存预热到 Redis。

响应 `data`：`SeckillActivity`。

### 6. 关闭秒杀活动

```http
PUT /api/admin/seckill/activities/{id}/close
```

说明：关闭后会清理活动相关 Redis Key。

响应 `data`：`SeckillActivity`。

### 7. 删除秒杀活动

```http
DELETE /api/admin/seckill/activities/{id}
```

说明：只有草稿活动可以删除。

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 五、后台秒杀商品接口

### 1. 查询活动商品列表

```http
GET /api/admin/seckill/activities/{activityId}/items
```

路径参数：

- `activityId`：活动 ID。

说明：返回该活动下全部未删除的秒杀商品，包括启用和禁用。

响应 `data`：`SeckillItemVO[]`。

### 2. 添加活动商品

```http
POST /api/admin/seckill/activities/{activityId}/items
```

说明：只有草稿活动可以添加商品。

请求体：

```json
{
  "skuId": 200,
  "seckillPrice": 1999.00,
  "seckillStock": 100,
  "limitPerUser": 1,
  "sortOrder": 0,
  "status": 1
}
```

字段说明：

- `skuId`：必填，SKU 必须存在、启用，且所属商品必须上架。
- `seckillPrice`：必填，不能小于 `0`。
- `seckillStock`：必填，必须大于 `0`。
- `limitPerUser`：可选，默认 `1`。
- `sortOrder`：可选，默认 `0`。
- `status`：可选，`1` 启用，`2` 禁用，默认 `1`。

响应 `data`：`SeckillItem`。

### 3. 修改活动商品

```http
PUT /api/admin/seckill/items/{id}
```

说明：只有草稿活动下的商品可以修改。

请求体：

```json
{
  "seckillPrice": 1899.00,
  "seckillStock": 80,
  "limitPerUser": 1,
  "sortOrder": 1,
  "status": 1
}
```

响应 `data`：`SeckillItem`。

### 4. 删除活动商品

```http
DELETE /api/admin/seckill/items/{id}
```

说明：只有草稿活动下的商品可以删除。

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 六、前端对接流程建议

### 前台用户流程

1. 进入秒杀页，调用 `GET /api/seckill/activities/current`。
2. 展示活动时间、商品、原价、秒杀价、剩余库存。
3. 用户点击抢购，调用 `POST /api/seckill/items/{seckillItemId}/orders`。
4. 成功拿到 `requestNo` 后，调用 `GET /api/seckill/results/{requestNo}` 轮询。
5. 结果为 `SUCCESS` 时跳转支付页或订单详情页。
6. 结果为 `FAILED` 时展示 `failureReason`。

### 后台运营流程

1. 创建活动：`POST /api/admin/seckill/activities`。
2. 添加商品：`POST /api/admin/seckill/activities/{activityId}/items`。
3. 确认活动和商品配置无误。
4. 发布活动：`PUT /api/admin/seckill/activities/{id}/publish`。
5. 活动结束或异常时关闭活动：`PUT /api/admin/seckill/activities/{id}/close`。

## 七、注意事项

1. 秒杀订单创建是异步的，提交接口成功不代表订单已经创建成功。
2. 当前实现默认使用用户地址列表中的第一条地址作为秒杀订单地址；用户没有地址时会下单失败。
3. 活动发布会锁定普通 SKU 库存，确保秒杀库存和普通下单库存隔离。
4. 同一用户对同一秒杀活动商品只能成功抢购一次。
5. 前端不要使用商品原价或前端传入价格生成订单金额，订单金额以后端秒杀价为准。
6. 活动发布后不允许修改活动和活动商品，如需调整请新建活动或关闭当前活动。
