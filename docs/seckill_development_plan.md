# 秒杀功能升级改造计划书

本文档用于规划商城后端秒杀功能的升级改造方案。当前项目已经引入 Redis、Redisson 和 RocketMQ，后续秒杀功能应围绕“高并发入口削峰、库存原子扣减、异步下单、结果可查询、失败可补偿”的目标设计。

## 一、建设目标

秒杀模块需要支持运营人员创建秒杀活动，用户在活动时间内抢购指定 SKU，并在高并发场景下保证库存不超卖、用户不重复购买、订单最终可落库。

核心目标：

1. 支持后台创建、发布、关闭秒杀活动。
2. 支持秒杀商品活动价、活动库存、限购数量和活动时间配置。
3. 秒杀开始前将活动与库存预热到 Redis。
4. 用户请求秒杀时优先在 Redis 中完成资格校验和库存扣减。
5. 抢购成功后通过 RocketMQ 异步创建订单，降低接口响应耗时。
6. 用户可以查询秒杀请求处理结果。
7. 系统异常时可以通过数据库、Redis 和消息状态进行补偿修复。

## 二、设计原则

1. 秒杀请求不能直接打到 MySQL 扣库存。
2. Redis 承担秒杀入口层的库存计数、限购标记和请求结果缓存。
3. Redisson 用于活动发布、活动关闭、库存预热、补偿任务等低频管理动作的分布式锁。
4. RocketMQ 用于异步下单、削峰填谷和失败重试。
5. MySQL 仍然是最终一致的数据源，最终订单、库存流水和活动统计必须落库。
6. 秒杀订单创建必须幂等，同一个用户同一个活动同一个 SKU 只能成功一次。
7. 前端不能传价格，秒杀价必须以后端活动配置为准。
8. 秒杀成功只代表获得下单资格，不代表已支付。

## 三、推荐整体架构

秒杀核心链路建议如下：

1. 管理员创建秒杀活动和活动商品。
2. 管理员发布活动，系统校验活动配置并预热 Redis。
3. 用户进入秒杀页，查询活动和商品状态。
4. 用户点击抢购，接口在 Redis 中校验时间、库存、限购和重复请求。
5. Redis 原子扣减成功后写入用户抢购结果，并发送 RocketMQ 下单消息。
6. 消费者收到消息后创建秒杀订单和订单明细。
7. 创建订单成功后更新抢购结果为成功。
8. 创建订单失败时更新结果为失败，并按失败原因决定是否回补 Redis 库存。

## 四、数据表设计建议

建议在现有商品、订单和库存表之外新增秒杀专用表，避免把活动逻辑塞进通用商品表。

### 1. `seckill_activities` 秒杀活动表

职责：保存秒杀活动主信息。

建议字段：

- `id`：活动 ID。
- `name`：活动名称。
- `description`：活动说明。
- `starts_at`：开始时间。
- `ends_at`：结束时间。
- `status`：活动状态，`1` 草稿，`2` 已发布，`3` 进行中，`4` 已结束，`5` 已关闭。
- `warm_up_at`：预热时间，可选。
- `created_by`：创建管理员 ID。
- `created_at`、`updated_at`、`deleted_at`。

关键索引：

- `status + starts_at + ends_at`：用于查询当前可用活动。

### 2. `seckill_items` 秒杀活动商品表

职责：保存活动中的具体 SKU、秒杀价、活动库存和限购规则。

建议字段：

- `id`：活动商品 ID。
- `activity_id`：活动 ID。
- `product_id`：商品 SPU ID。
- `sku_id`：商品 SKU ID。
- `seckill_price`：秒杀价。
- `seckill_stock`：活动总库存。
- `available_stock`：数据库侧剩余活动库存。
- `limit_per_user`：每个用户限购数量，建议第一版固定为 1。
- `sort_order`：排序值。
- `status`：状态，`1` 启用，`2` 禁用。
- `created_at`、`updated_at`、`deleted_at`。

关键约束：

- `activity_id + sku_id` 唯一，防止同一活动重复配置同一 SKU。

### 3. `seckill_orders` 秒杀订单关系表

职责：记录用户秒杀成功资格与最终订单关系，提供幂等约束。

建议字段：

- `id`：主键。
- `request_no`：秒杀请求号，唯一。
- `activity_id`：活动 ID。
- `seckill_item_id`：活动商品 ID。
- `user_id`：用户 ID。
- `order_id`：商城订单 ID，可为空，异步创建成功后回填。
- `order_no`：商城订单编号，可为空。
- `status`：状态，`10` 处理中，`20` 下单成功，`30` 下单失败，`40` 已取消。
- `failure_reason`：失败原因。
- `created_at`、`updated_at`。

