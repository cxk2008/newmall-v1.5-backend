# Controller 与 Service 层开发计划书

本文档用于规划商城后端 Controller 与 Service 层的开发顺序。原则是先完成用户能真实完成购物的核心链路，再补充运营、营销、评价和后台管理等增强功能。

## 一、开发目标

后端接口最终需要支撑一个完整商城网站，包括前台用户浏览商品、加入购物车、提交订单、支付后发货、确认收货、评价商品，以及后台管理员维护商品、订单、库存和营销内容。

当前项目已经具备：

- MySQL 表结构。
- Entity 实体类。
- MyBatis Mapper 接口和 XML。
- 用户注册、登录、JWT 鉴权。

后续重点是补齐业务 Service 和 REST Controller。

## 二、开发原则

1. 先完成主要功能，再完成次要功能。
2. 先打通前台用户购物闭环，再开发后台管理能力。
3. 先实现简单可靠的业务规则，再逐步增强营销、统计、审核等复杂能力。
4. 所有写操作都必须放在 Service 层处理，Controller 只负责参数接收、鉴权用户获取和响应返回。
5. 订单、库存、支付、退款相关逻辑必须使用事务。
6. 价格、商品名称、SKU 规格、收货地址等订单关键数据必须保存快照。

## 三、功能优先级总览

### 第一优先级：核心购物闭环

这一阶段目标是让用户可以完整完成“注册登录 -> 浏览商品 -> 加购物车 -> 下单 -> 查看订单”的主流程。

必须完成：

- 用户资料与收货地址。
- 商品分类、商品列表、商品详情。
- 购物车。
- 订单创建、订单列表、订单详情、取消订单。
- 库存锁定与释放。

暂不强依赖真实支付，可以先做模拟支付接口，让订单状态能从待支付流转到已支付。

### 第二优先级：支付、物流、评价

这一阶段目标是让订单生命周期更完整。

必须完成：

- 模拟支付或支付回调入口。
- 发货与物流查询。
- 确认收货。
- 商品评价。
- 订单状态日志。

### 第三优先级：后台管理

这一阶段目标是让管理员可以维护商城基础数据。

必须完成：

- 后台管理员登录或复用当前 Security 方案扩展管理员角色。
- 商品分类管理。
- 品牌管理。
- 商品 SPU/SKU 管理。
- 商品上下架。
- 库存调整。
- 订单管理。
- 发货管理。

### 第四优先级：营销与运营

这一阶段目标是提升商城运营能力。

可以后置完成：

- 优惠券。
- 用户优惠券。
- Banner 管理。
- 收藏。
- 商品浏览量、销量统计。
- 后台操作日志。

### 第五优先级：增强能力

这一阶段属于体验和稳定性增强。

可以最后完成：

- 商品搜索优化。
- 复杂筛选。
- 退款售后。
- 权限细分。
- 接口限流。
- 操作审计。
- 数据统计报表。

## 四、第一阶段：核心购物闭环

### 1. 用户地址模块

目标：用户可以维护自己的收货地址，下单时选择地址。

Service：

- `UserAddressService`

Controller：

- `UserAddressController`

接口建议：

- `GET /api/user/addresses`：查询当前用户地址列表。
- `GET /api/user/addresses/{id}`：查询地址详情。
- `POST /api/user/addresses`：新增地址。
- `PUT /api/user/addresses/{id}`：修改地址。
- `DELETE /api/user/addresses/{id}`：删除地址。
- `PUT /api/user/addresses/{id}/default`：设置默认地址。

关键规则：

- 只能操作当前登录用户自己的地址。
- 设置默认地址时，需要取消该用户其他默认地址。
- 删除地址建议优先软删除。

### 2. 商品浏览模块

目标：前台用户可以浏览分类、商品列表和商品详情。

Service：

- `ProductCategoryService`
- `ProductService`

Controller：

- `ProductCategoryController`
- `ProductController`

接口建议：

- `GET /api/categories/tree`：查询分类树。
- `GET /api/products`：分页查询商品列表。
- `GET /api/products/{id}`：查询商品详情。
- `GET /api/products/{id}/skus`：查询商品 SKU。
- `GET /api/products/search`：按关键词搜索商品。

关键规则：

- 前台只展示启用分类。
- 前台只展示已上架商品。
- 商品详情需要包含 SPU、SKU、图片、属性。
- 查询商品详情时可以增加浏览量。

### 3. 购物车模块

目标：用户可以添加商品到购物车、修改数量、选择结算商品。

Service：

- `CartService`

Controller：

- `CartController`

接口建议：

- `GET /api/cart`：查询购物车。
- `POST /api/cart/items`：加入购物车。
- `PUT /api/cart/items/{id}`：修改数量或选中状态。
- `DELETE /api/cart/items/{id}`：删除购物车项。
- `DELETE /api/cart/items`：批量删除购物车项。
- `PUT /api/cart/items/selected`：批量修改选中状态。

关键规则：

- 同一个用户同一个 SKU 只保留一条购物车记录。
- 加入购物车时需要校验商品和 SKU 是否上架、库存是否足够。
- 修改数量时不能超过可售库存。
- 购物车展示价格以当前 SKU 价格为准。

