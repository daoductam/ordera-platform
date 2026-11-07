-- ==============================================================================
-- V6__create_vouchers_and_rewards.sql
-- Phân hệ 5: Ví Voucher, Đổi Thưởng & Quà Hiện Vật Loyalty
-- Ánh xạ: docs/designs/db-design/05-rewards-and-vouchers-db.md
-- ==============================================================================

-- 1. Bảng vouchers (Kho Mã Giảm Giá & Ưu đãi)
CREATE TABLE vouchers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    code VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(16) NOT NULL, -- FIXED_AMOUNT, PERCENTAGE
    discount_value NUMERIC(12,2) NOT NULL,
    min_order_value NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    points_cost INT NOT NULL DEFAULT 0,
    valid_from TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_until TIMESTAMPTZ NOT NULL,
    max_issuance INT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_vouchers_tenant_code UNIQUE (tenant_id, code)
);

COMMENT ON TABLE vouchers IS 'Kho định nghĩa các chương trình ưu đãi, mã giảm giá và đổi thưởng (Figma 07: Voucher 100 NOK, Giảm -30%)';
COMMENT ON COLUMN vouchers.id IS 'Khóa chính mẫu voucher';
COMMENT ON COLUMN vouchers.tenant_id IS 'Khóa ngoại thuộc thương hiệu phát hành';
COMMENT ON COLUMN vouchers.code IS 'Mã voucher nhập tay (SAVE100, OFF30)';
COMMENT ON COLUMN vouchers.title IS 'Tiêu đề ưu đãi (Voucher Name)';
COMMENT ON COLUMN vouchers.description IS 'Mô tả chi tiết (Giảm 100 NOK giá trị cho Order của bạn)';
COMMENT ON COLUMN vouchers.discount_type IS 'Loại giảm giá: FIXED_AMOUNT (tiền mặt), PERCENTAGE (phần trăm)';
COMMENT ON COLUMN vouchers.discount_value IS 'Giá trị giảm: 100.00 (NOK) hoặc 30.00 (%)';
COMMENT ON COLUMN vouchers.min_order_value IS 'Giá trị đơn tối thiểu để áp dụng: 250.00 NOK hoặc 0.00 NOK';
COMMENT ON COLUMN vouchers.points_cost IS 'Số điểm cần để đổi trong Get More Rewards (100 Points, 1000 Points)';
COMMENT ON COLUMN vouchers.valid_from IS 'Thời điểm bắt đầu có hiệu lực';
COMMENT ON COLUMN vouchers.valid_until IS 'Thời điểm hết hạn hiệu lực (3 dec 2025)';
COMMENT ON COLUMN vouchers.max_issuance IS 'Tổng số lượng phát hành tối đa (null: không giới hạn)';
COMMENT ON COLUMN vouchers.is_active IS 'Chương trình voucher còn mở cho khách nhận/sử dụng không';
COMMENT ON COLUMN vouchers.created_at IS 'Thời gian tạo voucher';

CREATE INDEX idx_vouchers_active ON vouchers(tenant_id, is_active, valid_until);

-- 2. Bảng user_vouchers (Ví Voucher Khách hàng Sở hữu)
CREATE TABLE user_vouchers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    member_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE CASCADE,
    voucher_id UUID NOT NULL REFERENCES vouchers(id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, IN_USE, USED, EXPIRED
    applied_order_id UUID,
    redeemed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at TIMESTAMPTZ
);

COMMENT ON TABLE user_vouchers IS 'Ví lưu trữ các voucher mà từng khách hàng đang sở hữu trong My Rewards (Figma 07.03, 07.07, 07.08)';
COMMENT ON COLUMN user_vouchers.id IS 'Khóa chính bản ghi ví voucher';
COMMENT ON COLUMN user_vouchers.tenant_id IS 'Khóa ngoại định danh thương hiệu';
COMMENT ON COLUMN user_vouchers.member_id IS 'Khóa ngoại khách hàng sở hữu voucher';
COMMENT ON COLUMN user_vouchers.voucher_id IS 'Khóa ngoại liên kết tới mẫu voucher gốc';
COMMENT ON COLUMN user_vouchers.status IS 'Trạng thái voucher: AVAILABLE (sẵn sàng), IN_USE (đang gán giỏ hàng), USED (đã dùng), EXPIRED (hết hạn)';
COMMENT ON COLUMN user_vouchers.applied_order_id IS 'Mã đơn hàng đang áp dụng voucher này (Figma 07.08 In Use)';
COMMENT ON COLUMN user_vouchers.redeemed_at IS 'Thời điểm khách đổi điểm hoặc nhận voucher';
COMMENT ON COLUMN user_vouchers.used_at IS 'Thời điểm voucher được thanh toán xong cùng đơn hàng';

