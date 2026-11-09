-- Flyway Migration V11: Create Kiosk Devices Table and Indexes
-- Description: Supports Kiosk POS & Quick Accrual Terminals

CREATE TABLE IF NOT EXISTS kiosk_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    device_code VARCHAR(64) NOT NULL,
    device_name VARCHAR(128) NOT NULL,
    printer_status VARCHAR(32) NOT NULL DEFAULT 'OK',
    card_reader_status VARCHAR(32) NOT NULL DEFAULT 'ONLINE',
    idle_timeout_seconds INT NOT NULL DEFAULT 60,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_heartbeat_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_kiosk_devices_tenant_code UNIQUE (tenant_id, device_code)
);

CREATE INDEX IF NOT EXISTS idx_kiosk_devices_store ON kiosk_devices (store_id, is_active);

COMMENT ON TABLE kiosk_devices IS 'Quản lý các trạm máy tính Kiosk màn hình chạm toàn màn hình đặt tại quầy thu ngân';
COMMENT ON COLUMN kiosk_devices.id IS 'Khóa chính thiết bị Kiosk';
COMMENT ON COLUMN kiosk_devices.tenant_id IS 'Khóa ngoại định danh thương hiệu sở hữu';
COMMENT ON COLUMN kiosk_devices.store_id IS 'Khóa ngoại đặt tại chi nhánh cửa hàng nào';
COMMENT ON COLUMN kiosk_devices.device_code IS 'Mã định danh phần cứng thiết bị (ví dụ: kiosk-pos-sentrum-01)';
COMMENT ON COLUMN kiosk_devices.device_name IS 'Tên trạm hiển thị (ví dụ: Counter Touch Terminal 01)';
COMMENT ON COLUMN kiosk_devices.printer_status IS 'Tình trạng máy in bill nhiệt: OK, OUT_OF_PAPER, ERROR';
COMMENT ON COLUMN kiosk_devices.card_reader_status IS 'Tình trạng máy quẹt thẻ POS: ONLINE, OFFLINE, ERROR';
COMMENT ON COLUMN kiosk_devices.idle_timeout_seconds IS 'Thời gian đếm ngược tự động reset phiên (mặc định 60 giây)';
COMMENT ON COLUMN kiosk_devices.is_active IS 'Trạm có đang mở cho khách sử dụng hay tạm đóng';
COMMENT ON COLUMN kiosk_devices.last_heartbeat_at IS 'Thời điểm gửi tín hiệu ping sức khỏe thiết bị gần nhất';
COMMENT ON COLUMN kiosk_devices.created_at IS 'Thời điểm khởi tạo thiết bị Kiosk';

