-- ==============================================================================
-- V7__create_orders_and_checkout.sql
-- Phân hệ 6: Đơn hàng, Giỏ hàng & Thanh toán (Orders, Order Items, Modifiers)
-- ==============================================================================

-- 1. Bảng orders (Thông tin Đơn hàng & Hóa đơn Điện tử E-Receipt)
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE RESTRICT,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    order_code VARCHAR(32) NOT NULL,
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
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_orders_tenant_code UNIQUE (tenant_id, order_code)
);

COMMENT ON TABLE orders IS 'Lưu trữ thông tin chi tiết đơn hàng đặt món, thuế VAT, tips và thanh toán';
COMMENT ON COLUMN orders.id IS 'Khóa chính định danh đơn hàng';
COMMENT ON COLUMN orders.tenant_id IS 'Khóa ngoại thuộc thương hiệu nào';
COMMENT ON COLUMN orders.store_id IS 'Khóa ngoại chi nhánh cửa hàng chế biến đơn (Linn Sushi Sentrum)';
COMMENT ON COLUMN orders.user_id IS 'Khóa ngoại khách hàng đăng nhập (hoặc null nếu khách vãng lai)';
COMMENT ON COLUMN orders.order_code IS 'Mã hiển thị trên hóa đơn và e-receipt (ORD-20261105-0891)';
COMMENT ON COLUMN orders.pickup_method IS 'Hình thức nhận đồ: COUNTER (tại quầy), TABLE (tại bàn)';
COMMENT ON COLUMN orders.pickup_time_type IS 'Thời gian lấy đồ: ASAP (càng sớm càng tốt), SCHEDULED (hẹn giờ)';
COMMENT ON COLUMN orders.scheduled_pickup_at IS 'Khung giờ hẹn lấy món nếu chọn SCHEDULED';
COMMENT ON COLUMN orders.table_number IS 'Số bàn ăn nếu khách chọn nhận tại bàn (Table 05)';
COMMENT ON COLUMN orders.pickup_person_name IS 'Tên người đến lấy đồ (David Nguyen)';
COMMENT ON COLUMN orders.pickup_person_phone IS 'Số điện thoại người lấy đồ để tích điểm (+4797362748)';
COMMENT ON COLUMN orders.subtotal_amount IS 'Tiền hàng gốc trước giảm giá (267.00 Kr)';
COMMENT ON COLUMN orders.discount_amount IS 'Số tiền được giảm trừ từ voucher áp dụng (100.00 Kr hoặc 0.00)';
COMMENT ON COLUMN orders.tip_amount IS 'Tiền boa tip cho nhân viên phục vụ (+10.00 Kr)';
COMMENT ON COLUMN orders.vat_amount IS 'Tiền thuế GTGT VAT đã bao gồm trong tổng tiền';
COMMENT ON COLUMN orders.total_amount IS 'Tổng số tiền thanh toán thực tế: subtotal - discount + tip (277.00 Kr)';
COMMENT ON COLUMN orders.points_earned IS 'Số điểm tích được từ đơn hàng hiển thị trên UI (+170 points)';
COMMENT ON COLUMN orders.status IS 'Trạng thái đơn: PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED';
COMMENT ON COLUMN orders.payment_status IS 'Trạng thái thanh toán: UNPAID, PAID, REFUNDED';
COMMENT ON COLUMN orders.payment_method IS 'Phương thức thanh toán: CARD, VIPPS, APPLE_PAY, COUNTER_CASH';
COMMENT ON COLUMN orders.order_notes IS 'Ghi chú đặc biệt của khách (Special request, allergies, etc)';
COMMENT ON COLUMN orders.applied_voucher_id IS 'Khóa ngoại voucher được áp dụng cho đơn này';
COMMENT ON COLUMN orders.created_at IS 'Thời điểm bấm Pay đặt hàng';
COMMENT ON COLUMN orders.completed_at IS 'Thời điểm hoàn tất đơn hàng';

CREATE INDEX idx_orders_user_time ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_store_status ON orders(store_id, status, created_at DESC);

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

COMMENT ON TABLE order_items IS 'Chi tiết các món ăn, số lượng và đơn giá được đặt trong đơn hàng';
COMMENT ON COLUMN order_items.id IS 'Khóa chính dòng món ăn trong đơn';
COMMENT ON COLUMN order_items.order_id IS 'Khóa ngoại liên kết tới đơn hàng';
COMMENT ON COLUMN order_items.product_id IS 'Khóa ngoại món ăn gốc';
COMMENT ON COLUMN order_items.product_name IS 'Tên món ăn tại thời điểm đặt (Aurora, Tuna Maki)';
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

COMMENT ON TABLE order_item_modifiers IS 'Topping, size, mức ngọt, đá đã chọn kèm theo từng món ăn';
COMMENT ON COLUMN order_item_modifiers.id IS 'Khóa chính tùy chọn modifier';
COMMENT ON COLUMN order_item_modifiers.order_item_id IS 'Khóa ngoại thuộc món ăn nào trong đơn';
COMMENT ON COLUMN order_item_modifiers.modifier_group_name IS 'Tên nhóm tùy chọn (Size, Topping, Sweetness Level, Ice Level)';
COMMENT ON COLUMN order_item_modifiers.modifier_name IS 'Tên tùy chọn đã chọn (Size L, Mango Popping, Less ice)';
COMMENT ON COLUMN order_item_modifiers.extra_price IS 'Giá phụ thu tại thời điểm đặt';

CREATE INDEX idx_item_modifiers_item ON order_item_modifiers(order_item_id);

