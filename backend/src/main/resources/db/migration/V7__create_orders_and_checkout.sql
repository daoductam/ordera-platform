-- ==============================================================================
-- V7__create_orders_and_checkout.sql
-- Phân hệ 6: Đơn hàng, Giỏ hàng, Chi tiết Món, Modifiers & E-Receipt
-- Ánh xạ: docs/designs/db-design/06-orders-and-checkout-db.md
-- ==============================================================================

-- 1. Bảng orders (Thông tin Đơn hàng & Hóa đơn Điện tử E-Receipt)
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
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
    payment_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PAID, REFUNDED
    payment_method VARCHAR(32), -- CARD, VIPPS, APPLE_PAY, COUNTER_CASH
    order_notes VARCHAR(255),
    applied_voucher_id UUID REFERENCES user_vouchers(id) ON DELETE SET NULL,
    applied_gift_id UUID REFERENCES loyalty_gifts(id) ON DELETE SET NULL,
    applied_gift_title VARCHAR(128),
    e_receipt_email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_orders_tenant_code UNIQUE (tenant_id, order_code)
);

COMMENT ON TABLE orders IS 'Lưu trữ thông tin chi tiết đơn hàng đặt món, thuế VAT (15%), tips và E-Receipt (Figma 06.07, 06.10)';
COMMENT ON COLUMN orders.id IS 'Khóa chính định danh đơn hàng';
COMMENT ON COLUMN orders.tenant_id IS 'Khóa ngoại thuộc thương hiệu nào';
COMMENT ON COLUMN orders.store_id IS 'Khóa ngoại chi nhánh cửa hàng chế biến đơn (Linn Sushi Sentrum)';
COMMENT ON COLUMN orders.user_id IS 'Khóa ngoại khách hàng đăng nhập (hoặc null nếu khách vãng lai)';
COMMENT ON COLUMN orders.order_number IS 'Số thứ tự nhận món hàng ngày hiển thị to trên UI (ví dụ: #005, #907 - Figma 06.04, 06.05)';
COMMENT ON COLUMN orders.order_code IS 'Mã hiển thị trên hóa đơn và e-receipt (ORD-20261105-0891)';
COMMENT ON COLUMN orders.pickup_method IS 'Hình thức nhận đồ: COUNTER (tại quầy), TABLE (tại bàn)';
COMMENT ON COLUMN orders.pickup_time_type IS 'Thời gian lấy đồ: ASAP (càng sớm càng tốt: 15 phút), SCHEDULED (hẹn giờ)';
COMMENT ON COLUMN orders.scheduled_pickup_at IS 'Khung giờ hẹn lấy món nếu chọn SCHEDULED';
COMMENT ON COLUMN orders.table_number IS 'Số bàn ăn nếu khách chọn nhận tại bàn (Table 05)';
COMMENT ON COLUMN orders.pickup_person_name IS 'Tên người đến lấy đồ (David Nguyen)';
COMMENT ON COLUMN orders.pickup_person_phone IS 'Số điện thoại người lấy đồ để tích điểm (+4797362748)';
COMMENT ON COLUMN orders.subtotal_amount IS 'Tiền hàng gốc trước giảm giá (267.00 Kr)';
COMMENT ON COLUMN orders.discount_amount IS 'Số tiền được giảm trừ từ voucher áp dụng (100.00 Kr hoặc 0.00)';
COMMENT ON COLUMN orders.tip_amount IS 'Tiền boa tip cho nhân viên phục vụ (+10.00 Kr)';
COMMENT ON COLUMN orders.vat_amount IS 'Tiền thuế GTGT VAT 15% đã bao gồm trong tổng tiền';
COMMENT ON COLUMN orders.total_amount IS 'Tổng số tiền thanh toán thực tế: subtotal - discount + tip (177.00 Kr)';
COMMENT ON COLUMN orders.points_earned IS 'Số điểm tích được từ đơn hàng hiển thị trên UI (+170 points)';
COMMENT ON COLUMN orders.status IS 'Trạng thái đơn: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED';
COMMENT ON COLUMN orders.payment_status IS 'Trạng thái thanh toán: UNPAID, PAID, REFUNDED';
COMMENT ON COLUMN orders.payment_method IS 'Phương thức thanh toán: CARD, VIPPS, APPLE_PAY, COUNTER_CASH';
COMMENT ON COLUMN orders.order_notes IS 'Ghi chú đặc biệt của khách (Special request, allergies, etc)';
COMMENT ON COLUMN orders.applied_voucher_id IS 'Khóa ngoại voucher được áp dụng cho đơn này';
COMMENT ON COLUMN orders.applied_gift_id IS 'Khóa ngoại quà tặng tri ân cốc Tumbler đi kèm đơn (Figma 08.09, 08.10)';
COMMENT ON COLUMN orders.applied_gift_title IS 'Tên quà tặng thời điểm áp dụng để in biên nhận E-Receipt';
COMMENT ON COLUMN orders.e_receipt_email IS 'Địa chỉ Email khách yêu cầu gửi biên nhận điện tử E-Receipt (Figma 06.08, 06.10)';
COMMENT ON COLUMN orders.created_at IS 'Thời điểm bấm Pay đặt hàng';
COMMENT ON COLUMN orders.completed_at IS 'Thời điểm hoàn tất đơn hàng';

CREATE INDEX idx_orders_user_time ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_store_status ON orders(store_id, status, created_at DESC);
CREATE INDEX idx_orders_store_number ON orders(store_id, order_number, created_at DESC);

-- 2. Bảng order_items (Chi tiết Từng Món trong Đơn hàng)
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