### 4. 订单创建模块

目标：用户可以从购物车或立即购买生成订单。

Service：

- `OrderService`
- `InventoryService`

Controller：

- `OrderController`

接口建议：

- `POST /api/orders`：创建订单。
- `GET /api/orders`：查询当前用户订单列表。
- `GET /api/orders/{id}`：查询订单详情。
- `PUT /api/orders/{id}/cancel`：取消待支付订单。

关键规则：

- 创建订单必须校验收货地址归属。
- 创建订单必须校验商品状态、SKU 状态、库存。
- 创建订单时保存商品快照和地址快照。
- 创建订单时锁定库存。
- 取消订单时释放锁定库存。
- 创建订单、订单明细、库存锁定、库存日志必须在同一个事务中完成。

### 5. 模拟支付模块

目标：先不接真实第三方支付，也能让订单从待支付进入已支付状态。

Service：

- `PaymentService`

Controller：

- `PaymentController`

接口建议：

- `POST /api/payments/mock-pay/{orderId}`：模拟支付。
- `GET /api/payments/order/{orderId}`：查询订单支付记录。

关键规则：

- 只能支付当前用户自己的订单。
- 只有待支付订单可以支付。
- 支付成功后订单状态改为已支付。
- 支付成功后扣减真实库存并释放锁定库存。
- 支付记录、订单状态、库存扣减、订单日志必须在同一个事务中完成。

## 五、第二阶段：订单生命周期完善

### 1. 物流模块

目标：后台可以发货，用户可以查看物流信息。

Service：

- `ShipmentService`

Controller：

- `ShipmentController`

接口建议：

- `GET /api/orders/{orderId}/shipment`：用户查看物流。
- `POST /api/admin/orders/{orderId}/ship`：后台发货。

关键规则：

- 只有已支付订单可以发货。
- 发货后订单状态改为已发货。
- 发货时写入物流记录和订单状态日志。

### 2. 确认收货模块

目标：用户可以确认收到商品。

接口建议：

- `PUT /api/orders/{id}/receive`：确认收货。

关键规则：

- 只有已发货订单可以确认收货。
- 确认收货后订单状态改为已完成。
- 写入订单状态日志。

### 3. 评价模块

目标：用户可以对已完成订单中的商品进行评价。

Service：

- `ProductReviewService`

Controller：

- `ProductReviewController`

接口建议：

- `POST /api/reviews`：新增评价。
- `GET /api/products/{productId}/reviews`：查询商品评价。
- `GET /api/orders/{orderId}/reviews`：查询订单评价状态。

关键规则：

- 只能评价自己的已完成订单。
- 一个订单明细只能评价一次。
- 评分范围为 1 到 5。

## 六、第三阶段：后台管理

### 1. 后台商品基础管理

目标：管理员可以维护分类、品牌、商品、SKU。

Service：

- `AdminCategoryService`
- `AdminBrandService`
- `AdminProductService`
- `AdminSkuService`

Controller：

- `AdminCategoryController`
- `AdminBrandController`
- `AdminProductController`
- `AdminSkuController`

接口建议：

- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `POST /api/admin/brands`
- `PUT /api/admin/brands/{id}`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `PUT /api/admin/products/{id}/on-sale`
- `PUT /api/admin/products/{id}/off-sale`
- `POST /api/admin/products/{productId}/skus`
- `PUT /api/admin/skus/{id}`

关键规则：

- 商品上架前必须至少有一个启用 SKU。
- SKU 价格和库存不能为负数。
- 商品下架不影响历史订单。

### 2. 后台订单管理

目标：管理员可以查看订单、发货、关闭异常订单。

Service：

- `AdminOrderService`

Controller：

- `AdminOrderController`

接口建议：

- `GET /api/admin/orders`
- `GET /api/admin/orders/{id}`
- `PUT /api/admin/orders/{id}/close`
- `POST /api/admin/orders/{id}/ship`

关键规则：

- 管理员关闭订单需要记录原因。
- 所有状态变更都要写入订单状态日志。

### 3. 库存管理

目标：管理员可以调整 SKU 库存。

Service：

- `InventoryService`

Controller：

- `AdminInventoryController`

接口建议：

- `GET /api/admin/skus/{skuId}/inventory-logs`
- `POST /api/admin/skus/{skuId}/stock/increase`
- `POST /api/admin/skus/{skuId}/stock/decrease`

关键规则：

- 库存调整必须写库存流水。
- 减少库存不能导致库存小于 0。

## 七、第四阶段：营销与运营

### 1. 优惠券模块

目标：后台创建优惠券，用户领取并在下单时使用。

Service：

- `CouponService`

Controller：

- `CouponController`
- `AdminCouponController`

接口建议：

- `GET /api/coupons/available`
- `POST /api/coupons/{id}/claim`
- `GET /api/user/coupons`
- `POST /api/admin/coupons`
- `PUT /api/admin/coupons/{id}`

关键规则：

- 领取时校验时间、状态、库存、用户领取上限。
- 使用时校验有效期、最低消费金额和使用状态。

