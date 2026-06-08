# 商城数据库表说明

本文档说明 `database_schema.sql` 中各张 MySQL 表的职责、核心字段含义和主要业务关系。当前数据库面向一个标准 B2C 商城，覆盖用户、商品、购物车、订单、支付、物流、营销、评价和后台运营等模块。

## 一、整体设计说明

数据库名为 `mall`，字符集使用 `utf8mb4`，适合存储中文、Emoji 和多语言内容。

通用约定：

- 主键统一使用 `BIGINT UNSIGNED AUTO_INCREMENT`。
- 金额字段统一使用 `DECIMAL(10,2)`，避免浮点数精度问题。
- 大多数业务表包含 `created_at`、`updated_at` 字段。
- 需要软删除的数据表包含 `deleted_at` 字段。
- 商品、订单、支付、退款等核心业务表都保留业务编号，例如 `spu_code`、`sku_code`、`order_no`、`payment_no`、`refund_no`。
- 商品详情、规格、回调报文、评论图片等半结构化内容使用 `JSON` 或 `TEXT` 类型存储。

## 二、用户与权限模块

### 1. `users` 前台用户表

存储商城前台用户的账号信息，是用户下单、收藏、购物车、评价等行为的基础表。

主要字段：

- `username`：用户名，唯一。
- `phone`：手机号，唯一，可用于登录。
- `email`：邮箱，唯一，可用于登录或通知。
- `password_hash`：加密后的密码，不存明文密码。
- `nickname`、`avatar_url`：用户展示信息。
- `status`：用户状态，`1` 正常，`2` 禁用。
- `last_login_at`：最近登录时间。

关联关系：

- 一个用户可以有多个收货地址。
- 一个用户有一个购物车。
- 一个用户可以产生多个订单、支付记录、优惠券、收藏和评价。

### 2. `user_addresses` 用户收货地址表

存储用户的收货地址。下单时会把地址信息快照保存到订单表，避免用户后续修改地址影响历史订单。

主要字段：

- `user_id`：所属用户。
- `receiver_name`：收货人姓名。
- `receiver_phone`：收货人手机号。
- `province`、`city`、`district`：省市区。
- `detail_address`：详细地址。
- `is_default`：是否默认地址，`1` 是，`0` 否。

### 3. `admin_users` 后台管理员表

存储商城后台管理人员账号，用于商品管理、订单处理、退款审核、运营配置等后台操作。

主要字段：

- `username`：后台登录账号，唯一。
- `password_hash`：加密后的密码。
- `real_name`：管理员真实姓名。
- `role`：角色编码，例如 `operator`、`admin`。
- `status`：状态，`1` 正常，`2` 禁用。

## 三、商品模块

### 4. `product_categories` 商品分类表

存储商品分类，支持多级分类结构。

主要字段：

- `parent_id`：父级分类 ID，顶级分类为空。
- `name`：分类名称。
- `icon_url`：分类图标。
- `banner_url`：分类页横幅图。
- `sort_order`：排序值。
- `level`：分类层级。
- `status`：状态，`1` 启用，`2` 禁用。

典型用途：

- 首页分类导航。
- 商品列表筛选。
- 后台分类管理。

### 5. `brands` 品牌表

存储商品品牌信息。

主要字段：

- `name`：品牌名称，唯一。
- `logo_url`：品牌 Logo。
- `description`：品牌介绍。
- `sort_order`：排序值。
- `status`：状态，`1` 启用，`2` 禁用。

### 6. `products` 商品 SPU 表

存储商品的通用信息，也就是 SPU。一个 SPU 下面可以有多个 SKU。

例如：“iPhone 16” 是一个 SPU，“iPhone 16 黑色 256G” 是一个 SKU。

主要字段：

- `category_id`：所属分类。
- `brand_id`：所属品牌。
- `spu_code`：商品 SPU 编码，唯一。
- `name`：商品名称。
- `subtitle`：副标题或卖点。
- `main_image_url`：商品主图。
- `detail_html`：商品详情 HTML。
- `unit`：销售单位，例如 `件`。
- `price_min`、`price_max`：SKU 价格区间，便于列表页展示。
- `sales_count`：销量。
- `view_count`：浏览量。
- `status`：商品状态，`1` 草稿，`2` 上架，`3` 下架。
- `published_at`：上架时间。

