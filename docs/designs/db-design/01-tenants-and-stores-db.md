# Phân hệ 01: Tenancy & Cửa Hàng Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Quản trị Thương hiệu, Điểm bán, Bàn ăn & Cửa hàng Yêu thích**  
> **Ánh xạ Frontend:** `StoreDiscoveryPage.tsx`, `RestaurantList.tsx`, `LocationConfirmModal.tsx`  
> **File Migration:** `V1__create_tenants_and_stores.sql` & `V10__add_store_fields_and_favorites.sql`  
> **JPA Entities:** `TenantJpaEntity.java`, `StoreJpaEntity.java`, `StoreTableJpaEntity.java`, `CustomerFavoriteStoreJpaEntity.java`

---

### Bảng 1.1: `tenants` (Quản lý Thương hiệu / Đại lý)
Bảng trung tâm lưu trữ thông tin các thương hiệu F&B trong mô hình SaaS White-Label.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính định danh thương hiệu/đại lý |
| `code` | `VARCHAR(50)` | NO | | UK | Mã viết tắt đại lý (ví dụ: `linn_sushi`, `aby_tea`) |
| `name` | `VARCHAR(255)` | NO | | | Tên hiển thị thương hiệu (ví dụ: `Agency Name`, `Linn Sushi`) |
| `logo_url` | `TEXT` | YES | | | Đường dẫn URL logo thương hiệu |
| `primary_color` | `VARCHAR(16)` | YES | `#F5B800` | | Mã màu nhận diện chính (Hex code) dùng để render UI động |
| `secondary_color`| `VARCHAR(16)` | YES | `#1E293B` | | Mã màu nhận diện phụ |
| `status` | `VARCHAR(32)` | NO | `'ACTIVE'` | | Trạng thái hoạt động: `ACTIVE`, `SUSPENDED`, `INACTIVE` |
| `metadata` | `JSONB` | YES | `'{}'` | | Cấu hình mở rộng: slogan, hotline, email hỗ trợ, currency, defaultVatRate |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm tạo thương hiệu |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm cập nhật cấu hình lần cuối |

#### Indexes & Constraints
- `uq_tenants_code`: UNIQUE (`code`)

---

### Bảng 1.2: `stores` (Danh sách Cửa hàng / Chi nhánh)
Lưu trữ danh sách các điểm bán (Outlets) trực thuộc thương hiệu (Figma Nhóm 01: Linn Sushi Sentrum).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính chi nhánh cửa hàng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE RESTRICT | Định danh thương hiệu sở hữu chi nhánh này |
| `name` | `VARCHAR(255)` | NO | | | Tên chi nhánh (ví dụ: `Linn Sushi Sentrum`) |
| `address` | `TEXT` | NO | | | Địa chỉ đầy đủ (ví dụ: `Prinsens gate 10C, 0153 Oslo`) |
| `city` | `VARCHAR(100)` | NO | `'Oslo'` | | Thành phố đặt chi nhánh |
| `latitude` | `NUMERIC(10,7)`| YES | | | Tọa độ vĩ độ (GPS Latitude) để tính khoảng cách tìm cửa hàng gần nhất |
| `longitude` | `NUMERIC(10,7)`| YES | | | Tọa độ kinh độ (GPS Longitude) |
| `cover_image_url`| `TEXT` | YES | | | Ảnh bìa món/quán hiển thị trên thẻ chi nhánh (Figma `01.02`) |
| `logo_url` | `TEXT` | YES | | | Ảnh logo tròn của chi nhánh |
| `phone_number` | `VARCHAR(32)` | YES | | | Số điện thoại liên hệ của cửa hàng |
| `opening_hours` | `VARCHAR(100)` | YES | `'10:00 - 22:00'` | | Giờ mở cửa hàng ngày hiển thị lên app |
| `is_active` | `BOOLEAN` | NO | `true` | | Cửa hàng còn hoạt động hay đóng cửa |
| `accepts_pickup`| `BOOLEAN` | NO | `true` | | Cho phép đặt món nhận tại quầy (Counter Pickup) |
| `accepts_dinein`| `BOOLEAN` | NO | `true` | | Cho phép đặt món ngồi ăn tại bàn (Table Dine-in) |
| `accepts_delivery`| `BOOLEAN`| NO | `true` | | Cho phép đặt món giao hàng tận nơi (Delivery) |
| `delivery_radius_km`| `NUMERIC(5,2)`| YES | `5.0` | | Bán kính giao hàng tối đa (km) |
| `delivery_fee` | `NUMERIC(10,2)`| YES | `0.00` | | Phí giao hàng cơ bản (NOK) |
| `preparing_time_minutes`| `INT`| YES | `15` | | Thời gian chuẩn bị món ước tính (phút) hiển thị trên thẻ quán |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo chi nhánh |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian sửa thông tin chi nhánh |

#### Indexes & Constraints
- `idx_stores_tenant_id`: (`tenant_id`)
- `idx_stores_active_city`: (`tenant_id`, `city`, `is_active`)

---

### Bảng 1.3: `store_tables` (Quản lý Bàn tại quán)
Quản lý số bàn phục vụ hình thức nhận món tại bàn (Figma Nhóm 06: `Table: Select a table` & Quét QR `01.01`).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính của bàn |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu |
| `store_id` | `UUID` | NO | | FK -> `stores(id)` ON DELETE CASCADE | Thuộc chi nhánh cửa hàng nào |
| `table_number` | `VARCHAR(32)` | NO | | | Tên/Số bàn hiển thị (ví dụ: `Table 05`, `VIP-02`) |
| `zone` | `VARCHAR(100)` | YES | | | Khu vực đặt bàn (ví dụ: `Tầng 1 - Khu trong nhà`, `Ban công`) |
| `seating_capacity`| `INT` | NO | `4` | | Sức chứa số lượng khách tối đa của bàn |
| `qr_code_token`| `VARCHAR(128)`| YES | | UK | Mã Token QR code dán trên bàn để khách quét đặt trực tiếp |
| `is_occupied` | `BOOLEAN` | NO | `false` | | Trạng thái bàn đang có khách ngồi hay bàn trống |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo bàn |

#### Indexes & Constraints
- `idx_store_tables_store`: (`store_id`, `table_number`)
- `uq_store_tables_token`: UNIQUE (`qr_code_token`)

---

### Bảng 1.4: `customer_favorite_stores` (Cửa hàng Yêu thích của Hội viên)
Lưu trữ trạng thái yêu thích cửa hàng (Icon trái tim đỏ tại màn hình `01.02`, `01.04`).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính bản ghi yêu thích |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE CASCADE | Phân lập theo thương hiệu |
| `user_id` | `UUID` | NO | | FK -> `users(id)` ON DELETE CASCADE | Hội viên bấm yêu thích |
| `store_id` | `UUID` | NO | | FK -> `stores(id)` ON DELETE CASCADE | Chi nhánh cửa hàng được thích |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm bấm yêu thích |

#### Indexes & Constraints
- `uq_customer_store_favorite`: UNIQUE (`tenant_id`, `user_id`, `store_id`)
- `idx_fav_stores_user`: (`tenant_id`, `user_id`)