COMMENT ON TABLE order_items IS 'Chi tiết các món ăn, số lượng và đơn giá được đặt trong đơn hàng (Figma 06.06: 2 Aurora, 1 Tuna Maki)';
COMMENT ON COLUMN order_items.id IS 'Khóa chính dòng món ăn trong đơn';
COMMENT ON COLUMN order_items.order_id IS 'Khóa ngoại liên kết tới đơn hàng';
COMMENT ON COLUMN order_items.product_id IS 'Khóa ngoại món ăn gốc';
COMMENT ON COLUMN order_items.product_name IS 'Tên món ăn tại thời điểm đặt (tránh bị ảnh hưởng nếu đổi tên sau này)';
COMMENT ON COLUMN order_items.unit_price IS 'Đơn giá món tại thời điểm đặt (89.00 Kr)';
COMMENT ON COLUMN order_items.quantity IS 'Số lượng đặt (ví dụ: 2 Aurora, 1 Tuna Maki)';
COMMENT ON COLUMN order_items.item_total IS 'Thành tiền dòng món: (unit_price + modifiers) * quantity';
COMMENT ON COLUMN order_items.special_instruction IS 'Ghi chú chế biến riêng cho món này';

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- 3. Bảng order_item_modifiers (Tùy chọn Topping của Từng Món trong Đơn)
CREATE TABLE order_item_modifiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    modifier_group_name VARCHAR(100) NOT NULL,
    modifier_name VARCHAR(100) NOT NULL,
    extra_price NUMERIC(12,2) NOT NULL DEFAULT 0.00
);

COMMENT ON TABLE order_item_modifiers IS 'Topping, size, mức ngọt, đá đã chọn kèm theo từng món ăn (Figma 06.06: Size L, Mango Popping, Less ice)';
COMMENT ON COLUMN order_item_modifiers.id IS 'Khóa chính tùy chọn modifier';
COMMENT ON COLUMN order_item_modifiers.order_item_id IS 'Khóa ngoại thuộc món ăn nào trong đơn';
COMMENT ON COLUMN order_item_modifiers.modifier_group_name IS 'Tên nhóm tùy chọn (Size, Topping, Sweetness Level, Ice Level)';
COMMENT ON COLUMN order_item_modifiers.modifier_name IS 'Tên tùy chọn đã chọn (Size L, Mango Popping, Less ice)';
COMMENT ON COLUMN order_item_modifiers.extra_price IS 'Giá phụ thu tại thời điểm đặt';

CREATE INDEX idx_item_modifiers_item ON order_item_modifiers(order_item_id);

-- 4. Bảng carts (Phiên Làm Việc Giỏ Hàng Khách Hàng)
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

COMMENT ON TABLE carts IS 'Quản lý giỏ hàng tạm thời trước khi tạo đơn hàng (Figma 06.01, 06.06). Đồng bộ giữa Redis và Postgres';
COMMENT ON COLUMN carts.id IS 'Khóa chính phiên giỏ hàng';
COMMENT ON COLUMN carts.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN carts.store_id IS 'Khóa ngoại chi nhánh cửa hàng đang chọn';
COMMENT ON COLUMN carts.user_id IS 'Khóa ngoại khách hàng đăng nhập (hoặc null nếu khách vãng lai)';
COMMENT ON COLUMN carts.comment IS 'Ghi chú đặc biệt hoặc dị ứng (Figma 06.06)';
COMMENT ON COLUMN carts.applied_voucher_code IS 'Mã voucher đang tạm gán vào giỏ';
COMMENT ON COLUMN carts.applied_gift_id IS 'Khóa ngoại món quà tri ân đang chọn';
COMMENT ON COLUMN carts.updated_at IS 'Thời điểm cập nhật giỏ gần nhất';
COMMENT ON COLUMN carts.created_at IS 'Thời điểm tạo giỏ';

CREATE INDEX idx_carts_user ON carts(tenant_id, user_id);
CREATE INDEX idx_carts_updated ON carts(updated_at);

-- 5. Bảng cart_items (Chi Tiết Món Ăn trong Giỏ Hàng)
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    selected_modifier_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    special_instruction VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE cart_items IS 'Danh sách món đang chờ đặt trong giỏ (Figma 06.06: 2 Aurora, 1 Tuna Maki)';
COMMENT ON COLUMN cart_items.id IS 'Khóa chính món trong giỏ';
COMMENT ON COLUMN cart_items.cart_id IS 'Khóa ngoại thuộc giỏ hàng nào';
COMMENT ON COLUMN cart_items.product_id IS 'Khóa ngoại món ăn gốc trong menu';
COMMENT ON COLUMN cart_items.quantity IS 'Số lượng đặt (từ 1 đến 99)';
COMMENT ON COLUMN cart_items.selected_modifier_ids IS 'Danh sách ID các modifier đã chọn (Size, Topping, Đá, Ngọt)';
COMMENT ON COLUMN cart_items.special_instruction IS 'Lưu ý chế biến riêng cho món này';
COMMENT ON COLUMN cart_items.created_at IS 'Thời điểm thêm vào giỏ';

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- 6. Khóa ngoại liên kết chéo tới orders
ALTER TABLE user_vouchers
    ADD CONSTRAINT fk_user_vouchers_order
    FOREIGN KEY (applied_order_id) REFERENCES orders(id) ON DELETE SET NULL;

ALTER TABLE user_gift_redemptions
    ADD CONSTRAINT fk_user_gift_redemptions_order
    FOREIGN KEY (applied_order_id) REFERENCES orders(id) ON DELETE SET NULL;