索引说明：

- `FULLTEXT KEY ft_products_name_subtitle` 用于商品名称和副标题的全文搜索。
- `category_id + status` 适合分类商品列表查询。
- `status + sort_order` 适合前台商品排序展示。

### 7. `product_skus` 商品 SKU 表

存储商品的具体销售规格、价格和库存。

主要字段：

- `product_id`：所属 SPU。
- `sku_code`：SKU 编码，唯一。
- `name`：SKU 名称。
- `image_url`：SKU 图片。
- `spec_json`：规格 JSON，例如颜色、尺码、容量。
- `sale_price`：销售价。
- `market_price`：市场价。
- `cost_price`：成本价。
- `weight_gram`：重量，单位克，可用于运费计算。
- `stock`：可售库存。
- `locked_stock`：锁定库存，通常用于未支付订单占库存。
- `low_stock_threshold`：低库存预警阈值。
- `status`：SKU 状态，`1` 启用，`2` 禁用。

### 8. `product_images` 商品图片表

存储商品轮播图、详情图或 SKU 图片。

主要字段：

- `product_id`：所属商品。
- `sku_id`：可选，表示该图片属于某个具体 SKU。
- `image_url`：图片地址。
- `sort_order`：排序值。

### 9. `product_attributes` 商品属性定义表

定义某个分类下商品可以填写的属性。

例如手机分类可以有：屏幕尺寸、CPU、内存、存储容量等。

主要字段：

- `category_id`：所属分类，可为空表示通用属性。
- `name`：属性名称。
- `input_type`：录入类型，`1` 文本，`2` 单选，`3` 多选。
- `options_json`：可选项 JSON。
- `is_required`：是否必填。
- `sort_order`：排序值。

### 10. `product_attribute_values` 商品属性值表

存储某个商品具体填写的属性值。

主要字段：

- `product_id`：商品 ID。
- `attribute_id`：属性定义 ID。
- `value`：属性值。

用途：

- 商品详情页参数展示。
- 商品筛选。
- 后台商品参数维护。

## 四、购物车与收藏模块

### 11. `carts` 购物车表

每个用户对应一个购物车。

主要字段：

- `user_id`：用户 ID，唯一。

### 12. `cart_items` 购物车明细表

存储购物车中的具体商品 SKU。

主要字段：

- `cart_id`：购物车 ID。
- `user_id`：用户 ID，冗余保存，便于按用户查询。
- `product_id`：商品 SPU ID。
- `sku_id`：商品 SKU ID。
- `quantity`：购买数量。
- `selected`：是否选中，`1` 选中，`0` 未选中。

约束说明：

- `cart_id + sku_id` 唯一，防止同一个购物车重复添加同一个 SKU。

### 13. `favorites` 收藏表

存储用户收藏的商品。

主要字段：

- `user_id`：用户 ID。
- `product_id`：商品 ID。

约束说明：

- `user_id + product_id` 唯一，防止重复收藏。

## 五、订单、支付与物流模块

### 14. `orders` 订单主表

存储订单的主信息，包括用户、收货地址快照、金额、状态和关键时间点。

主要字段：

- `order_no`：订单编号，唯一。
- `user_id`：下单用户。
- `status`：订单状态。
- `receiver_name`、`receiver_phone`、`receiver_province`、`receiver_city`、`receiver_district`、`receiver_address`：收货地址快照。
- `product_amount`：商品总金额。
- `freight_amount`：运费。
- `discount_amount`：优惠金额。
- `payable_amount`：应付金额。
- `paid_amount`：实付金额。
- `payment_method`：支付方式，`1` 支付宝，`2` 微信，`3` 银行卡，`4` 余额。
- `paid_at`：支付时间。
- `shipped_at`：发货时间。
- `completed_at`：完成时间。
- `closed_at`：关闭时间。

订单状态：

- `10`：待支付。
- `20`：已支付。
- `30`：已发货。
- `40`：已完成。
- `50`：已关闭。
- `60`：已退款。

### 15. `order_items` 订单明细表

存储订单中的每一个商品 SKU，并保存下单时的商品快照。

主要字段：

