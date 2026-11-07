# Phân hệ 06: Đơn Hàng & Thanh Toán Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Giỏ Hàng, Tính Thuế VAT (15%), Tips, Đặt Món & Hóa Đơn Điện Tử E-Receipt**  
> **Ánh xạ Frontend:** `CartPage.tsx`, `OrdersPage.tsx`, `CheckoutView.tsx`, `EReceiptModal.tsx`  
> **File Migration:** `V7__create_orders_and_checkout.sql`  
> **JPA Entities:** `OrderJpaEntity.java`, `OrderItemJpaEntity.java`, `OrderItemModifierJpaEntity.java`

---

### Bảng 6.1: `orders` (Thông tin Đơn hàng & E-Receipt)
Lưu trữ toàn bộ đơn hàng (Figma 06.07 Checkout, 06.10 E-Receipt).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính đơn hàng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `store_id` | `UUID` | NO | | FK -> `stores(id)` | Cửa hàng tiếp nhận chế biến đơn (Linn Sushi Sentrum) |
| `user_id` | `UUID` | YES | | FK -> `users(id)` | Khách hàng đăng nhập (hoặc null nếu khách vãng lai) |
| `order_number` | `VARCHAR(16)` | NO | | | Số thứ tự nhận món hàng ngày hiển thị to trên UI (ví dụ: `#005`, `#907` - Figma 06.04, 06.05) |
| `order_code` | `VARCHAR(32)` | NO | | UK | Mã hiển thị trên hóa đơn (ví dụ: `ORD-20261105-0891`) |
| `pickup_method` | `VARCHAR(32)` | NO | `'COUNTER'` | | Hình thức nhận: `COUNTER` (nhận tại quầy), `TABLE` (nhận tại bàn) |
| `pickup_time_type`| `VARCHAR(32)`| NO | `'ASAP'` | | Thời gian lấy: `ASAP` (càng sớm càng tốt: 15 phút), `SCHEDULED` (hẹn giờ) |
| `scheduled_pickup_at`| `TIMESTAMPTZ`| YES | | | Giờ hẹn lấy đồ nếu chọn SCHEDULED |
| `table_number` | `VARCHAR(32)` | YES | | | Số bàn nếu khách chọn nhận tại bàn (`Table 05`) |
| `pickup_person_name`| `VARCHAR(128)`| NO | | | Tên người nhận (ví dụ: `David Nguyen`) |
| `pickup_person_phone`| `VARCHAR(32)`| NO | | | SĐT người nhận để tích điểm (ví dụ: `+4797362748`) |
| `subtotal_amount`| `NUMERIC(12,2)`| NO | | | Tiền hàng gốc đã gồm VAT (ví dụ: `267.00` Kr) |
| `discount_amount`| `NUMERIC(12,2)`| NO | `0.00` | | Số tiền được giảm từ voucher (ví dụ: `100.00` Kr hoặc `0.00`) |
| `tip_amount` | `NUMERIC(12,2)`| NO | `0.00` | | Tiền boa tip cho nhân viên quầy (ví dụ: `+10.00` Kr) |
| `vat_amount` | `NUMERIC(12,2)`| NO | `0.00` | | Tiền thuế GTGT đã bao gồm trong tổng tiền: $Total \times \frac{15}{115}$ |
| `total_amount` | `NUMERIC(12,2)`| NO | | | Tổng số tiền thanh toán thực tế: `subtotal - discount + tip` (ví dụ: `177.00` Kr) |
| `points_earned` | `INT` | NO | `0` | | Số điểm tích được từ đơn hàng hiển thị trên UI (`+170 points`) |
| `status` | `VARCHAR(32)` | NO | `'PENDING'` | | Trạng thái: `PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED` |
| `payment_status`| `VARCHAR(32)` | NO | `'UNPAID'` | | Trạng thái thanh toán: `UNPAID`, `PAID`, `REFUNDED` |
| `payment_method`| `VARCHAR(32)` | YES | | | Phương thức: `CARD`, `VIPPS`, `APPLE_PAY`, `COUNTER_CASH` |
| `order_notes` | `VARCHAR(255)`| YES | | | Ghi chú của khách (ví dụ: `Special request, allergies, etc`) |
| `applied_voucher_id`| `UUID` | YES | | FK -> `user_vouchers(id)` | Khóa ngoại voucher áp dụng cho đơn này |
| `applied_gift_id`| `UUID` | YES | | FK -> `loyalty_gifts(id)` | Khóa ngoại quà tặng tri ân cốc Tumbler đi kèm đơn (Figma 08.09, 08.10) |
| `applied_gift_title`| `VARCHAR(128)`| YES | | | Tên quà tặng thời điểm áp dụng để in biên nhận E-Receipt |
| `e_receipt_email` | `VARCHAR(255)`| YES | | | Địa chỉ Email khách yêu cầu gửi biên nhận điện tử E-Receipt (Figma 06.08, 06.10) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian bấm Pay đặt hàng |
| `completed_at` | `TIMESTAMPTZ` | YES | | | Thời gian khách nhận đồ xong |

