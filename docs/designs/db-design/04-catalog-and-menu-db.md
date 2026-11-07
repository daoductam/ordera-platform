# Phân hệ 04: Danh Mục & Món Ăn Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Danh Mục Món, Nhóm Con, Chi Tiết Sản Phẩm, Banner Khuyến Mãi & Nhóm Tùy Chọn Modifier (Size, Topping, Đá/Đường)**  
> **Ánh xạ Prototype:** `docs/prototype/1-kham-pha-va-chon-cua-hang/` (`01.06`, `01.07`)  
> **Ánh xạ Frontend:** `MenuScreen.tsx`, `ProductDetailModal.tsx`, `ReorderCartView.tsx`  
> **File Migration:** `V5__create_catalog_and_modifiers.sql` (Kèm DDL nâng cấp hỗ trợ subcategories & banners)  
> **JPA Entities:** `CategoryJpaEntity.java`, `ProductJpaEntity.java`, `ProductModifierGroupJpaEntity.java`, `ProductModifierJpaEntity.java`

---

### Bảng 4.1: `categories` (Nhóm Món ăn / Đồ uống Chính)
Phân loại thực đơn chính hiển thị trên thanh cuộn ngang (Figma 01.06: `Popular`, `Maki`, `Nigiri`, `Drinks`, `Hot Food`).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính danh mục |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu nào (Multi-tenancy isolation) |
| `name` | `VARCHAR(100)` | NO | | | Tên nhóm món (ví dụ: `Popular`, `Maki`, `Nigiri`, `Drinks`, `Hot Food`) |
| `icon` | `VARCHAR(50)` | YES | | | Tên icon Lucide hiển thị cạnh tên (ví dụ: `flame`, `sushi`, `cup-soda`) |
| `display_order` | `INT` | NO | `0` | | Thứ tự sắp xếp hiển thị trên thanh trượt danh mục |
| `is_active` | `BOOLEAN` | NO | `true` | | Cho phép hiển thị trên thực đơn |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo danh mục |

#### Indexes & Constraints
- `idx_categories_tenant`: (`tenant_id`, `display_order`)

---

### Bảng 4.2: `subcategories` (Nhóm Con / Subcategory Pills)
Phân loại phụ hiển thị dạng nút pill bo tròn dưới danh mục chính (Figma 01.06: Dưới nhóm *Maki* có *Fresh maki*, *Fried maki*).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính nhóm con |
| `category_id` | `UUID` | NO | | FK -> `categories(id)` ON DELETE CASCADE | Thuộc danh mục cha nào |
| `name` | `VARCHAR(100)` | NO | | | Tên nhóm phụ (ví dụ: `Fresh maki`, `Fried maki`, `Special rolls`) |
| `display_order` | `INT` | NO | `0` | | Thứ tự sắp xếp hiển thị thanh pill |
| `is_active` | `BOOLEAN` | NO | `true` | | Trạng thái hiển thị |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo |

#### Indexes & Constraints
- `idx_subcategories_category`: (`category_id`, `display_order`)

---

### Bảng 4.3: `products` (Món ăn / Đồ uống)
Chi tiết món (Figma 01.06, 06.06: `Laks Maki`, `Aurora` 89 Kr, `Tuna Maki` 89 Kr).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính của món |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `category_id` | `UUID` | NO | | FK -> `categories(id)` | Thuộc nhóm thực đơn chính nào |
| `subcategory_id` | `UUID` | YES | | FK -> `subcategories(id)` ON DELETE SET NULL | Thuộc nhóm con nào (nếu có) |
| `name` | `VARCHAR(255)` | NO | | | Tên món ăn (ví dụ: `Laks Maki`, `Aurora`, `Tuna Maki`) |
| `description` | `TEXT` | YES | | | Mô tả thành phần, hương vị món ăn |
| `base_price` | `NUMERIC(12,2)`| NO | | | Giá bán tiêu chuẩn chưa bao gồm topping (ví dụ: `89.00` Kr) |
| `original_price` | `NUMERIC(12,2)`| YES | | | Giá gốc gạch ngang khi có ưu đãi (ví dụ: `130.00` Kr trong Figma 01.06) |
| `pieces_text` | `VARCHAR(50)` | YES | | | Số lượng miếng hiển thị trên card (ví dụ: `8 biter` / `8 pieces`) |
| `dietary_tags` | `JSONB` | NO | `'[]'` | | Thẻ đặc tính món ăn dạng JSON (ví dụ: `["Popular", "Gluten Free", "Spicy"]`) |
| `image_url` | `TEXT` | YES | | | Đường dẫn ảnh món ăn |
| `is_available` | `BOOLEAN` | NO | `true` | | Món còn hàng để bán hay đã hết hàng tạm thời |
| `vat_rate` | `NUMERIC(4,2)` | NO | `15.00` | | Mức thuế VAT áp dụng theo luật Na Uy (15% đồ ăn mang về hoặc 25%) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo món |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian sửa món |