### 2. Banner 模块

目标：首页可配置运营 Banner。

Service：

- `BannerService`

Controller：

- `BannerController`
- `AdminBannerController`

接口建议：

- `GET /api/banners?position=home`
- `POST /api/admin/banners`
- `PUT /api/admin/banners/{id}`
- `DELETE /api/admin/banners/{id}`

### 3. 收藏模块

目标：用户可以收藏商品。

Service：

- `FavoriteService`

Controller：

- `FavoriteController`

接口建议：

- `GET /api/favorites`
- `POST /api/favorites/{productId}`
- `DELETE /api/favorites/{productId}`

## 八、第五阶段：售后与增强

### 1. 退款模块

目标：用户可以申请退款，管理员可以审核。

Service：

- `RefundService`

Controller：

- `RefundController`
- `AdminRefundController`

接口建议：

- `POST /api/refunds`
- `GET /api/refunds`
- `GET /api/refunds/{id}`
- `PUT /api/admin/refunds/{id}/approve`
- `PUT /api/admin/refunds/{id}/reject`

关键规则：

- 退款金额不能超过可退金额。
- 审核通过后需要更新订单明细退款状态。
- 退款完成后需要按业务情况退回库存。

### 2. 后台操作日志

目标：记录管理员关键操作。

Service：

- `OperationLogService`

关键规则：

- 商品改价、上下架、库存调整、订单关闭、发货、退款审核都需要记录日志。

### 3. 查询与分页规范

目标：统一列表接口返回结构。

建议新增：

- `PageRequest`
- `PageResult<T>`

接口规则：

- `pageNum` 默认 1。
- `pageSize` 默认 10。
- 最大 `pageSize` 建议限制为 100。
- 列表接口统一返回 `total`、`pageNum`、`pageSize`、`records`。

## 九、建议开发顺序

### 第 1 批：前台基础资料与商品

1. 用户地址模块。
2. 分类查询。
3. 商品列表。
4. 商品详情。

完成标准：

- 登录用户可以维护地址。
- 未登录用户可以浏览商品。
- 商品详情能展示 SKU、图片和属性。

### 第 2 批：购物车与订单

1. 购物车增删改查。
2. 创建订单。
3. 查询订单。
4. 取消订单。
5. 库存锁定与释放。

完成标准：

- 用户可以从购物车创建订单。
- 订单明细中保存商品快照。
- 库存不会被超卖。

### 第 3 批：支付与订单状态

1. 模拟支付。
2. 支付记录。
3. 库存扣减。
4. 订单状态日志。

完成标准：

- 订单可以从待支付变为已支付。
- 支付后库存正确扣减。
- 状态变化可追踪。

### 第 4 批：物流与评价

1. 后台发货。
2. 用户查看物流。
3. 用户确认收货。
4. 用户评价商品。

完成标准：

- 订单生命周期可以走到已完成。
- 已完成订单可以评价。

### 第 5 批：后台商品管理

1. 分类管理。
2. 品牌管理。
3. 商品 SPU 管理。
4. SKU 管理。
5. 上下架。
6. 库存调整。

完成标准：

- 管理员可以维护商品基础数据。
- 前台商品展示来自后台维护结果。

### 第 6 批：营销和售后

1. Banner。
2. 收藏。
3. 优惠券。
4. 退款。
5. 后台操作日志。

完成标准：

- 商城具备基础运营能力。
- 用户可以收藏、领券、申请退款。
- 后台关键操作可追踪。

## 十、接口权限规划

### 公开接口

- 注册。
- 登录。
- 分类查询。
- 商品列表。
- 商品详情。
- 商品评价列表。
- Banner 查询。

### 登录用户接口

- 当前用户信息。
- 地址管理。
- 购物车。
- 创建订单。
- 我的订单。
- 支付。
- 确认收货。
- 评价。
- 收藏。
- 领取优惠券。
- 我的优惠券。
- 申请退款。

### 管理员接口

- 分类管理。
- 品牌管理。
- 商品管理。
- SKU 管理。
- 订单管理。
- 发货。
- 库存调整。
- 优惠券管理。
- Banner 管理。
- 退款审核。
- 操作日志查询。

## 十一、风险点与注意事项

1. 库存并发是核心风险，创建订单和支付扣库存必须重点测试。
2. 订单金额必须以后端计算为准，不能相信前端传入的价格。
3. 订单快照必须完整保存，否则商品修改后会影响历史订单。
4. 删除商品、SKU、分类时要考虑历史订单引用，优先使用状态禁用或软删除。
5. JWT 密钥当前是开发配置，生产环境必须改成环境变量或外部配置。
6. 后台管理员权限建议单独设计角色，不要长期和普通用户混用。
7. 支付回调将来接第三方时必须做签名验证和幂等处理。
8. 所有分页查询都需要限制最大 `pageSize`，避免一次查太多数据。

## 十二、下一步建议

建议下一步先实现第一批功能中的用户地址模块和商品查询模块。它们风险低、依赖少，可以快速建立 Controller、Service、DTO、分页响应等通用代码风格，为后续购物车和订单模块打基础。