关键约束：

- `request_no` 唯一，保证消息消费幂等。
- `activity_id + seckill_item_id + user_id` 唯一，保证一人一单。

### 4. 现有订单表扩展建议

建议在 `orders` 表增加订单来源字段：

- `source_type`：订单来源，`1` 普通订单，`2` 秒杀订单。
- `source_id`：来源业务 ID，可存 `seckill_orders.id`。

如果第一阶段不想改动订单表，也可以先只在 `seckill_orders` 中保存关联关系。

## 五、Redis Key 设计

秒杀 Redis Key 需要保持命名统一，方便排查和清理。

### 1. 活动信息

- `seckill:activity:{activityId}`

存储活动基础信息，例如开始时间、结束时间、状态。

### 2. 活动商品信息

- `seckill:item:{seckillItemId}`

存储活动商品快照，例如 `activityId`、`skuId`、`productId`、`seckillPrice`、`limitPerUser`。

### 3. 秒杀库存

- `seckill:stock:{seckillItemId}`

存储活动剩余库存。用户抢购时通过 Lua 脚本原子判断和扣减。

### 4. 用户购买标记

- `seckill:user:{activityId}:{seckillItemId}:{userId}`

用户秒杀成功后写入，防止重复抢购。过期时间建议覆盖活动结束后一段时间。

### 5. 秒杀结果

- `seckill:result:{requestNo}`

存储请求处理结果：

- `PROCESSING`：处理中。
- `SUCCESS`：下单成功。
- `FAILED`：下单失败。

### 6. 活动商品集合

- `seckill:activity:items:{activityId}`

存储活动下的秒杀商品 ID 列表，便于活动页查询。

## 六、Redis 原子扣减方案

用户请求秒杀时建议使用 Lua 脚本一次性完成以下判断：

1. 活动是否已预热。
2. 当前库存是否大于 0。
3. 用户是否已经抢购过。
4. 扣减库存。
5. 写入用户购买标记。

返回码建议：

- `0`：抢购资格获取成功。
- `1`：库存不足。
- `2`：用户已抢购。
- `3`：活动未预热或活动商品不存在。

这样可以避免多个 Redis 命令之间的并发窗口。

## 七、RocketMQ 消息设计

### 1. Topic 与 Tag

建议：

- Topic：`mall_seckill_order`
- Tag：`create_order`

### 2. 消息体字段

建议定义 `SeckillOrderMessage`：

- `requestNo`：请求号。
- `activityId`：活动 ID。
- `seckillItemId`：活动商品 ID。
- `productId`：商品 ID。
- `skuId`：SKU ID。
- `userId`：用户 ID。
- `quantity`：购买数量，第一版建议固定为 1。
- `seckillPrice`：秒杀价。
- `createdAt`：请求时间。

### 3. 生产者职责

秒杀接口在 Redis 扣减成功后：

1. 生成 `requestNo`。
2. 写入 `seckill:result:{requestNo}` 为 `PROCESSING`。
3. 发送 RocketMQ 下单消息。
4. 返回 `requestNo` 给前端。

### 4. 消费者职责

消费者收到消息后：

1. 根据 `requestNo` 查询 `seckill_orders`，已存在则直接返回成功，保证幂等。
2. 插入 `seckill_orders` 处理中记录。
3. 校验活动和活动商品状态。
4. 使用数据库条件扣减 `seckill_items.available_stock`。
5. 创建商城订单和订单明细。
6. 回填 `seckill_orders.order_id` 和 `order_no`。
7. 更新 Redis 结果为 `SUCCESS`。

如果消费失败：

- 可重试异常：抛出异常交给 RocketMQ 重试。
- 不可重试异常：记录失败原因，更新结果为 `FAILED`，必要时回补 Redis 库存。

## 八、接口设计建议

### 1. 前台接口

Controller：

- `SeckillController`

接口：

- `GET /api/seckill/activities/current`：查询当前秒杀活动。
- `GET /api/seckill/activities/{activityId}/items`：查询活动商品列表。
- `GET /api/seckill/items/{seckillItemId}`：查询秒杀商品详情。
- `POST /api/seckill/items/{seckillItemId}/orders`：提交秒杀请求。
- `GET /api/seckill/results/{requestNo}`：查询秒杀结果。