#### Indexes & Constraints
- `idx_products_category`: (`category_id`, `is_available`)
- `idx_products_subcategory`: (`subcategory_id`, `is_available`)
- `idx_products_tenant_search`: (`tenant_id`, `name`)

---

### Bảng 4.4: `product_modifier_groups` (Nhóm Tùy chọn Topping / Size)
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

### Bảng 4.5: `product_modifiers` (Các Tùy chọn Chi tiết & Giá Phụ thu)
Danh sách các option bên trong nhóm modifier.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính option |
| `group_id` | `UUID` | NO | | FK -> `product_modifier_groups(id)` ON DELETE CASCADE | Thuộc nhóm modifier nào |
| `name` | `VARCHAR(100)` | NO | | | Tên option (ví dụ: `Size L`, `Mango Popping`, `Strawberry Popping`, `Standard Ice`, `Less ice`) |
| `extra_price` | `NUMERIC(12,2)`| NO | `0.00` | | Giá cộng thêm vào đơn (ví dụ: Size L cộng thêm `15.00` Kr, topping `10.00` Kr) |
| `is_default` | `BOOLEAN` | NO | `false` | | Tùy chọn mặc định được tích sẵn |

---

### Bảng 4.6: `promotion_banners` (Banner Khuyến mãi & Chiến dịch Tiếp thị)
Quản trị các banner quảng cáo chiến dịch trên đầu trang thực đơn (Figma 01.06: *"Summer Sales: 20% Off"*).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính banner |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE CASCADE | Thuộc thương hiệu |
| `store_id` | `UUID` | YES | | FK -> `stores(id)` ON DELETE CASCADE | Áp dụng riêng chi nhánh (NULL = áp dụng toàn hệ thống) |
| `title` | `VARCHAR(255)` | NO | | | Tiêu đề banner (ví dụ: `Summer Sales: 20% Off`) |
| `subtitle` | `TEXT` | YES | | | Nội dung phụ mô tả ưu đãi |
| `image_url` | `TEXT` | NO | | | Đường dẫn ảnh banner nền |
| `cta_label` | `VARCHAR(64)` | YES | | | Nhãn nút hành động (ví dụ: `Learn more`, `Order Now`) |
| `action_type` | `VARCHAR(64)` | YES | | | Loại chuyển trang (`NAVIGATE_CATEGORY`, `OPEN_MODAL`, `OPEN_URL`) |
| `target_id` | `UUID` | YES | | | ID đối tượng chuyển hướng đến (ví dụ ID danh mục `c1eebc99...`) |
| `is_active` | `BOOLEAN` | NO | `true` | | Trạng thái hiển thị |
| `display_order` | `INT` | NO | `0` | | Thứ tự hiển thị xoay vòng (carousel) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian cập nhật |

#### Indexes & Constraints
- `idx_promotion_banners_tenant_store`: (`tenant_id`, `store_id`, `is_active`)

---

## Tham Khảo DDL Flyway Mở Rộng (Migration Script Reference)

```sql
-- 1. Bổ sung icon cho categories
ALTER TABLE categories ADD COLUMN IF NOT EXISTS icon VARCHAR(50);

-- 2. Tạo bảng subcategories
CREATE TABLE IF NOT EXISTS subcategories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_subcategories_category ON subcategories(category_id, display_order);

-- 3. Bổ sung các trường UI vào bảng products
ALTER TABLE products ADD COLUMN IF NOT EXISTS subcategory_id UUID REFERENCES subcategories(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS original_price NUMERIC(12,2);
ALTER TABLE products ADD COLUMN IF NOT EXISTS pieces_text VARCHAR(50);
ALTER TABLE products ADD COLUMN IF NOT EXISTS dietary_tags JSONB NOT NULL DEFAULT '[]';
CREATE INDEX IF NOT EXISTS idx_products_subcategory ON products(subcategory_id, is_available);

-- 4. Tạo bảng promotion_banners
CREATE TABLE IF NOT EXISTS promotion_banners (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    store_id UUID REFERENCES stores(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    subtitle TEXT,
    image_url TEXT NOT NULL,
    cta_label VARCHAR(64),
    action_type VARCHAR(64),
    target_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_promotion_banners_tenant_store ON promotion_banners(tenant_id, store_id, is_active);
```