#### Indexes & Constraints
- `uq_orders_tenant_code`: UNIQUE (`tenant_id`, `order_code`)
- `idx_orders_user_time`: (`user_id`, `created_at` DESC)
- `idx_orders_store_status`: (`store_id`, `status`, `created_at` DESC)
- `idx_orders_store_number`: (`store_id`, `order_number`, `created_at` DESC)

---

### Bảng 6.2: `order_items` (Chi tiết Từng Món trong Đơn hàng)
Lưu danh sách món ăn đã đặt (Figma 06.06: 2 Aurora, 1 Tuna Maki).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính món trong đơn |
| `order_id` | `UUID` | NO | | FK -> `orders(id)` ON DELETE CASCADE | Thuộc đơn hàng nào |
| `product_id` | `UUID` | NO | | FK -> `products(id)` | Món ăn gốc |
| `product_name` | `VARCHAR(255)` | NO | | | Tên món thời điểm đặt (tránh bị ảnh hưởng nếu đổi tên sau này) |
| `unit_price` | `NUMERIC(12,2)`| NO | | | Đơn giá món tại thời điểm đặt (ví dụ: `89.00` Kr) |
| `quantity` | `INT` | NO | `1` | | Số lượng đặt (ví dụ: 2 món Aurora, 1 món Tuna Maki) |
| `item_total` | `NUMERIC(12,2)`| NO | | | Thành tiền của dòng: `(unit_price + modifiers_price) * quantity` |
| `special_instruction`| `VARCHAR(255)`| YES | | | Ghi chú riêng cho món này |

#### Indexes & Constraints
- `idx_order_items_order`: (`order_id`)

---

### Bảng 6.3: `order_item_modifiers` (Tùy chọn Topping của Từng Món trong Đơn)
Lưu vết các topping, size đã chọn (Figma 06.06: Size L, Mango Popping, Less ice).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính modifier của item |
| `order_item_id` | `UUID` | NO | | FK -> `order_items(id)` ON DELETE CASCADE | Thuộc dòng món ăn nào |
| `modifier_group_name`| `VARCHAR(100)`| NO | | | Tên nhóm: `Size`, `Topping`, `Sweetness Level`, `Ice Level` |
| `modifier_name`| `VARCHAR(100)` | NO | | | Tên lựa chọn: `Size L`, `Mango Popping`, `Less ice` |
| `extra_price` | `NUMERIC(12,2)`| NO | `0.00` | | Giá phụ thu tại thời điểm đặt |

#### Indexes & Constraints
- `idx_item_modifiers_item`: (`order_item_id`)

---

### Bảng 6.4: `carts` (Phiên Làm Việc Giỏ Hàng Khách Hàng)
Quản lý giỏ hàng tạm thời trước khi tạo đơn hàng (Figma 06.01, 06.06). Đồng bộ giữa Redis và Postgres.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính phiên giỏ hàng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `store_id` | `UUID` | NO | | FK -> `stores(id)` | Chi nhánh cửa hàng đang chọn |
| `user_id` | `UUID` | YES | | FK -> `users(id)` ON DELETE CASCADE | Khách hàng đăng nhập (hoặc null nếu khách vãng lai) |
| `comment` | `VARCHAR(200)` | YES | | | Ghi chú đặc biệt hoặc dị ứng (Figma 06.06) |
| `applied_voucher_code`| `VARCHAR(64)`| YES | | | Mã voucher đang tạm gán vào giỏ |
| `applied_gift_id` | `UUID` | YES | | FK -> `loyalty_gifts(id)` | Món quà tri ân đang chọn |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm cập nhật giỏ gần nhất |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm tạo giỏ |

