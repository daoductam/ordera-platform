-- ==============================================================================
-- V5__create_catalog_and_modifiers.sql
-- Phân hệ 4: Danh mục & Món ăn (Catalog, Products, Modifiers)
-- ==============================================================================

-- 1. Bảng categories (Nhóm Món ăn / Đồ uống)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE categories IS 'Danh mục phân loại thực đơn: Sushi Rolls, Boba Tea, Sides, Desserts';
COMMENT ON COLUMN categories.id IS 'Khóa chính nhóm món';
COMMENT ON COLUMN categories.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN categories.name IS 'Tên nhóm món hiển thị';
COMMENT ON COLUMN categories.display_order IS 'Thứ tự hiển thị trên thanh cuộn menu';
COMMENT ON COLUMN categories.is_active IS 'Cho phép hiển thị trên thực đơn';
COMMENT ON COLUMN categories.created_at IS 'Thời gian tạo danh mục';

CREATE INDEX idx_categories_tenant ON categories(tenant_id, display_order);

-- 2. Bảng products (Món ăn / Đồ uống)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(12,2) NOT NULL,
    image_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT true,
    vat_rate NUMERIC(4,2) NOT NULL DEFAULT 15.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE products IS 'Danh sách các món ăn, đồ uống trong thực đơn (Aurora, Tuna Maki 89 Kr)';
COMMENT ON COLUMN products.id IS 'Khóa chính của món ăn';
COMMENT ON COLUMN products.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN products.category_id IS 'Khóa ngoại thuộc nhóm danh mục nào';
COMMENT ON COLUMN products.name IS 'Tên món ăn (Aurora, Tuna Maki)';
COMMENT ON COLUMN products.description IS 'Mô tả nguyên liệu, hương vị';
COMMENT ON COLUMN products.base_price IS 'Giá bán tiêu chuẩn chưa bao gồm topping (ví dụ: 89.00 Kr)';
COMMENT ON COLUMN products.image_url IS 'Đường dẫn ảnh chụp món ăn';
COMMENT ON COLUMN products.is_available IS 'Món còn hàng để phục vụ hay đã hết hàng tạm thời';
COMMENT ON COLUMN products.vat_rate IS 'Mức thuế VAT áp dụng theo luật (ví dụ: 15.00% cho đồ ăn)';
COMMENT ON COLUMN products.created_at IS 'Thời điểm tạo món';
COMMENT ON COLUMN products.updated_at IS 'Thời điểm cập nhật thông tin món';

CREATE INDEX idx_products_category ON products(category_id, is_available);
CREATE INDEX idx_products_tenant_search ON products(tenant_id, name);

-- 3. Bảng product_modifier_groups (Nhóm Tùy chọn Topping / Size)
CREATE TABLE product_modifier_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    min_selection INT NOT NULL DEFAULT 0,
    max_selection INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0
);

COMMENT ON TABLE product_modifier_groups IS 'Nhóm tùy chọn thuộc tính kèm món: Size, Topping, Sweetness Level, Ice Level';
COMMENT ON COLUMN product_modifier_groups.id IS 'Khóa chính nhóm tùy chọn';
COMMENT ON COLUMN product_modifier_groups.product_id IS 'Khóa ngoại thuộc món ăn nào';
COMMENT ON COLUMN product_modifier_groups.name IS 'Tên nhóm tùy chọn (Size, Topping, Mức ngọt, Lượng đá)';
COMMENT ON COLUMN product_modifier_groups.min_selection IS 'Số lựa chọn tối thiểu bắt buộc chọn (0: tùy ý, 1: bắt buộc)';
COMMENT ON COLUMN product_modifier_groups.max_selection IS 'Số lựa chọn tối đa được chọn (1: Radio button, >1: Checkbox)';
COMMENT ON COLUMN product_modifier_groups.display_order IS 'Thứ tự hiển thị các nhóm tùy chọn';

CREATE INDEX idx_modifier_groups_product ON product_modifier_groups(product_id, display_order);

-- 4. Bảng product_modifiers (Các Tùy chọn Chi tiết & Giá Phụ thu)
CREATE TABLE product_modifiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES product_modifier_groups(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    extra_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    is_default BOOLEAN NOT NULL DEFAULT false
);

COMMENT ON TABLE product_modifiers IS 'Các tùy chọn chi tiết bên trong nhóm: Size L (+15 Kr), Mango Popping (+10 Kr), Less ice';
COMMENT ON COLUMN product_modifiers.id IS 'Khóa chính tùy chọn chi tiết';
COMMENT ON COLUMN product_modifiers.group_id IS 'Khóa ngoại thuộc nhóm tùy chọn nào';
COMMENT ON COLUMN product_modifiers.name IS 'Tên tùy chọn (Size L, Mango Popping, Ít đá)';
COMMENT ON COLUMN product_modifiers.extra_price IS 'Giá phụ thu cộng thêm vào tiền món (15.00 Kr, 10.00 Kr)';
COMMENT ON COLUMN product_modifiers.is_default IS 'Được chọn mặc định khi mở món';

CREATE INDEX idx_modifiers_group ON product_modifiers(group_id);

