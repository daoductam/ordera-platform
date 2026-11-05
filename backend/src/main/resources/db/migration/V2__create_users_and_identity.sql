-- ==============================================================================
-- V2__create_users_and_identity.sql
-- Phân hệ 2: Người dùng & Xác thực (Users, OTP Logs)
-- ==============================================================================

-- 1. Bảng users (Tài khoản Người dùng Hệ thống)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    phone_number VARCHAR(32) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255),
    is_phone_verified BOOLEAN NOT NULL DEFAULT false,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    role VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_tenant_phone UNIQUE (tenant_id, phone_number),
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

COMMENT ON TABLE users IS 'Lưu trữ thông tin tài khoản định danh người dùng đăng nhập';
COMMENT ON COLUMN users.id IS 'Khóa chính định danh tài khoản người dùng';
COMMENT ON COLUMN users.tenant_id IS 'Khóa ngoại thuộc thương hiệu/đại lý nào';
COMMENT ON COLUMN users.phone_number IS 'Số điện thoại quốc tế đầy đủ (+4797362748)';
COMMENT ON COLUMN users.email IS 'Địa chỉ email đăng ký (info@ordera.no, abytran@ordera.no)';
COMMENT ON COLUMN users.full_name IS 'Họ và tên khách hàng (David Nguyen, ABY TRAN)';
COMMENT ON COLUMN users.password_hash IS 'Mật khẩu băm BCrypt nếu khách sử dụng đăng nhập password';
COMMENT ON COLUMN users.is_phone_verified IS 'Trạng thái đã xác minh số điện thoại qua mã OTP';
COMMENT ON COLUMN users.is_email_verified IS 'Trạng thái đã xác minh email qua mã OTP';
COMMENT ON COLUMN users.role IS 'Phân quyền: CUSTOMER, STORE_STAFF, AGENCY_ADMIN, SYSTEM_SUPERADMIN';
COMMENT ON COLUMN users.status IS 'Trạng thái tài khoản: ACTIVE, LOCKED, DELETED';
COMMENT ON COLUMN users.last_login_at IS 'Thời điểm đăng nhập gần nhất';
COMMENT ON COLUMN users.created_at IS 'Thời điểm đăng ký tài khoản';
COMMENT ON COLUMN users.updated_at IS 'Thời điểm cập nhật thông tin gần nhất';

CREATE INDEX idx_users_tenant_role ON users(tenant_id, role, status);

-- 2. Bảng otp_audit_logs (Nhật ký Gửi & Xác thực Mã OTP)
CREATE TABLE otp_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    channel VARCHAR(16) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(128) NOT NULL,
    purpose VARCHAR(32) NOT NULL DEFAULT 'AUTH',
    attempt_count INT NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    ip_address VARCHAR(45),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE otp_audit_logs IS 'Nhật ký audit lưu vết các lượt sinh và xác thực mã OTP chống brute-force';
COMMENT ON COLUMN otp_audit_logs.id IS 'Khóa chính phiên xác thực OTP';
COMMENT ON COLUMN otp_audit_logs.tenant_id IS 'Định danh thương hiệu';
COMMENT ON COLUMN otp_audit_logs.channel IS 'Kênh gửi mã: SMS, EMAIL';
COMMENT ON COLUMN otp_audit_logs.recipient IS 'Số điện thoại hoặc email nhận mã';
COMMENT ON COLUMN otp_audit_logs.otp_hash IS 'Mã OTP 6 số đã băm SHA-256 (bảo mật)';
COMMENT ON COLUMN otp_audit_logs.purpose IS 'Mục đích gửi mã: REGISTER, LOGIN, PASSWORD_RESET';
COMMENT ON COLUMN otp_audit_logs.attempt_count IS 'Số lần nhập sai mã (tối đa 5 lần sẽ khóa phiên)';
COMMENT ON COLUMN otp_audit_logs.is_verified IS 'Trạng thái xác thực OTP thành công';
COMMENT ON COLUMN otp_audit_logs.ip_address IS 'Địa chỉ IP của client yêu cầu OTP';
COMMENT ON COLUMN otp_audit_logs.expires_at IS 'Thời điểm hết hạn của mã (+120 giây cooldown)';
COMMENT ON COLUMN otp_audit_logs.created_at IS 'Thời gian tạo phiên OTP';

CREATE INDEX idx_otp_recipient_time ON otp_audit_logs(recipient, created_at DESC);