#### Indexes & Constraints
- `idx_carts_user`: (`tenant_id`, `user_id`)
- `idx_carts_updated`: (`updated_at`)

---

### Bảng 6.5: `cart_items` (Chi Tiết Món Ăn trong Giỏ Hàng)
Danh sách món đang chờ đặt trong giỏ (Figma 06.06: 2 Aurora, 1 Tuna Maki).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính món trong giỏ |
| `cart_id` | `UUID` | NO | | FK -> `carts(id)` ON DELETE CASCADE | Thuộc giỏ hàng nào |
| `product_id` | `UUID` | NO | | FK -> `products(id)` | Món ăn gốc trong menu |
| `quantity` | `INT` | NO | `1` | | Số lượng đặt (từ 1 đến 99) |
| `selected_modifier_ids`| `JSONB` | NO | `'[]'::jsonb` | | Danh sách ID các modifier đã chọn (Size, Topping, Đá, Ngọt) |
| `special_instruction`| `VARCHAR(255)`| YES | | | Lưu ý chế biến riêng cho món này |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm thêm vào giỏ |

#### Indexes & Constraints
- `idx_cart_items_cart`: (`cart_id`)

---

## Kiến Trúc Caching Giỏ Hàng với Redis (Cart Session Architecture)

1. **Redis Key Pattern:** `cart:{tenantId}:{cartId}` (hoặc `cart:{tenantId}:user:{userId}`).
2. **TTL (Time to Live):** 7 ngày kể từ thao tác cuối cùng của khách hàng.
3. **Cơ Chế Khóa Phân Tán:** Khi khách hàng bấm thanh toán (`POST /api/v1/orders/checkout`), hệ thống sử dụng Redisson Lock `lock:order:checkout:{userId}` trong 5 giây nhằm ngăn ngừa double-checkout trừ tiền lặp lại.
4. **Chuyển Đổi Dữ Liệu:** Khi đơn hàng thanh toán thành công (`PAID`), giỏ hàng trong Redis sẽ được xóa sạch và toàn bộ chi tiết món được ghi nhận vào bảng `orders`, `order_items`, `order_item_modifiers`.

---

## Tham Khảo DDL Flyway Mở Rộng (Migration Script Reference)

```sql
-- Flyway Script: V7__create_orders_and_checkout.sql

-- 1. Bảng orders
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE RESTRICT,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    order_number VARCHAR(16) NOT NULL, -- #005, #907
    order_code VARCHAR(32) NOT NULL,   -- ORD-20261105-0891
    pickup_method VARCHAR(32) NOT NULL DEFAULT 'COUNTER', -- COUNTER, TABLE
    pickup_time_type VARCHAR(32) NOT NULL DEFAULT 'ASAP', -- ASAP, SCHEDULED
    scheduled_pickup_at TIMESTAMPTZ,
    table_number VARCHAR(32),
    pickup_person_name VARCHAR(128) NOT NULL,
    pickup_person_phone VARCHAR(32) NOT NULL,
    subtotal_amount NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    tip_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    vat_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL,
    points_earned INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
    payment_method VARCHAR(32),
    order_notes VARCHAR(255),
    applied_voucher_id UUID REFERENCES user_vouchers(id) ON DELETE SET NULL,
    applied_gift_id UUID REFERENCES loyalty_gifts(id) ON DELETE SET NULL,
    applied_gift_title VARCHAR(128),
    e_receipt_email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_orders_tenant_code UNIQUE (tenant_id, order_code)
);

CREATE INDEX idx_orders_user_time ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_store_status ON orders(store_id, status, created_at DESC);
CREATE INDEX idx_orders_store_number ON orders(store_id, order_number, created_at DESC);

-- 2. Bảng order_items
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    item_total NUMERIC(12,2) NOT NULL,
    special_instruction VARCHAR(255)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- 3. Bảng order_item_modifiers
CREATE TABLE order_item_modifiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    modifier_group_name VARCHAR(100) NOT NULL,
    modifier_name VARCHAR(100) NOT NULL,
    extra_price NUMERIC(12,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX idx_item_modifiers_item ON order_item_modifiers(order_item_id);

-- 4. Bảng carts
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    comment VARCHAR(200),
    applied_voucher_code VARCHAR(64),
    applied_gift_id UUID REFERENCES loyalty_gifts(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_carts_user ON carts(tenant_id, user_id);

-- 5. Bảng cart_items
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    selected_modifier_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    special_instruction VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
```

