-- ==============================================================================
-- V4__create_points_ledger.sql
-- Phân hệ 3 (Tiếp theo): Sổ cái Điểm Bất biến (Immutable Points Ledger)
-- Sử dụng kỹ thuật PostgreSQL Declarative RANGE Partitioning theo created_at
-- ==============================================================================

-- 1. Bảng chính points_ledger (Phân vùng theo thời gian)
CREATE TABLE points_ledger (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    member_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE RESTRICT,
    transaction_type VARCHAR(32) NOT NULL,
    points_delta INT NOT NULL,
    balance_after INT NOT NULL,
    reference_type VARCHAR(32),
    reference_id VARCHAR(64),
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE points_ledger IS 'Sổ cái kép bất biến ghi nhận mọi giao dịch cộng/trừ điểm thưởng theo chuẩn ngân hàng';
COMMENT ON COLUMN points_ledger.id IS 'Khóa chính dòng giao dịch sổ cái';
COMMENT ON COLUMN points_ledger.tenant_id IS 'Khóa ngoại định danh thương hiệu';
COMMENT ON COLUMN points_ledger.member_id IS 'Khóa ngoại định danh thành viên';
COMMENT ON COLUMN points_ledger.transaction_type IS 'Loại giao dịch: ORDER_EARN, VOUCHER_REDEEM, GIFT_REDEEM, TIER_BONUS, ADMIN_ADJUST, CLAIM_APPROVED';
COMMENT ON COLUMN points_ledger.points_delta IS 'Số điểm biến động: số dương (+170) là cộng, số âm (-100) là trừ';
COMMENT ON COLUMN points_ledger.balance_after IS 'Số dư điểm khả dụng ngay sau khi thực hiện giao dịch này';
COMMENT ON COLUMN points_ledger.reference_type IS 'Bảng liên kết nguồn: ORDERS, USER_VOUCHERS, SUPPORT_CLAIMS';
COMMENT ON COLUMN points_ledger.reference_id IS 'Mã ID tham chiếu của đơn hàng hoặc voucher';
COMMENT ON COLUMN points_ledger.description IS 'Diễn giải giao dịch hiển thị lịch sử cho khách hàng';
COMMENT ON COLUMN points_ledger.created_at IS 'Dấu thời gian phát sinh giao dịch bất biến';

-- 2. Tạo các phân vùng cụ thể (Partitions)
CREATE TABLE points_ledger_y2026 PARTITION OF points_ledger
    FOR VALUES FROM ('2026-01-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');

CREATE TABLE points_ledger_y2027 PARTITION OF points_ledger
    FOR VALUES FROM ('2027-01-01 00:00:00+00') TO ('2028-01-01 00:00:00+00');

CREATE TABLE points_ledger_default PARTITION OF points_ledger DEFAULT;

-- 3. Tạo Indexes trên bảng phân vùng
CREATE INDEX idx_points_ledger_member_time ON points_ledger(member_id, created_at DESC);
CREATE INDEX idx_points_ledger_ref ON points_ledger(reference_type, reference_id);
CREATE INDEX idx_points_ledger_tenant ON points_ledger(tenant_id, created_at DESC);

