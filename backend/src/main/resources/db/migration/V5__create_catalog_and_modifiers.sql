-- ==============================================================================
-- V5__create_catalog_and_modifiers.sql
-- Phân hệ 4: Danh mục, Nhóm con, Món ăn, Tùy chọn Modifiers & Banner Khuyến mãi
-- Ánh xạ: docs/designs/db-design/04-catalog-and-menu-db.md
-- ==============================================================================

-- 1. Bảng categories (Nhóm Món ăn / Đồ uống Chính)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE categories IS 'Danh mục phân loại thực đơn chính hiển thị trên thanh cuộn ngang (Figma 01.06: Popular, Maki, Nigiri, Drinks, Hot Food)';
COMMENT ON COLUMN categories.id IS 'Khóa chính nhóm món';
COMMENT ON COLUMN categories.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN categories.name IS 'Tên nhóm món hiển thị';
COMMENT ON COLUMN categories.icon IS 'Tên icon Lucide hiển thị cạnh tên (flame, sushi, cup-soda)';
COMMENT ON COLUMN categories.display_order IS 'Thứ tự hiển thị trên thanh cuộn menu';
COMMENT ON COLUMN categories.is_active IS 'Cho phép hiển thị trên thực đơn';
COMMENT ON COLUMN categories.created_at IS 'Thời gian tạo danh mục';

CREATE INDEX idx_categories_tenant ON categories(tenant_id, display_order);

-- 2. Bảng subcategories (Nhóm Con / Subcategory Pills)
CREATE TABLE subcategories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE subcategories IS 'Phân loại phụ hiển thị dạng nút pill bo tròn dưới danh mục chính (Figma 01.06: Fresh maki, Fried maki)';
COMMENT ON COLUMN subcategories.id IS 'Khóa chính nhóm con';
COMMENT ON COLUMN subcategories.category_id IS 'Khóa ngoại thuộc danh mục cha nào';
COMMENT ON COLUMN subcategories.name IS 'Tên nhóm phụ (ví dụ: Fresh maki, Fried maki, Special rolls)';
COMMENT ON COLUMN subcategories.display_order IS 'Thứ tự sắp xếp hiển thị thanh pill';
COMMENT ON COLUMN subcategories.is_active IS 'Trạng thái hiển thị';
COMMENT ON COLUMN subcategories.created_at IS 'Thời gian tạo';

CREATE INDEX idx_subcategories_category ON subcategories(category_id, display_order);

-- 3. Bảng products (Món ăn / Đồ uống)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    subcategory_id UUID REFERENCES subcategories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(12,2) NOT NULL,
    original_price NUMERIC(12,2),
    pieces_text VARCHAR(50),
    dietary_tags JSONB NOT NULL DEFAULT '[]',
    image_url TEXT,
    is_available BOOLEAN NOT NULL DEFAULT true,
    vat_rate NUMERIC(4,2) NOT NULL DEFAULT 15.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE products IS 'Danh sách các món ăn, đồ uống trong thực đơn (Figma 01.06, 06.06: Aurora 89 Kr, Tuna Maki 89 Kr)';
COMMENT ON COLUMN products.id IS 'Khóa chính của món ăn';
COMMENT ON COLUMN products.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN products.category_id IS 'Khóa ngoại thuộc nhóm danh mục nào';
COMMENT ON COLUMN products.subcategory_id IS 'Khóa ngoại thuộc nhóm con nào (nếu có)';
COMMENT ON COLUMN products.name IS 'Tên món ăn (ví dụ: Aurora, Tuna Maki, Laks Maki)';
COMMENT ON COLUMN products.description IS 'Mô tả nguyên liệu, hương vị';
COMMENT ON COLUMN products.base_price IS 'Giá bán tiêu chuẩn chưa bao gồm topping (ví dụ: 89.00 Kr)';
COMMENT ON COLUMN products.original_price IS 'Giá gốc gạch ngang khi có ưu đãi (ví dụ: 130.00 Kr trong Figma 01.06)';
COMMENT ON COLUMN products.pieces_text IS 'Số lượng miếng hiển thị trên card (ví dụ: 8 biter / 8 pieces)';
COMMENT ON COLUMN products.dietary_tags IS 'Thẻ đặc tính món ăn dạng JSON (ví dụ: ["Popular", "Gluten Free", "Spicy"])';
COMMENT ON COLUMN products.image_url IS 'Đường dẫn ảnh chụp món ăn';
COMMENT ON COLUMN products.is_available IS 'Món còn hàng để phục vụ hay đã hết hàng tạm thời';
COMMENT ON COLUMN products.vat_rate IS 'Mức thuế VAT áp dụng theo luật (ví dụ: 15.00% cho đồ ăn mang về hoặc 25%)';
COMMENT ON COLUMN products.created_at IS 'Thời điểm tạo món';
COMMENT ON COLUMN products.updated_at IS 'Thời điểm cập nhật thông tin món';

