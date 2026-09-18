# newmall 商城后端

基于 Spring Boot 3 + MyBatis 的 B2C 商城后端服务，覆盖用户、商品、购物车、订单、支付、物流、营销优惠券、秒杀、评价与后台管理等模块。秒杀链路使用 Redis + Redisson + RocketMQ 实现高并发下的削峰与异步下单，商品检索接入 Elasticsearch。

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / 构建 | Java 21、Maven |
| 框架 | Spring Boot 3.5、Spring Web、Spring Security |
| 持久层 | MyBatis 3、MySQL、HikariCP |
| 认证鉴权 | JWT（jjwt 0.13）、BCrypt |
| 缓存 / 分布式锁 | Redis（Lettuce 连接池）、Redisson |
| 消息队列 | RocketMQ（异步下单、订单超时、ES 同步） |
| 搜索 | Elasticsearch Java API Client |
| 工具库 | Lombok、Hutool |

## 功能模块

- **用户与鉴权**：注册、登录、获取当前用户、收货地址管理；JWT 无状态鉴权，区分普通用户与管理员角色。
- **商品**：分类树、品牌、商品 SPU/SKU、商品图片与属性、上下架、关键词搜索（ES）。
- **购物车与收藏**：购物车增删改查、商品收藏。
- **订单**：下单、订单列表与详情、取消订单、订单状态日志；下单对商品/SKU/收货地址做快照，库存锁定与释放，超时未支付由 RocketMQ 延迟消息自动关闭。
- **支付与退款**：模拟支付、支付记录查询、退款申请与后台审核。
- **物流**：后台发货、物流查询、用户确认收货。
- **营销**：优惠券发放与领取、用户优惠券、Banner 运营位。
- **秒杀**：活动与活动商品管理、Redis 预热、库存原子扣减、限购校验、异步下单、抢购结果查询与失败补偿。
- **后台管理**：管理员登录、商品/分类/品牌/SKU/库存/订单/退款/优惠券/Banner/秒杀活动维护、操作日志与库存流水。

## 目录结构

```
src/main/java/com/itye/mall
├── common        # 通用返回体、分页、常量、雪花 ID、MyBatis 基类、工具
├── controller    # REST 接口层（前台接口 + Admin* 后台接口）
├── dto           # 请求参数对象
├── vo            # 响应视图对象
├── entity        # 数据库实体
├── mapper        # MyBatis Mapper 接口
├── service       # 业务逻辑层
├── security      # JWT 过滤器、鉴权配置
├── mq            # RocketMQ 生产者/消费者与消息体
├── es            # Elasticsearch 配置、文档、索引初始化与同步
└── exception     # 全局异常处理

src/main/resources
├── application.yml
└── mapper/*.xml  # MyBatis 映射文件

docs              # 数据库表说明、接口文档与开发计划
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+（或使用自带的 `mvnw`）
- MySQL 8.x
- Redis 6.x+
- RocketMQ 4.x/5.x（NameServer 默认 `localhost:9876`）
- Elasticsearch 8.x（可选，`mall.elasticsearch.enabled=false` 可关闭）

### 2. 初始化数据库

创建数据库 `mall`（`utf8mb4`），并依次执行：

```bash
mysql -uroot -p mall < docs/database_schema.sql
mysql -uroot -p mall < docs/seckill_schema.sql   # 秒杀模块升级脚本
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，按本机环境调整：

- `spring.datasource`：数据库地址、账号、密码
- `spring.data.redis` / `redisson`：Redis 地址
- `rocketmq.name-server`：RocketMQ 地址
- `mall.jwt.secret`：JWT 签名密钥，**上线前务必替换为不少于 32 位的随机字符串**
- `mall.elasticsearch.*`：ES 地址与账号密码，或直接将 `enabled` 设为 `false`

> 提示：示例配置中的密码仅用于本地开发，请勿将生产密钥提交到仓库，建议改用环境变量或配置中心注入。

### 4. 启动服务

```bash
./mvnw spring-boot:run
```

服务默认监听 `http://localhost:8080`。

## 接口约定

所有接口统一返回 `ApiResponse` 结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code = 0` 表示成功，非 `0` 表示失败，错误信息以 `message` 为准。
- 需要登录的接口通过请求头携带 Token：`Authorization: Bearer <token>`。
- 后台接口统一以 `/api/admin/**` 为前缀，需要管理员角色的 Token。

主要接口分组：

| 前缀 | 说明 |
| --- | --- |
| `/api/auth` | 用户注册、登录、当前用户 |
| `/api/products`、`/api/categories` | 商品搜索、详情、SKU、分类树 |
| `/api/cart`、`/api/favorites` | 购物车、收藏 |
| `/api/orders`、`/api/payments`、`/api/refunds` | 订单、支付、退款 |
| `/api/coupons`、`/api/user/coupons`、`/api/banners` | 优惠券、用户优惠券、Banner |
| `/api/seckill` | 秒杀活动查询、抢购、结果查询 |
| `/api/admin/**` | 后台管理接口 |

秒杀模块的详细接口说明见 [docs/seckill_api.md](docs/seckill_api.md)。

## 文档

- [docs/database_schema.md](docs/database_schema.md)：数据库表结构与业务关系说明
- [docs/controller_service_development_plan.md](docs/controller_service_development_plan.md)：Controller/Service 层开发计划
- [docs/seckill_api.md](docs/seckill_api.md)：秒杀接口文档
- [docs/seckill_development_plan.md](docs/seckill_development_plan.md)：秒杀功能设计说明

## 秒杀核心链路

1. 管理员创建活动与活动商品，发布时校验配置并将活动信息、库存预热到 Redis。
2. 用户抢购请求先在 Redis 中完成时间、库存、限购与重复请求校验，并原子扣减库存。
3. 扣减成功写入抢购结果并发送 RocketMQ 下单消息，接口快速返回 `requestNo`。
4. 消费者幂等地创建秒杀订单与订单明细，成功后更新抢购结果。
5. 下单失败时更新失败原因，并按规则回补 Redis 库存，保证最终一致。