CREATE INDEX idx_user_vouchers_member_status ON user_vouchers(member_id, status);
CREATE INDEX idx_user_vouchers_applied_order ON user_vouchers(applied_order_id);

-- 3. Bảng loyalty_gifts (Kho Quà tặng Hiện vật Loyalty)
CREATE TABLE loyalty_gifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    image_url TEXT,
    points_cost INT NOT NULL DEFAULT 100,
    stock_quantity INT NOT NULL DEFAULT 10,
    tag VARCHAR(64),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE loyalty_gifts IS 'Danh mục quà tặng hiện vật đổi bằng điểm (ly giữ nhiệt tumbler, móc khóa) (Figma 07: Tumbler cup gift)';
COMMENT ON COLUMN loyalty_gifts.id IS 'Khóa chính quà tặng';
COMMENT ON COLUMN loyalty_gifts.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN loyalty_gifts.title IS 'Tên quà tặng (Gift Name - Cốc Tumbler Màu Hồng)';
COMMENT ON COLUMN loyalty_gifts.description IS 'Mô tả chi tiết và hướng dẫn nhận quà tại quầy của Admin';
COMMENT ON COLUMN loyalty_gifts.image_url IS 'Đường dẫn ảnh ly tumbler';
COMMENT ON COLUMN loyalty_gifts.points_cost IS 'Số điểm cần để đổi (100 Points)';
COMMENT ON COLUMN loyalty_gifts.stock_quantity IS 'Số lượng tồn kho còn lại (Quantity left: 10 Pieces)';
COMMENT ON COLUMN loyalty_gifts.tag IS 'Tag nhãn nổi bật trên giao diện (Hot Reward, Limited Edition - Prototype 07.04)';
COMMENT ON COLUMN loyalty_gifts.is_active IS 'Quà còn mở cho đổi thưởng không';
COMMENT ON COLUMN loyalty_gifts.created_at IS 'Thời gian tạo quà';

-- 4. Bảng user_gift_redemptions (Lịch sử Đổi & Nhận Quà Hiện vật)
CREATE TABLE user_gift_redemptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    member_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE RESTRICT,
    gift_id UUID NOT NULL REFERENCES loyalty_gifts(id) ON DELETE RESTRICT,
    points_spent INT NOT NULL,
    redemption_code VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, SELECTED, CLAIMED, CANCELLED
    applied_order_id UUID,
    claimed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE user_gift_redemptions IS 'Lưu trữ các lượt đổi quà hiện vật của khách để xuất trình tại quầy nhận quà';
COMMENT ON COLUMN user_gift_redemptions.id IS 'Khóa chính lượt đổi quà';
COMMENT ON COLUMN user_gift_redemptions.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN user_gift_redemptions.member_id IS 'Khóa ngoại thành viên đổi quà';
COMMENT ON COLUMN user_gift_redemptions.gift_id IS 'Khóa ngoại món quà được đổi';
COMMENT ON COLUMN user_gift_redemptions.points_spent IS 'Số điểm đã trừ trong tài khoản';
COMMENT ON COLUMN user_gift_redemptions.redemption_code IS 'Mã nhận quà 8 chữ số độc nhất cho nhân viên quầy quét';
COMMENT ON COLUMN user_gift_redemptions.status IS 'Trạng thái: PENDING (chờ nhận), SELECTED (đang gán đơn), CLAIMED (đã nhận), CANCELLED (hủy)';
COMMENT ON COLUMN user_gift_redemptions.applied_order_id IS 'Đơn hàng áp dụng quà tặng đi kèm (Figma 08.09, 08.10)';
COMMENT ON COLUMN user_gift_redemptions.claimed_at IS 'Thời điểm nhân viên quầy bàn giao quà cho khách';
COMMENT ON COLUMN user_gift_redemptions.created_at IS 'Thời điểm khách bấm đổi quà';

CREATE INDEX idx_gift_redemption_member ON user_gift_redemptions(member_id, status);
CREATE INDEX idx_gift_redemption_applied_order ON user_gift_redemptions(applied_order_id);