CREATE INDEX idx_products_category ON products(category_id, is_available);
CREATE INDEX idx_products_subcategory ON products(subcategory_id, is_available);
CREATE INDEX idx_products_tenant_search ON products(tenant_id, name);

-- 4. Bảng product_modifier_groups (Nhóm Tùy chọn Topping / Size)
CREATE TABLE product_modifier_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    min_selection INT NOT NULL DEFAULT 0,
    max_selection INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0
);

COMMENT ON TABLE product_modifier_groups IS 'Nhóm tùy chọn thuộc tính kèm món: Size, Topping, Sweetness Level, Ice Level (Figma 06.06)';
COMMENT ON COLUMN product_modifier_groups.id IS 'Khóa chính nhóm tùy chọn';
COMMENT ON COLUMN product_modifier_groups.product_id IS 'Khóa ngoại thuộc món ăn nào';
COMMENT ON COLUMN product_modifier_groups.name IS 'Tên nhóm tùy chọn (Size, Topping, Mức ngọt, Lượng đá)';
COMMENT ON COLUMN product_modifier_groups.min_selection IS 'Số lựa chọn tối thiểu bắt buộc chọn (0: tùy ý, 1: bắt buộc)';
COMMENT ON COLUMN product_modifier_groups.max_selection IS 'Số lựa chọn tối đa được chọn (1: Radio button, >1: Checkbox)';
COMMENT ON COLUMN product_modifier_groups.display_order IS 'Thứ tự hiển thị các nhóm tùy chọn';

CREATE INDEX idx_modifier_groups_product ON product_modifier_groups(product_id, display_order);

-- 5. Bảng product_modifiers (Các Tùy chọn Chi tiết & Giá Phụ thu)
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

-- 6. Bảng promotion_banners (Banner Khuyến mãi & Chiến dịch Tiếp thị)
CREATE TABLE promotion_banners (
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

COMMENT ON TABLE promotion_banners IS 'Quản trị các banner quảng cáo chiến dịch trên đầu trang thực đơn (Figma 01.06: Summer Sales: 20% Off)';
COMMENT ON COLUMN promotion_banners.id IS 'Khóa chính banner';
COMMENT ON COLUMN promotion_banners.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN promotion_banners.store_id IS 'Áp dụng riêng chi nhánh (NULL = áp dụng toàn hệ thống)';
COMMENT ON COLUMN promotion_banners.title IS 'Tiêu đề banner (ví dụ: Summer Sales: 20% Off)';
COMMENT ON COLUMN promotion_banners.subtitle IS 'Nội dung phụ mô tả ưu đãi';
COMMENT ON COLUMN promotion_banners.image_url IS 'Đường dẫn ảnh banner nền';
COMMENT ON COLUMN promotion_banners.cta_label IS 'Nhãn nút hành động (ví dụ: Learn more, Order Now)';
COMMENT ON COLUMN promotion_banners.action_type IS 'Loại chuyển trang (NAVIGATE_CATEGORY, OPEN_MODAL, OPEN_URL)';
COMMENT ON COLUMN promotion_banners.target_id IS 'ID đối tượng chuyển hướng đến (ví dụ ID danh mục)';
COMMENT ON COLUMN promotion_banners.is_active IS 'Trạng thái hiển thị';
COMMENT ON COLUMN promotion_banners.display_order IS 'Thứ tự hiển thị xoay vòng (carousel)';
COMMENT ON COLUMN promotion_banners.created_at IS 'Thời gian tạo';
COMMENT ON COLUMN promotion_banners.updated_at IS 'Thời gian cập nhật';

CREATE INDEX idx_promotion_banners_tenant_store ON promotion_banners(tenant_id, store_id, is_active);