- `order_id`：订单 ID。
- `order_no`：订单编号。
- `user_id`：用户 ID。
- `product_id`：商品 SPU ID。
- `sku_id`：商品 SKU ID。
- `product_name`：下单时商品名称快照。
- `sku_name`：下单时 SKU 名称快照。
- `sku_spec_json`：下单时 SKU 规格快照。
- `image_url`：下单时商品图片快照。
- `unit_price`：下单单价。
- `quantity`：购买数量。
- `total_amount`：该行总金额。
- `refund_status`：退款状态，`0` 无退款，`1` 申请中，`2` 已退款，`3` 已拒绝。

设计原因：

订单明细保留商品快照，是为了避免商品改名、改价格、改图片后影响历史订单展示。

### 16. `order_status_logs` 订单状态日志表

记录订单状态流转过程。

主要字段：

- `order_id`：订单 ID。
- `old_status`：变更前状态。
- `new_status`：变更后状态。
- `operator_type`：操作人类型，`1` 用户，`2` 管理员，`3` 系统。
- `operator_id`：操作人 ID。
- `note`：备注。

典型用途：

- 查看订单时间线。
- 排查订单异常。
- 后台审计。

### 17. `payments` 支付记录表

存储订单支付流水。

主要字段：

- `payment_no`：内部支付单号，唯一。
- `order_id`：订单 ID。
- `order_no`：订单编号。
- `user_id`：用户 ID。
- `channel`：支付渠道，`1` 支付宝，`2` 微信，`3` 银行卡，`4` 余额。
- `amount`：支付金额。
- `status`：支付状态，`10` 待支付，`20` 成功，`30` 失败，`40` 关闭。
- `transaction_id`：第三方支付平台交易号。
- `callback_payload`：支付回调原始内容。

### 18. `shipments` 物流发货表

存储订单物流信息。

主要字段：

- `order_id`：订单 ID，唯一。
- `order_no`：订单编号。
- `logistics_company`：物流公司。
- `logistics_no`：物流单号。
- `status`：物流状态，`10` 待发货，`20` 已发货，`30` 已收货。
- `shipped_at`：发货时间。
- `received_at`：签收时间。

### 19. `refunds` 退款表

存储订单退款或订单明细退款申请。

主要字段：

- `refund_no`：退款单号，唯一。
- `order_id`：订单 ID。
- `order_item_id`：订单明细 ID，可为空。为空时可表示整单退款。
- `user_id`：申请用户。
- `amount`：退款金额。
- `reason`：退款原因。
- `status`：退款状态。
- `handled_by`：处理退款的管理员。
- `handled_at`：处理时间。
- `refunded_at`：实际退款完成时间。

退款状态：

- `10`：申请中。
- `20`：已同意。
- `30`：已拒绝。
- `40`：已退款。
- `50`：已关闭。

## 六、库存模块

### 20. `inventory_logs` 库存流水表

记录 SKU 库存变化，用于库存追踪和问题排查。

主要字段：

- `sku_id`：SKU ID。
- `change_type`：变更类型。
- `quantity_change`：库存变化数量，正数增加，负数减少。
- `stock_after`：变更后的可售库存。
- `locked_stock_after`：变更后的锁定库存。
- `biz_type`：业务类型，例如 `order`、`payment`、`refund`、`admin`。
- `biz_id`：业务编号，例如订单号或退款单号。
- `note`：备注。

库存变更类型：

- `1`：入库。
- `2`：出库。
- `3`：锁定库存。
- `4`：释放锁定库存。
- `5`：扣减库存。
- `6`：退回库存。

典型流程：

- 用户提交订单：增加 `locked_stock`。
- 用户支付成功：减少 `stock`，减少 `locked_stock`。
- 订单超时取消：减少 `locked_stock`。
- 退款退货完成：增加 `stock`。

## 七、营销模块

### 21. `coupons` 优惠券表

存储平台优惠券规则。

主要字段：

- `name`：优惠券名称。
- `type`：优惠类型，`1` 满减/立减，`2` 折扣。
- `face_value`：优惠金额或折扣值。
- `min_order_amount`：最低使用金额。
- `total_quantity`：发放总量，`0` 表示不限量。
- `claimed_quantity`：已领取数量。
- `used_quantity`：已使用数量。
- `per_user_limit`：每个用户可领取数量。
- `starts_at`、`ends_at`：有效期。
- `status`：状态，`1` 启用，`2` 禁用。

