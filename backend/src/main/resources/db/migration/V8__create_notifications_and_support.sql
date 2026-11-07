-- ==============================================================================
-- V8__create_notifications_and_support.sql
-- Phân hệ 8: Thông Báo, Hòm Thư Đa Danh Mục & Hỗ Trợ Tra Cứu Điểm
-- Ánh xạ: docs/designs/db-design/08-notifications-and-support-db.md
-- ==============================================================================

-- 1. Bảng system_notifications (Kho Thông Báo Hệ Thống & Chiến Dịch)
CREATE TABLE system_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    category VARCHAR(32) NOT NULL DEFAULT 'Messages', -- Messages, News
    type VARCHAR(32) NOT NULL DEFAULT 'system_upgrade', -- missing_points, system_upgrade, promo_gift, order_status
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    content TEXT NOT NULL,
    banner_image_url TEXT,
    action_label VARCHAR(64),
    action_url TEXT,
    has_support_form BOOLEAN NOT NULL DEFAULT false,
    support_email VARCHAR(255),
    support_phone VARCHAR(32),
    is_active BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE system_notifications IS 'Quản lý các tin tức, sự kiện và thông báo do Admin phát hành (Figma 05: Messages & News)';
COMMENT ON COLUMN system_notifications.id IS 'Khóa chính thông báo';
COMMENT ON COLUMN system_notifications.tenant_id IS 'Khóa ngoại thuộc thương hiệu nào';
COMMENT ON COLUMN system_notifications.category IS 'Tab hiển thị trên UI: Messages (tin nhắn), News (tin tức)';
COMMENT ON COLUMN system_notifications.type IS 'Loại nội dung: missing_points, system_upgrade, promo_gift, order_status';
COMMENT ON COLUMN system_notifications.title IS 'Tiêu đề thông báo (ví dụ: Missing Your Points?)';
COMMENT ON COLUMN system_notifications.summary IS 'Đoạn trích ngắn hiển thị ngoài danh sách';
COMMENT ON COLUMN system_notifications.content IS 'Nội dung chi tiết thông báo hiển thị trong Modal (Figma 05.03)';
COMMENT ON COLUMN system_notifications.banner_image_url IS 'Ảnh banner minh họa lớn';
COMMENT ON COLUMN system_notifications.action_label IS 'Nhãn nút hành động (ví dụ: Learn more, Xem chi tiết)';
COMMENT ON COLUMN system_notifications.action_url IS 'Đường dẫn điều hướng khi bấm nút';
COMMENT ON COLUMN system_notifications.has_support_form IS 'Hiển thị nút bấm Submit Form hỗ trợ ở cuối thông báo không';
COMMENT ON COLUMN system_notifications.support_email IS 'Email liên hệ hỗ trợ hiển thị trong bài (vd: info@ordera.no)';
COMMENT ON COLUMN system_notifications.support_phone IS 'Hotline hỗ trợ hiển thị trong bài (vd: +4797362748)';
COMMENT ON COLUMN system_notifications.is_active IS 'Thông báo còn hiệu lực hay đã ẩn';
COMMENT ON COLUMN system_notifications.expires_at IS 'Hạn hiển thị thông báo';
COMMENT ON COLUMN system_notifications.created_at IS 'Thời điểm đăng thông báo';

CREATE INDEX idx_notif_tenant_category ON system_notifications (tenant_id, category, is_active, created_at DESC);

-- 2. Bảng user_notifications (Trạng Thái Hộp Thư Của Hội Viên)
CREATE TABLE user_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_id UUID NOT NULL REFERENCES system_notifications(id) ON DELETE CASCADE,
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_notifications UNIQUE (tenant_id, user_id, notification_id)
);

COMMENT ON TABLE user_notifications IS 'Lưu vết trạng thái đã đọc/chưa đọc của từng khách hàng đối với từng thông báo (Figma 05.01, 05.02)';
COMMENT ON COLUMN user_notifications.id IS 'Khóa chính bản ghi đọc tin';
COMMENT ON COLUMN user_notifications.tenant_id IS 'Khóa ngoại thuộc thương hiệu';
COMMENT ON COLUMN user_notifications.user_id IS 'Khóa ngoại khách hàng nhận thông báo';
COMMENT ON COLUMN user_notifications.notification_id IS 'Khóa ngoại thông báo liên kết';
COMMENT ON COLUMN user_notifications.is_read IS 'Trạng thái đã đọc (true) hay chưa đọc (false)';
COMMENT ON COLUMN user_notifications.read_at IS 'Thời điểm khách mở đọc tin';
COMMENT ON COLUMN user_notifications.created_at IS 'Thời điểm thông báo được chuyển vào hộp thư user';

CREATE INDEX idx_user_notif_unread ON user_notifications (tenant_id, user_id, is_read);

-- 3. Bảng support_inquiries (Phiếu Yêu Cầu Hỗ Trợ Tra Cứu Điểm Thiếu)
CREATE TABLE support_inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    ticket_code VARCHAR(32) NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    receipt_code VARCHAR(64),
    order_date DATE,
    bill_amount NUMERIC(12,2),
    note TEXT NOT NULL,
    receipt_photo_url TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_REVIEW, RESOLVED, REJECTED
    admin_notes TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_support_inquiries_code UNIQUE (tenant_id, ticket_code)
);

COMMENT ON TABLE support_inquiries IS 'Lưu trữ thông tin khách hàng gửi qua Modal 05.03 khi khiếu nại thất lạc điểm cũ';
COMMENT ON COLUMN support_inquiries.id IS 'Khóa chính phiếu hỗ trợ';
COMMENT ON COLUMN support_inquiries.tenant_id IS 'Khóa ngoại phân lập theo thương hiệu';
COMMENT ON COLUMN support_inquiries.ticket_code IS 'Mã phiếu đối soát (ví dụ: INQ-20250317-009)';
COMMENT ON COLUMN support_inquiries.user_id IS 'Khóa ngoại khách hàng đăng nhập (hoặc null nếu chưa đăng nhập)';
COMMENT ON COLUMN support_inquiries.email IS 'Địa chỉ Email khách để CSKH liên hệ phản hồi';
COMMENT ON COLUMN support_inquiries.phone_number IS 'Số điện thoại đã tích điểm trên hệ thống cũ';
COMMENT ON COLUMN support_inquiries.receipt_code IS 'Mã hóa đơn gần nhất (nếu có, ví dụ: RCP-907)';
COMMENT ON COLUMN support_inquiries.order_date IS 'Ngày mua hàng trên biên lai cũ';
COMMENT ON COLUMN support_inquiries.bill_amount IS 'Giá trị đơn hàng cũ đã thanh toán (NOK)';
COMMENT ON COLUMN support_inquiries.note IS 'Nội dung chi tiết khách cần hỗ trợ kiểm tra';
COMMENT ON COLUMN support_inquiries.receipt_photo_url IS 'Đường dẫn ảnh chụp biên nhận làm bằng chứng';
COMMENT ON COLUMN support_inquiries.status IS 'Trạng thái xử lý: PENDING, IN_REVIEW, RESOLVED, REJECTED';
COMMENT ON COLUMN support_inquiries.admin_notes IS 'Ghi chú xử lý của nhân viên CSKH/Admin';
COMMENT ON COLUMN support_inquiries.resolved_at IS 'Thời điểm xử lý xong phiếu';
COMMENT ON COLUMN support_inquiries.created_at IS 'Thời điểm gửi phiếu';

CREATE INDEX idx_support_inquiries_status ON support_inquiries (tenant_id, status, created_at DESC);