权限：

- 查询活动和商品可以公开。
- 提交秒杀和查询个人结果必须登录。

### 2. 后台接口

Controller：

- `AdminSeckillActivityController`
- `AdminSeckillItemController`

接口：

- `GET /api/admin/seckill/activities`：分页查询活动。
- `GET /api/admin/seckill/activities/{id}`：活动详情。
- `POST /api/admin/seckill/activities`：创建活动。
- `PUT /api/admin/seckill/activities/{id}`：修改活动。
- `PUT /api/admin/seckill/activities/{id}/publish`：发布并预热活动。
- `PUT /api/admin/seckill/activities/{id}/close`：关闭活动。
- `POST /api/admin/seckill/activities/{activityId}/items`：添加活动商品。
- `PUT /api/admin/seckill/items/{id}`：修改活动商品。
- `DELETE /api/admin/seckill/items/{id}`：删除活动商品。

权限：

- 全部要求管理员登录。

## 九、服务拆分建议

建议新增以下 Service：

- `SeckillActivityService`：活动管理、活动校验、状态流转。
- `SeckillItemService`：活动商品管理。
- `SeckillWarmUpService`：活动预热和 Redis 清理。
- `SeckillService`：用户秒杀入口逻辑。
- `SeckillOrderService`：秒杀订单创建和幂等处理。
- `SeckillResultService`：结果写入与查询。
- `SeckillMessageProducer`：RocketMQ 消息发送。
- `SeckillOrderConsumer`：RocketMQ 消息消费。

其中用户入口 `SeckillService` 应尽量轻，只做 Redis 校验扣减和发消息，不直接创建订单。

## 十、与现有订单库存模块的关系

当前普通订单流程是：

1. 创建订单时锁定 SKU 库存。
2. 支付成功时扣减 SKU 库存。
3. 取消订单时释放锁定库存。

秒杀订单建议第一版采用独立活动库存：

1. 发布活动时从普通 SKU 可售库存中冻结一部分到 `seckill_items.seckill_stock`。
2. 用户抢购时扣 Redis 秒杀库存。
3. 消费者创建订单时扣 `seckill_items.available_stock`。
4. 秒杀订单支付成功后再扣普通 SKU 库存，或在活动发布时先锁定普通 SKU 库存。

推荐第一版更稳妥的方案：

1. 活动发布时调用现有 `InventoryService.lockStock` 锁定普通 SKU 库存。
2. 秒杀成功创建待支付订单，但不要重复锁普通 SKU 库存。
3. 秒杀支付成功时从已锁定库存中扣减。
4. 秒杀订单取消或超时未支付时释放对应锁定库存，并按规则回补活动库存。

这样可以复用现有库存模型，避免普通订单和秒杀订单争抢同一份可售库存。

## 十一、状态流转设计

### 1. 活动状态

- `1` 草稿：可编辑，不对用户展示。
- `2` 已发布：已通过校验，可预热。
- `3` 进行中：当前时间在活动区间内。
- `4` 已结束：活动自然结束。
- `5` 已关闭：管理员手动关闭。

### 2. 秒杀订单状态

- `10` 处理中：用户已抢到资格，正在异步创建订单。
- `20` 下单成功：商城订单创建成功。
- `30` 下单失败：异步下单失败。
- `40` 已取消：订单超时或用户取消。

### 3. 秒杀结果状态

- `PROCESSING`：处理中。
- `SUCCESS`：成功，返回订单 ID 和订单号。
- `FAILED`：失败，返回失败原因。

## 十二、开发批次建议

### 第 1 批：基础模型与管理能力

目标：管理员可以配置秒杀活动。

任务：

1. 新增秒杀活动表和活动商品表。
2. 新增 Entity、Mapper XML、DTO、VO。
3. 新增后台活动 CRUD。
4. 新增后台活动商品 CRUD。
5. 增加活动发布校验。

完成标准：

- 后台可以创建活动。
- 后台可以给活动添加 SKU。
- 活动商品不能重复。
- 秒杀价、库存、时间范围都有基础校验。

### 第 2 批：Redis 预热与活动查询

目标：活动发布后可以进入 Redis 秒杀缓存。

任务：

1. 配置 Redis 和 Redisson。
2. 实现 `SeckillWarmUpService`。
3. 活动发布时预热活动信息、商品信息和库存。
4. 活动关闭时清理或标记 Redis Key。
5. 实现前台活动和商品查询接口。

