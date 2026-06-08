-- Mall database schema for MySQL 8.0+
-- Charset: utf8mb4
-- Money fields use DECIMAL. Soft delete fields use deleted_at where needed.

CREATE DATABASE IF NOT EXISTS mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE mall;

-- =========================
-- 1. User and auth
-- =========================

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  username VARCHAR(64) NOT NULL COMMENT 'Login/display username',
  phone VARCHAR(32) NULL COMMENT 'Phone number',
  email VARCHAR(128) NULL COMMENT 'Email address',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Password hash',
  nickname VARCHAR(64) NULL COMMENT 'Nickname',
  avatar_url VARCHAR(512) NULL COMMENT 'Avatar URL',
  gender TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 unknown, 1 male, 2 female',
  birthday DATE NULL COMMENT 'Birthday',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 active, 2 disabled',
  last_login_at DATETIME NULL COMMENT 'Last login time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_phone (phone),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Frontend users';

CREATE TABLE user_addresses (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Address ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  receiver_name VARCHAR(64) NOT NULL COMMENT 'Receiver name',
  receiver_phone VARCHAR(32) NOT NULL COMMENT 'Receiver phone',
  province VARCHAR(64) NOT NULL COMMENT 'Province',
  city VARCHAR(64) NOT NULL COMMENT 'City',
  district VARCHAR(64) NOT NULL COMMENT 'District/county',
  detail_address VARCHAR(255) NOT NULL COMMENT 'Street detail',
  postal_code VARCHAR(16) NULL COMMENT 'Postal code',
  is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 default, 0 normal',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_user_addresses_user_id (user_id),
  CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User shipping addresses';

CREATE TABLE admin_users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Admin ID',
  username VARCHAR(64) NOT NULL COMMENT 'Admin username',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Password hash',
  real_name VARCHAR(64) NULL COMMENT 'Real name',
  phone VARCHAR(32) NULL COMMENT 'Phone number',
  email VARCHAR(128) NULL COMMENT 'Email address',
  role VARCHAR(64) NOT NULL DEFAULT 'operator' COMMENT 'Role code',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 active, 2 disabled',
  last_login_at DATETIME NULL COMMENT 'Last login time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_users_username (username),
  KEY idx_admin_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backend admin users';

-- =========================
-- 2. Catalog
-- =========================

CREATE TABLE product_categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  parent_id BIGINT UNSIGNED NULL COMMENT 'Parent category ID',
  name VARCHAR(128) NOT NULL COMMENT 'Category name',
  icon_url VARCHAR(512) NULL COMMENT 'Icon URL',
  banner_url VARCHAR(512) NULL COMMENT 'Banner URL',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  level TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Category level',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_product_categories_parent_id (parent_id),
  KEY idx_product_categories_status_sort (status, sort_order),
  CONSTRAINT fk_product_categories_parent FOREIGN KEY (parent_id) REFERENCES product_categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product categories';

CREATE TABLE brands (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Brand ID',
  name VARCHAR(128) NOT NULL COMMENT 'Brand name',
  logo_url VARCHAR(512) NULL COMMENT 'Logo URL',
  description VARCHAR(512) NULL COMMENT 'Brand description',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_brands_name (name),
  KEY idx_brands_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Brands';

CREATE TABLE products (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Product ID',
  category_id BIGINT UNSIGNED NOT NULL COMMENT 'Category ID',
  brand_id BIGINT UNSIGNED NULL COMMENT 'Brand ID',
  spu_code VARCHAR(64) NOT NULL COMMENT 'SPU code',
  name VARCHAR(255) NOT NULL COMMENT 'Product name',
  subtitle VARCHAR(255) NULL COMMENT 'Subtitle/selling point',
  main_image_url VARCHAR(512) NULL COMMENT 'Main image URL',
  detail_html MEDIUMTEXT NULL COMMENT 'Product detail HTML',
  unit VARCHAR(32) NOT NULL DEFAULT '件' COMMENT 'Sales unit',
  price_min DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Minimum sale price',
  price_max DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Maximum sale price',
  sales_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sales count',
  view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'View count',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 draft, 2 on_sale, 3 off_sale',
  published_at DATETIME NULL COMMENT 'Published time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_products_spu_code (spu_code),
  KEY idx_products_category_status (category_id, status),
  KEY idx_products_brand_id (brand_id),
  KEY idx_products_status_sort (status, sort_order),
  FULLTEXT KEY ft_products_name_subtitle (name, subtitle),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories (id),
  CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product SPU';

CREATE TABLE product_skus (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  sku_code VARCHAR(64) NOT NULL COMMENT 'SKU code',
  name VARCHAR(255) NOT NULL COMMENT 'SKU name',
  image_url VARCHAR(512) NULL COMMENT 'SKU image URL',
  spec_json JSON NULL COMMENT 'Specification JSON, e.g. color/size',
  sale_price DECIMAL(10,2) NOT NULL COMMENT 'Sale price',
  market_price DECIMAL(10,2) NULL COMMENT 'Market price',
  cost_price DECIMAL(10,2) NULL COMMENT 'Cost price',
  weight_gram INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Weight in grams',
  stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Available stock',
  locked_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Stock locked by unpaid orders',
  low_stock_threshold INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Low stock threshold',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_skus_sku_code (sku_code),
  KEY idx_product_skus_product_id (product_id),
  KEY idx_product_skus_status_stock (status, stock),
  CONSTRAINT fk_product_skus_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product SKU';

CREATE TABLE product_images (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Image ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  sku_id BIGINT UNSIGNED NULL COMMENT 'Optional SKU ID',
  image_url VARCHAR(512) NOT NULL COMMENT 'Image URL',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_images_product_id (product_id),
  KEY idx_product_images_sku_id (sku_id),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_product_images_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product images';

CREATE TABLE product_attributes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Attribute ID',
  category_id BIGINT UNSIGNED NULL COMMENT 'Category ID',
  name VARCHAR(128) NOT NULL COMMENT 'Attribute name',
  input_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 text, 2 select, 3 multi_select',
  options_json JSON NULL COMMENT 'Available options',
  is_required TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 required, 0 optional',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_attributes_category_id (category_id),
  CONSTRAINT fk_product_attributes_category FOREIGN KEY (category_id) REFERENCES product_categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product attributes';

CREATE TABLE product_attribute_values (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Attribute value ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  attribute_id BIGINT UNSIGNED NOT NULL COMMENT 'Attribute ID',
  value VARCHAR(255) NOT NULL COMMENT 'Attribute value',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_attribute_values (product_id, attribute_id),
  KEY idx_product_attribute_values_attribute_id (attribute_id),
  CONSTRAINT fk_product_attribute_values_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_product_attribute_values_attribute FOREIGN KEY (attribute_id) REFERENCES product_attributes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product attribute values';

-- =========================
-- 3. Cart and favorites
-- =========================

CREATE TABLE carts (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Cart ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_carts_user_id (user_id),
  CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shopping carts';

CREATE TABLE cart_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Cart item ID',
  cart_id BIGINT UNSIGNED NOT NULL COMMENT 'Cart ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  sku_id BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  quantity INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Quantity',
  selected TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 selected, 0 unselected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cart_items_cart_sku (cart_id, sku_id),
  KEY idx_cart_items_user_id (user_id),
  KEY idx_cart_items_product_id (product_id),
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
  CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_cart_items_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shopping cart items';

CREATE TABLE favorites (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Favorite ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorites_user_product (user_id, product_id),
  KEY idx_favorites_product_id (product_id),
  CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product favorites';

-- =========================
-- 4. Orders, payment, shipment
-- =========================

CREATE TABLE orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Order ID',
  order_no VARCHAR(64) NOT NULL COMMENT 'Order number',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 pending_pay, 20 paid, 30 shipped, 40 completed, 50 closed, 60 refunded',
  receiver_name VARCHAR(64) NOT NULL COMMENT 'Receiver name snapshot',
  receiver_phone VARCHAR(32) NOT NULL COMMENT 'Receiver phone snapshot',
  receiver_province VARCHAR(64) NOT NULL COMMENT 'Receiver province snapshot',
  receiver_city VARCHAR(64) NOT NULL COMMENT 'Receiver city snapshot',
  receiver_district VARCHAR(64) NOT NULL COMMENT 'Receiver district snapshot',
  receiver_address VARCHAR(255) NOT NULL COMMENT 'Receiver detail address snapshot',
  product_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Goods total amount',
  freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Freight amount',
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Discount amount',
  payable_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount user should pay',
  paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Actual paid amount',
  payment_method TINYINT UNSIGNED NULL COMMENT '1 alipay, 2 wechat, 3 card, 4 balance',
  remark VARCHAR(512) NULL COMMENT 'User remark',
  paid_at DATETIME NULL COMMENT 'Payment time',
  shipped_at DATETIME NULL COMMENT 'Shipment time',
  completed_at DATETIME NULL COMMENT 'Completion time',
  closed_at DATETIME NULL COMMENT 'Closed time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_orders_order_no (order_no),
  KEY idx_orders_user_status (user_id, status),
  KEY idx_orders_status_created (status, created_at),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Orders';

CREATE TABLE order_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Order item ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  order_no VARCHAR(64) NOT NULL COMMENT 'Order number',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID snapshot relation',
  sku_id BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID snapshot relation',
  product_name VARCHAR(255) NOT NULL COMMENT 'Product name snapshot',
  sku_name VARCHAR(255) NOT NULL COMMENT 'SKU name snapshot',
  sku_spec_json JSON NULL COMMENT 'SKU spec snapshot',
  image_url VARCHAR(512) NULL COMMENT 'Image URL snapshot',
  unit_price DECIMAL(10,2) NOT NULL COMMENT 'Unit price snapshot',
  quantity INT UNSIGNED NOT NULL COMMENT 'Quantity',
  total_amount DECIMAL(10,2) NOT NULL COMMENT 'Line total amount',
  refund_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 none, 1 applying, 2 refunded, 3 rejected',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_items_order_id (order_id),
  KEY idx_order_items_user_id (user_id),
  KEY idx_order_items_product_id (product_id),
  KEY idx_order_items_sku_id (sku_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_order_items_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_order_items_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order items';

CREATE TABLE order_status_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  old_status TINYINT UNSIGNED NULL COMMENT 'Old status',
  new_status TINYINT UNSIGNED NOT NULL COMMENT 'New status',
  operator_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 user, 2 admin, 3 system',
  operator_id BIGINT UNSIGNED NULL COMMENT 'Operator ID',
  note VARCHAR(512) NULL COMMENT 'Operation note',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_status_logs_order_id (order_id),
  CONSTRAINT fk_order_status_logs_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order status logs';

CREATE TABLE payments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Payment ID',
  payment_no VARCHAR(64) NOT NULL COMMENT 'Internal payment number',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  order_no VARCHAR(64) NOT NULL COMMENT 'Order number',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  channel TINYINT UNSIGNED NOT NULL COMMENT '1 alipay, 2 wechat, 3 card, 4 balance',
  amount DECIMAL(10,2) NOT NULL COMMENT 'Payment amount',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 pending, 20 success, 30 failed, 40 closed',
  transaction_id VARCHAR(128) NULL COMMENT 'Third-party transaction ID',
  paid_at DATETIME NULL COMMENT 'Paid time',
  callback_payload JSON NULL COMMENT 'Payment callback payload',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payments_payment_no (payment_no),
  KEY idx_payments_order_id (order_id),
  KEY idx_payments_user_id (user_id),
  KEY idx_payments_transaction_id (transaction_id),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Payment records';

CREATE TABLE shipments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Shipment ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  order_no VARCHAR(64) NOT NULL COMMENT 'Order number',
  logistics_company VARCHAR(128) NULL COMMENT 'Logistics company',
  logistics_no VARCHAR(128) NULL COMMENT 'Tracking number',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 pending, 20 shipped, 30 received',
  shipped_at DATETIME NULL COMMENT 'Shipped time',
  received_at DATETIME NULL COMMENT 'Received time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_shipments_order_id (order_id),
  KEY idx_shipments_logistics_no (logistics_no),
  CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shipment records';

CREATE TABLE refunds (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Refund ID',
  refund_no VARCHAR(64) NOT NULL COMMENT 'Refund number',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  order_item_id BIGINT UNSIGNED NULL COMMENT 'Optional order item ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  amount DECIMAL(10,2) NOT NULL COMMENT 'Refund amount',
  reason VARCHAR(512) NULL COMMENT 'Refund reason',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 applying, 20 approved, 30 rejected, 40 refunded, 50 closed',
  handled_by BIGINT UNSIGNED NULL COMMENT 'Admin handler ID',
  handled_at DATETIME NULL COMMENT 'Handled time',
  refunded_at DATETIME NULL COMMENT 'Refund completed time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refunds_refund_no (refund_no),
  KEY idx_refunds_order_id (order_id),
  KEY idx_refunds_user_status (user_id, status),
  CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_refunds_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
  CONSTRAINT fk_refunds_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_refunds_admin FOREIGN KEY (handled_by) REFERENCES admin_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refund records';

-- =========================
-- 5. Inventory
-- =========================

CREATE TABLE inventory_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Inventory log ID',
  sku_id BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  change_type TINYINT UNSIGNED NOT NULL COMMENT '1 in, 2 out, 3 lock, 4 unlock, 5 deduct, 6 return',
  quantity_change INT NOT NULL COMMENT 'Quantity change, positive or negative',
  stock_after INT UNSIGNED NOT NULL COMMENT 'Stock after change',
  locked_stock_after INT UNSIGNED NOT NULL COMMENT 'Locked stock after change',
  biz_type VARCHAR(32) NOT NULL COMMENT 'Business type, e.g. order/payment/refund/admin',
  biz_id VARCHAR(64) NULL COMMENT 'Business ID or number',
  note VARCHAR(512) NULL COMMENT 'Note',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_inventory_logs_sku_id (sku_id),
  KEY idx_inventory_logs_biz (biz_type, biz_id),
  CONSTRAINT fk_inventory_logs_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Inventory change logs';

-- =========================
-- 6. Marketing
-- =========================

CREATE TABLE coupons (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Coupon ID',
  name VARCHAR(128) NOT NULL COMMENT 'Coupon name',
  type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 amount_off, 2 percent_off',
  face_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount off or percentage value',
  min_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Minimum order amount',
  total_quantity INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total quantity, 0 unlimited',
  claimed_quantity INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Claimed quantity',
  used_quantity INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Used quantity',
  per_user_limit INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Claim limit per user',
  starts_at DATETIME NOT NULL COMMENT 'Valid start time',
  ends_at DATETIME NOT NULL COMMENT 'Valid end time',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_coupons_status_time (status, starts_at, ends_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Coupons';

CREATE TABLE user_coupons (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'User coupon ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  coupon_id BIGINT UNSIGNED NOT NULL COMMENT 'Coupon ID',
  status TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '10 unused, 20 used, 30 expired',
  order_id BIGINT UNSIGNED NULL COMMENT 'Used order ID',
  claimed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  used_at DATETIME NULL COMMENT 'Used time',
  PRIMARY KEY (id),
  KEY idx_user_coupons_user_status (user_id, status),
  KEY idx_user_coupons_coupon_id (coupon_id),
  KEY idx_user_coupons_order_id (order_id),
  CONSTRAINT fk_user_coupons_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_coupons_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id),
  CONSTRAINT fk_user_coupons_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Coupons claimed by users';

CREATE TABLE banners (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Banner ID',
  title VARCHAR(128) NOT NULL COMMENT 'Banner title',
  image_url VARCHAR(512) NOT NULL COMMENT 'Banner image URL',
  link_url VARCHAR(512) NULL COMMENT 'Click link URL',
  position VARCHAR(64) NOT NULL DEFAULT 'home' COMMENT 'Display position',
  sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 enabled, 2 disabled',
  starts_at DATETIME NULL COMMENT 'Display start time',
  ends_at DATETIME NULL COMMENT 'Display end time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_banners_position_status_sort (position, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Homepage and activity banners';

-- =========================
-- 7. Reviews
-- =========================

CREATE TABLE product_reviews (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Review ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  order_item_id BIGINT UNSIGNED NOT NULL COMMENT 'Order item ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT 'Product ID',
  sku_id BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  rating TINYINT UNSIGNED NOT NULL COMMENT '1-5 rating',
  content VARCHAR(1000) NULL COMMENT 'Review content',
  images_json JSON NULL COMMENT 'Review images',
  is_anonymous TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 anonymous, 0 normal',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1 visible, 2 hidden',
  reply_content VARCHAR(1000) NULL COMMENT 'Merchant reply',
  replied_at DATETIME NULL COMMENT 'Reply time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_reviews_order_item (order_item_id),
  KEY idx_product_reviews_product_status (product_id, status),
  KEY idx_product_reviews_user_id (user_id),
  CONSTRAINT fk_product_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_product_reviews_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_product_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
  CONSTRAINT fk_product_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_product_reviews_sku FOREIGN KEY (sku_id) REFERENCES product_skus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product reviews';

-- =========================
-- 8. Operation logs
-- =========================

CREATE TABLE operation_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Operation log ID',
  admin_id BIGINT UNSIGNED NULL COMMENT 'Admin ID',
  action VARCHAR(128) NOT NULL COMMENT 'Action name',
  target_type VARCHAR(64) NULL COMMENT 'Target type',
  target_id VARCHAR(64) NULL COMMENT 'Target ID',
  ip_address VARCHAR(64) NULL COMMENT 'IP address',
  user_agent VARCHAR(512) NULL COMMENT 'User agent',
  detail_json JSON NULL COMMENT 'Detail JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_operation_logs_admin_id (admin_id),
  KEY idx_operation_logs_target (target_type, target_id),
  KEY idx_operation_logs_created_at (created_at),
  CONSTRAINT fk_operation_logs_admin FOREIGN KEY (admin_id) REFERENCES admin_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backend operation logs';

