-- Seckill feature schema upgrade for MySQL 8.0+
-- This script adds seckill activity tables and extends orders with source fields.

USE mall;

-- =========================
-- 1. Extend orders
-- =========================

ALTER TABLE orders
  ADD COLUMN source_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Order source: 1 normal, 2 seckill' AFTER status,
  ADD COLUMN source_id BIGINT UNSIGNED NULL COMMENT 'Source business ID, e.g. seckill_orders.id' AFTER source_type,
  ADD KEY idx_orders_source (source_type, source_id);

-- =========================
-- 2. Seckill activities
-- =========================

CREATE TABLE IF NOT EXISTS seckill_activities (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Seckill activity ID',
  name VARCHAR(128) NOT NULL COMMENT 'Activity name',
  description VARCHAR(512) NULL COMMENT 'Activity description',
  starts_at DATETIME NOT NULL COMMENT 'Activity start time',
  ends_at DATETIME NOT NULL COMMENT 'Activity end time',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 draft, 2 published, 3 ongoing, 4 ended, 5 closed',
  warm_up_at DATETIME NULL COMMENT 'Redis warm-up time',
  created_by BIGINT UNSIGNED NULL COMMENT 'Admin creator ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_seckill_activities_status_time (status, starts_at, ends_at),
  KEY idx_seckill_activities_created_by (created_by),
  CONSTRAINT fk_seckill_activities_admin FOREIGN KEY (created_by) REFERENCES admin_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seckill activities';

-- =========================
-- 3. Seckill items
-- =========================

CREATE TABLE IF NOT EXISTS seckill_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Seckill item ID',
  activity_id BIGINT UNSIGNED NOT NULL COMMENT 'Seckill activity ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  sku_id BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  seckill_price DECIMAL(10,2) NOT NULL COMMENT 'Seckill price',
  seckill_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total seckill stock',
  available_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Database remaining seckill stock',
  limit_per_user INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Purchase limit per user',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seckill_items_activity_sku (activity_id, sku_id),
  KEY idx_seckill_items_activity_status_sort (activity_id, status, sort_order),
  KEY idx_seckill_items_product_id (product_id),
  KEY idx_seckill_items_sku_id (sku_id),
  CONSTRAINT fk_seckill_items_activity FOREIGN KEY (activity_id) REFERENCES seckill_activities (id),
  CONSTRAINT fk_seckill_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_seckill_items_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id),
  CONSTRAINT chk_seckill_items_price CHECK (seckill_price >= 0),
  CONSTRAINT chk_seckill_items_stock CHECK (available_stock <= seckill_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seckill activity items';

-- =========================
-- 4. Seckill orders
-- =========================

CREATE TABLE IF NOT EXISTS seckill_orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Seckill order relation ID',
  request_no VARCHAR(64) NOT NULL COMMENT 'Seckill request number',
  activity_id BIGINT UNSIGNED NOT NULL COMMENT 'Seckill activity ID',
  seckill_item_id BIGINT UNSIGNED NOT NULL COMMENT 'Seckill item ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  order_id BIGINT UNSIGNED NULL COMMENT 'Created mall order ID',
  order_no VARCHAR(64) NULL COMMENT 'Created mall order number',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 processing, 20 order_created, 30 failed, 40 canceled',
  failure_reason VARCHAR(512) NULL COMMENT 'Failure reason',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seckill_orders_request_no (request_no),
  UNIQUE KEY uk_seckill_orders_user_item (activity_id, seckill_item_id, user_id),
  KEY idx_seckill_orders_user_status (user_id, status),
  KEY idx_seckill_orders_order_id (order_id),
  KEY idx_seckill_orders_order_no (order_no),
  KEY idx_seckill_orders_status_created (status, created_at),
  CONSTRAINT fk_seckill_orders_activity FOREIGN KEY (activity_id) REFERENCES seckill_activities (id),
  CONSTRAINT fk_seckill_orders_item FOREIGN KEY (seckill_item_id) REFERENCES seckill_items (id),
  CONSTRAINT fk_seckill_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_seckill_orders_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seckill order relation and idempotency records';
