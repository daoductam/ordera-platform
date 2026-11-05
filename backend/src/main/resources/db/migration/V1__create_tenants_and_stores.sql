-- ==============================================================================
-- V1__create_tenants_and_stores.sql
-- Phân hệ 1: Tenancy & Điểm bán (Stores, Tables)
-- ==============================================================================

-- 1. Kích hoạt Extension UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Bảng tenants (Thương hiệu / Đại lý)
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    logo_url TEXT,
    primary_color VARCHAR(16) DEFAULT '#F5B800',
    secondary_color VARCHAR(16) DEFAULT '#1E293B',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE tenants IS 'Bảng lưu trữ thông tin các thương hiệu F&B trong mô hình SaaS White-Label';
COMMENT ON COLUMN tenants.id IS 'Khóa chính định danh thương hiệu/đại lý';
COMMENT ON COLUMN tenants.code IS 'Mã định danh viết tắt của đại lý (linn_sushi, aby_tea)';
COMMENT ON COLUMN tenants.name IS 'Tên hiển thị thương hiệu (Agency Name, Linn Sushi)';
COMMENT ON COLUMN tenants.logo_url IS 'Đường dẫn URL ảnh logo thương hiệu';
COMMENT ON COLUMN tenants.primary_color IS 'Mã màu nhận diện chính (Hex) dùng để render giao diện động';
COMMENT ON COLUMN tenants.secondary_color IS 'Mã màu nhận diện phụ';
COMMENT ON COLUMN tenants.status IS 'Trạng thái hoạt động: ACTIVE, SUSPENDED, INACTIVE';
COMMENT ON COLUMN tenants.metadata IS 'Cấu hình mở rộng dạng JSON: hotline, chính sách điểm riêng';
COMMENT ON COLUMN tenants.created_at IS 'Thời điểm tạo thương hiệu';
COMMENT ON COLUMN tenants.updated_at IS 'Thời điểm cập nhật cấu hình lần cuối';

-- 3. Bảng stores (Danh sách Cửa hàng / Chi nhánh)
CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Oslo',
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    phone_number VARCHAR(32),
    opening_hours VARCHAR(100) DEFAULT '10:00 - 22:00',
    is_active BOOLEAN NOT NULL DEFAULT true,
    accepts_pickup BOOLEAN NOT NULL DEFAULT true,
    accepts_dinein BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE stores IS 'Danh sách các điểm bán (Outlets/Chi nhánh) trực thuộc thương hiệu';
COMMENT ON COLUMN stores.id IS 'Khóa chính định danh chi nhánh cửa hàng';
COMMENT ON COLUMN stores.tenant_id IS 'Khóa ngoại liên kết tới thương hiệu sở hữu';
COMMENT ON COLUMN stores.name IS 'Tên chi nhánh cửa hàng (Linn Sushi Sentrum)';
COMMENT ON COLUMN stores.address IS 'Địa chỉ chi nhánh (Prinsens gate 10C, 0153 Oslo)';
COMMENT ON COLUMN stores.city IS 'Thành phố đặt chi nhánh';
COMMENT ON COLUMN stores.latitude IS 'Tọa độ GPS vĩ độ để tính khoảng cách tìm cửa hàng gần nhất';
COMMENT ON COLUMN stores.longitude IS 'Tọa độ GPS kinh độ';
COMMENT ON COLUMN stores.phone_number IS 'Số điện thoại liên hệ cửa hàng';
COMMENT ON COLUMN stores.opening_hours IS 'Khung giờ mở cửa hoạt động trong ngày';
COMMENT ON COLUMN stores.is_active IS 'Trạng thái cửa hàng đang mở cửa hay đóng cửa';
COMMENT ON COLUMN stores.accepts_pickup IS 'Cửa hàng có chấp nhận đặt nhận tại quầy (Counter Pickup) không';
COMMENT ON COLUMN stores.accepts_dinein IS 'Cửa hàng có chấp nhận đặt ăn tại bàn (Table Dine-in) không';
COMMENT ON COLUMN stores.created_at IS 'Thời điểm tạo chi nhánh';
COMMENT ON COLUMN stores.updated_at IS 'Thời điểm cập nhật thông tin chi nhánh';

CREATE INDEX idx_stores_tenant_id ON stores(tenant_id);
CREATE INDEX idx_stores_active_city ON stores(tenant_id, city, is_active);

-- 4. Bảng store_tables (Quản lý Bàn ăn tại quán)
CREATE TABLE store_tables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    table_number VARCHAR(32) NOT NULL,
    seating_capacity INT NOT NULL DEFAULT 4,
    qr_code_token VARCHAR(128) UNIQUE,
    is_occupied BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE store_tables IS 'Quản lý danh sách bàn ăn tại cửa hàng phục vụ khách chọn nhận tại bàn';
COMMENT ON COLUMN store_tables.id IS 'Khóa chính định danh bàn ăn';
COMMENT ON COLUMN store_tables.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN store_tables.store_id IS 'Khóa ngoại thuộc chi nhánh cửa hàng nào';
COMMENT ON COLUMN store_tables.table_number IS 'Tên/Số bàn hiển thị (Table 01, VIP-02)';
COMMENT ON COLUMN store_tables.seating_capacity IS 'Sức chứa số lượng khách tối đa của bàn';
COMMENT ON COLUMN store_tables.qr_code_token IS 'Mã token QR dán trên mặt bàn để khách quét đặt trực tiếp';
COMMENT ON COLUMN store_tables.is_occupied IS 'Trạng thái bàn đang có khách ngồi hay bàn trống';
COMMENT ON COLUMN store_tables.created_at IS 'Thời điểm tạo bàn';

CREATE INDEX idx_store_tables_store ON store_tables(store_id, table_number);

