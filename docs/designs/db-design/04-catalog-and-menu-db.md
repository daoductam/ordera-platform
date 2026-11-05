# Phân hệ 04: Danh Mục & Món Ăn Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Danh Mục Món, Chi Tiết Sản Phẩm & Nhóm Tùy Chọn Modifier (Size, Topping, Đá/Đường)**  
> **Ánh xạ Frontend:** `HomePage.tsx`, `ProductDetailModal.tsx`  
> **File Migration:** `V5__create_catalog_and_modifiers.sql`  
> **JPA Entities:** `CategoryJpaEntity.java`, `ProductJpaEntity.java`, `ProductModifierGroupJpaEntity.java`, `ProductModifierJpaEntity.java`

---

### Bảng 4.1: `categories` (Nhóm Món ăn / Đồ uống)
Phân loại thực đơn (Figma Nhóm 01: Categories bar).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính danh mục |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `name` | `VARCHAR(100)` | NO | | | Tên nhóm món (ví dụ: `Sushi Rolls`, `Boba Tea`, `Sides`, `Desserts`) |
| `display_order` | `INT` | NO | `0` | | Thứ tự sắp xếp hiển thị trên thanh trượt danh mục |
| `is_active` | `BOOLEAN` | NO | `true` | | Cho phép hiển thị trên thực đơn |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo danh mục |

#### Indexes & Constraints
- `idx_categories_tenant`: (`tenant_id`, `display_order`)

---

### Bảng 4.2: `products` (Món ăn / Đồ uống)
Chi tiết món (Figma 01.06, 06.06: `Aurora` 89 Kr, `Tuna Maki` 89 Kr).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính của món |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `category_id` | `UUID` | NO | | FK -> `categories(id)` | Thuộc nhóm thực đơn nào |
| `name` | `VARCHAR(255)` | NO | | | Tên món ăn (ví dụ: `Aurora`, `Tuna Maki`) |
| `description` | `TEXT` | YES | | | Mô tả thành phần, hương vị món ăn |
| `base_price` | `NUMERIC(12,2)`| NO | | | Giá bán tiêu chuẩn chưa bao gồm topping (ví dụ: `89.00` Kr) |
| `image_url` | `TEXT` | YES | | | Đường dẫn ảnh món ăn |
| `is_available` | `BOOLEAN` | NO | `true` | | Món còn hàng để bán hay đã hết hàng tạm thời |
| `vat_rate` | `NUMERIC(4,2)` | NO | `15.00` | | Mức thuế VAT áp dụng theo luật Na Uy (15% đồ ăn mang về hoặc 25%) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo món |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian sửa món |

#### Indexes & Constraints
- `idx_products_category`: (`category_id`, `is_available`)
- `idx_products_tenant_search`: (`tenant_id`, `name`)

---

### Bảng 4.3: `product_modifier_groups` (Nhóm Tùy chọn Topping / Size)
Định nghĩa các nhóm thuộc tính có thể chọn kèm món ăn (Figma 06.06: `Size: L`, `Sweetness Level`, `Ice Level`).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính nhóm tùy chọn |
| `product_id` | `UUID` | NO | | FK -> `products(id)` ON DELETE CASCADE | Thuộc món ăn nào |
| `name` | `VARCHAR(100)` | NO | | | Tên nhóm tùy chọn (ví dụ: `Size`, `Topping`, `Sweetness Level`, `Ice Level`) |
| `min_selection` | `INT` | NO | `0` | | Số lượng tối thiểu bắt buộc chọn (0: không bắt buộc, 1: bắt buộc chọn 1) |
| `max_selection` | `INT` | NO | `1` | | Số lượng tối đa được chọn (1: Radio button, >1: Checkbox nhiều topping) |
| `display_order` | `INT` | NO | `0` | | Thứ tự hiển thị |

---

### Bảng 4.4: `product_modifiers` (Các Tùy chọn Chi tiết & Giá Phụ thu)
Danh sách các option bên trong nhóm modifier.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính option |
| `group_id` | `UUID` | NO | | FK -> `product_modifier_groups(id)` ON DELETE CASCADE | Thuộc nhóm modifier nào |
| `name` | `VARCHAR(100)` | NO | | | Tên option (ví dụ: `Size L`, `Mango Popping`, `Strawberry Popping`, `Standard Ice`, `Less ice`) |
| `extra_price` | `NUMERIC(12,2)`| NO | `0.00` | | Giá cộng thêm vào đơn (ví dụ: Size L cộng thêm `15.00` Kr, topping `10.00` Kr) |
| `is_default` | `BOOLEAN` | NO | `false` | | Tùy chọn mặc định được tích sẵn |

