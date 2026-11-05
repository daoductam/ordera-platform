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
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian bấm Pay đặt hàng |
| `completed_at` | `TIMESTAMPTZ` | YES | | | Thời gian khách nhận đồ xong |

#### Indexes & Constraints
- `uq_orders_tenant_code`: UNIQUE (`tenant_id`, `order_code`)
- `idx_orders_user_time`: (`user_id`, `created_at` DESC)
- `idx_orders_store_status`: (`store_id`, `status`, `created_at` DESC)

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

