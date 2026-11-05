-- ==============================================================================
-- V3__create_loyalty_and_tiers.sql
-- Phân hệ 3: Hạng Thành viên & Hồ sơ Khách hàng (Loyalty, Tiers, Support Claims)
-- ==============================================================================

-- 1. Bảng tier_definitions (Cấu hình 6 Hạng thẻ thành viên)
CREATE TABLE tier_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    tier_name VARCHAR(32) NOT NULL,
    tier_order INT NOT NULL,
    min_points_required INT NOT NULL DEFAULT 0,
    theme_color_hex VARCHAR(16) NOT NULL,
    earn_points_rate NUMERIC(6,4) NOT NULL DEFAULT 1.0000,
    benefits_summary JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tier_tenant_name UNIQUE (tenant_id, tier_name),
    CONSTRAINT uq_tier_tenant_order UNIQUE (tenant_id, tier_order)
);

COMMENT ON TABLE tier_definitions IS 'Cấu hình chuẩn 6 bậc thành viên: New, Silver, Gold, Platinum, Diamond, Ruby';
COMMENT ON COLUMN tier_definitions.id IS 'Khóa chính định danh cấp bậc';
COMMENT ON COLUMN tier_definitions.tenant_id IS 'Khóa ngoại thuộc chính sách thương hiệu nào';
COMMENT ON COLUMN tier_definitions.tier_name IS 'Tên cấp bậc: New, Silver, Gold, Platinum, Diamond, Ruby';
COMMENT ON COLUMN tier_definitions.tier_order IS 'Thứ tự thăng tiến bậc: 1 (New) đến 6 (Ruby)';
COMMENT ON COLUMN tier_definitions.min_points_required IS 'Mốc điểm tích luỹ trọn đời tối thiểu để đạt hạng (0, 200, 400, 1000, 2000, 4000)';
COMMENT ON COLUMN tier_definitions.theme_color_hex IS 'Mã màu nhận diện thẻ theo Figma: #1EB759, #00A3FF, #F5B800, #FF5B4D, #7C3AED, #D9148E';
COMMENT ON COLUMN tier_definitions.earn_points_rate IS 'Hệ số nhân tích điểm (ví dụ: x1.0, x1.5, x2.0)';
COMMENT ON COLUMN tier_definitions.benefits_summary IS 'Danh sách đặc quyền của hạng thẻ lưu dạng JSON array';
COMMENT ON COLUMN tier_definitions.created_at IS 'Thời gian tạo cấu hình bậc';

-- 2. Bảng member_profiles (Hồ sơ Khách hàng Thành viên)
CREATE TABLE member_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_tier_id UUID NOT NULL REFERENCES tier_definitions(id) ON DELETE RESTRICT,
    current_points INT NOT NULL DEFAULT 0,
    lifetime_points INT NOT NULL DEFAULT 0,
    date_of_birth DATE,
    gender VARCHAR(16),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE member_profiles IS 'Lưu trữ thông tin điểm và thẻ thành viên của khách hàng';
COMMENT ON COLUMN member_profiles.id IS 'Khóa chính hồ sơ thành viên';
COMMENT ON COLUMN member_profiles.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN member_profiles.user_id IS 'Khóa ngoại liên kết 1-1 với tài khoản đăng nhập users';
COMMENT ON COLUMN member_profiles.current_tier_id IS 'Khóa ngoại cấp bậc thành viên hiện tại';
COMMENT ON COLUMN member_profiles.current_points IS 'Số điểm khả dụng có thể dùng đổi voucher/quà (120 điểm)';
COMMENT ON COLUMN member_profiles.lifetime_points IS 'Tổng điểm tích luỹ trọn đời để xét mốc thăng hạng thẻ (300 điểm)';
COMMENT ON COLUMN member_profiles.date_of_birth IS 'Ngày sinh nhật khách hàng (23/08/2000)';
COMMENT ON COLUMN member_profiles.gender IS 'Giới tính: MALE, FEMALE, OTHER';
COMMENT ON COLUMN member_profiles.avatar_url IS 'Đường dẫn ảnh đại diện avatar';
COMMENT ON COLUMN member_profiles.created_at IS 'Thời điểm mở thẻ thành viên';
COMMENT ON COLUMN member_profiles.updated_at IS 'Thời điểm cập nhật hồ sơ gần nhất';

CREATE INDEX idx_member_profiles_tier ON member_profiles(tenant_id, current_tier_id);

-- 3. Bảng support_claims (Khiếu nại Thiếu điểm Hóa đơn)
CREATE TABLE support_claims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    member_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE RESTRICT,
    receipt_code VARCHAR(64) NOT NULL,
    order_date DATE NOT NULL,
    bill_amount NUMERIC(12,2) NOT NULL,
    bill_photo_url TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    points_awarded INT,
    reviewer_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);

COMMENT ON TABLE support_claims IS 'Yêu cầu khiếu nại cộng bù điểm cho hóa đơn mua hàng tại quầy bị sót điểm';
COMMENT ON COLUMN support_claims.id IS 'Khóa chính ticket khiếu nại';
COMMENT ON COLUMN support_claims.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN support_claims.member_id IS 'Khóa ngoại thành viên gửi yêu cầu';
COMMENT ON COLUMN support_claims.receipt_code IS 'Mã hóa đơn in trên phiếu tính tiền';
COMMENT ON COLUMN support_claims.order_date IS 'Ngày mua hàng in trên hóa đơn';
COMMENT ON COLUMN support_claims.bill_amount IS 'Số tiền ghi trên hóa đơn để tính điểm cộng bù';
COMMENT ON COLUMN support_claims.bill_photo_url IS 'Đường dẫn ảnh chụp hóa đơn khách tải lên';
COMMENT ON COLUMN support_claims.status IS 'Trạng thái xử lý: PENDING, APPROVED, REJECTED';
COMMENT ON COLUMN support_claims.points_awarded IS 'Số điểm được cộng bù sau khi quản lý phê duyệt';
COMMENT ON COLUMN support_claims.reviewer_note IS 'Ghi chú của quản lý khi duyệt hoặc từ chối';
COMMENT ON COLUMN support_claims.created_at IS 'Thời gian gửi khiếu nại';
COMMENT ON COLUMN support_claims.resolved_at IS 'Thời gian quản lý xử lý';

CREATE INDEX idx_support_claims_member ON support_claims(member_id, status);
CREATE INDEX idx_support_claims_tenant_pending ON support_claims(tenant_id, status) WHERE status = 'PENDING';