完成标准：

- 发布活动后 Redis 中能看到活动和库存。
- 前台可以查询当前活动和秒杀商品。

### 第 3 批：秒杀入口与 Redis 原子扣减

目标：用户可以抢购并拿到请求号。

任务：

1. 编写 Redis Lua 扣减脚本。
2. 实现 `POST /api/seckill/items/{seckillItemId}/orders`。
3. 实现用户重复抢购校验。
4. 实现请求结果初始化。
5. 秒杀成功后发送 RocketMQ 消息。

完成标准：

- 库存不足时立即失败。
- 同一用户重复抢购会失败。
- Redis 库存不会扣成负数。
- 接口快速返回 `requestNo`。

### 第 4 批：MQ 异步下单

目标：消费者可以稳定创建秒杀订单。

任务：

1. 定义 `SeckillOrderMessage`。
2. 实现 RocketMQ 生产者。
3. 实现 RocketMQ 消费者。
4. 消费端创建 `seckill_orders` 幂等记录。
5. 复用或扩展现有 `OrderService` 创建秒杀订单。
6. 更新秒杀结果为成功或失败。

完成标准：

- 消息重复消费不会重复创建订单。
- 秒杀订单可以在用户订单列表中查看。
- 秒杀订单金额使用活动价。

### 第 5 批：取消、超时和补偿

目标：异常场景可恢复。

任务：

1. 秒杀待支付订单超时关闭。
2. 关闭订单时释放普通库存锁定。
3. 根据业务规则决定是否回补秒杀库存。
4. 增加补偿任务，扫描长时间 `PROCESSING` 的秒杀记录。
5. 增加 Redis 与数据库库存差异检查工具。

完成标准：

- 消息发送失败、消费失败、订单创建失败都有明确状态。
- 长时间处理中请求不会永久卡住。
- 库存异常有可执行的修复路径。

### 第 6 批：压测与稳定性

目标：验证高并发下不超卖、不重复下单。

任务：

1. 增加 Service 层并发测试。
2. 增加 Redis Lua 脚本单元测试。
3. 增加 RocketMQ 消费幂等测试。
4. 使用压测工具验证秒杀入口。
5. 观察 Redis、RocketMQ、MySQL 慢 SQL 和接口耗时。

完成标准：

- 高并发请求下活动库存不超卖。
- 同一用户不会重复下单。
- 秒杀入口平均响应耗时稳定。

## 十三、配置建议

建议在 `application.yml` 中补充：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

rocketmq:
  name-server: localhost:9876
  producer:
    group: mall-seckill-producer

mall:
  seckill:
    result-expire-seconds: 1800
    user-mark-expire-seconds: 86400
    stock-key-expire-seconds: 86400
```

生产环境中 Redis、RocketMQ、JWT 密钥和数据库连接都应使用环境变量或外部配置。

## 十四、核心风险点

1. Redis 扣减成功但 MQ 发送失败：需要回补 Redis 库存和用户标记，或记录失败请求用于补偿。
2. MQ 消息重复消费：必须依赖 `request_no` 唯一约束和消费端幂等。
3. MySQL 创建订单失败：需要更新秒杀结果，必要时回补库存。
4. 活动发布重复预热：必须使用 Redisson 分布式锁，避免库存重复写入。
5. 秒杀库存和普通 SKU 库存关系：必须明确是发布时锁定普通库存，还是支付时扣普通库存。
6. 用户重复请求：Redis 标记和数据库唯一约束要双保险。
7. 前端轮询结果过于频繁：结果查询接口需要限流或短期缓存。
8. 订单超时关闭：需要与现有普通订单取消逻辑区分，避免错误回补库存。
9. RocketMQ 版本兼容：当前 `rocketmq-spring-boot-starter` 版本较老，后续升级 Spring Boot 时要单独验证兼容性。

## 十五、建议优先落地方案

第一版建议先做“单活动、单 SKU 限购 1 件、模拟支付”的最小闭环：

1. 后台创建活动和活动商品。
2. 发布活动时锁定普通 SKU 库存并预热 Redis。
3. 用户秒杀时 Redis 原子扣减。
4. RocketMQ 异步创建待支付订单。
5. 用户查询秒杀结果。
6. 用户使用现有模拟支付完成支付。
7. 支付成功后复用现有库存扣减和订单状态流转。

完成这个闭环后，再扩展多场次、多 SKU、多限购、真实支付、库存补偿和压测优化。