### 22. `user_coupons` 用户优惠券表

存储用户领取后的优惠券实例。

主要字段：

- `user_id`：用户 ID。
- `coupon_id`：优惠券 ID。
- `status`：用户优惠券状态，`10` 未使用，`20` 已使用，`30` 已过期。
- `order_id`：使用在哪个订单上。
- `claimed_at`：领取时间。
- `used_at`：使用时间。

### 23. `banners` Banner 表

存储首页或活动页 Banner 配置。

主要字段：

- `title`：Banner 标题。
- `image_url`：图片地址。
- `link_url`：点击跳转地址。
- `position`：展示位置，例如 `home`。
- `sort_order`：排序值。
- `status`：状态，`1` 启用，`2` 禁用。
- `starts_at`、`ends_at`：展示时间范围。

## 八、评价模块

### 24. `product_reviews` 商品评价表

存储用户对已购买商品的评价。

主要字段：

- `user_id`：评价用户。
- `order_id`：订单 ID。
- `order_item_id`：订单明细 ID，唯一，保证一个订单明细只评价一次。
- `product_id`：商品 SPU ID。
- `sku_id`：商品 SKU ID。
- `rating`：评分，1 到 5 分。
- `content`：评价内容。
- `images_json`：评价图片 JSON。
- `is_anonymous`：是否匿名。
- `status`：状态，`1` 展示，`2` 隐藏。
- `reply_content`：商家回复内容。
- `replied_at`：回复时间。

典型用途：

- 商品详情页展示评价。
- 后台评价审核。
- 商家回复用户评价。

## 九、后台运营日志模块

### 25. `operation_logs` 后台操作日志表

记录管理员在后台的关键操作。

主要字段：

- `admin_id`：管理员 ID。
- `action`：操作名称，例如 `product.create`、`order.ship`。
- `target_type`：操作对象类型，例如 `product`、`order`。
- `target_id`：操作对象 ID。
- `ip_address`：操作 IP。
- `user_agent`：浏览器或客户端信息。
- `detail_json`：操作详情 JSON。

典型用途：

- 审计后台操作。
- 排查误操作。
- 记录敏感动作，例如改价、退款、发货。

## 十、核心业务关系

### 商品关系

- `product_categories` 1 对多 `products`
- `brands` 1 对多 `products`
- `products` 1 对多 `product_skus`
- `products` 1 对多 `product_images`
- `product_categories` 1 对多 `product_attributes`
- `products` 1 对多 `product_attribute_values`

### 用户关系

- `users` 1 对多 `user_addresses`
- `users` 1 对 1 `carts`
- `carts` 1 对多 `cart_items`
- `users` 多对多 `products`，通过 `favorites` 表实现收藏关系

### 订单关系

- `users` 1 对多 `orders`
- `orders` 1 对多 `order_items`
- `orders` 1 对多 `order_status_logs`
- `orders` 1 对多 `payments`
- `orders` 1 对 1 `shipments`
- `orders` 1 对多 `refunds`
- `order_items` 1 对 0/1 `product_reviews`

### 营销关系

- `coupons` 1 对多 `user_coupons`
- `users` 1 对多 `user_coupons`
- `orders` 1 对 0/1 或 1 对多 `user_coupons`，取决于后续业务是否允许一单多券

## 十一、后续开发建议

1. 后端实体类可以直接按表拆分，但订单创建、支付回调、库存扣减建议放在事务服务中统一处理。
2. 订单号、支付单号、退款单号不要使用数据库自增 ID 直接暴露给前端，建议生成带日期和随机数的业务编号。
3. 库存扣减需要重点处理并发问题，建议使用 SQL 条件扣减或乐观锁策略。
4. 订单地址、商品名称、SKU 规格、商品价格必须保存快照，历史订单不能依赖商品当前数据。
5. 如果后期要支持多商户，可以在商品、订单、退款、结算等表中增加 `merchant_id`。
6. 如果后期要支持秒杀、拼团等活动，建议单独增加活动表，不要把所有营销逻辑塞进 `coupons`。

