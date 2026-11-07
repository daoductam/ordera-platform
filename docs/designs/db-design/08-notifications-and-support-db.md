# Phân hệ 08: Thông Báo & Hỗ Trợ Khách Hàng Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Hòm Thư Đa Danh Mục (Messages & News), Quản lý Trạng Thái Đã Đọc & Phiếu Yêu Cầu Hỗ Trợ Điểm Thiếu**  
> **Ánh xạ Frontend:** `NotificationListView.tsx`, `NotificationDetailModal.tsx`, `MissingPointsBanner.tsx`  
> **File Migration:** `V12__create_notifications_and_support.sql`  

---

### Bảng 8.1: `system_notifications` (Kho Thông Báo Hệ Thống & Chiến Dịch)
Quản lý các tin tức, sự kiện và hướng dẫn do Admin phát hành (Figma Nhóm 05: Messages & News).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính thông báo |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE CASCADE | Thuộc thương hiệu nào |
| `category` | `VARCHAR(32)` | NO | `'Messages'` | | Tab hiển thị trên UI: `Messages` (tin nhắn), `News` (tin tức) |
| `type` | `VARCHAR(32)` | NO | `'system_upgrade'` | | Loại nội dung: `missing_points`, `system_upgrade`, `promo_gift`, `order_status` |
| `title` | `VARCHAR(255)` | NO | | | Tiêu đề thông báo (vd: *Missing Your Points?*) |
| `summary` | `TEXT` | NO | | | Đoạn trích ngắn hiển thị ngoài danh sách |
| `content` | `TEXT` | NO | | | Nội dung chi tiết thông báo hiển thị trong Modal (Figma `05.03`) |
| `banner_image_url` | `TEXT` | YES | | | Ảnh banner minh họa lớn |
| `action_label` | `VARCHAR(64)` | YES | | | Nhãn nút hành động (ví dụ: *Learn more*, *Xem chi tiết*) |
| `action_url` | `TEXT` | YES | | | Đường dẫn điều hướng khi bấm nút |
| `has_support_form` | `BOOLEAN` | NO | `false` | | Hiển thị nút bấm `[Submit Form]` hỗ trợ ở cuối thông báo không |
| `support_email` | `VARCHAR(255)` | YES | | | Email liên hệ hỗ trợ hiển thị trong bài (vd: `info@ordera.no`) |
| `support_phone` | `VARCHAR(32)` | YES | | | Hotline hỗ trợ hiển thị trong bài (vd: `+4797362748`) |
| `is_active` | `BOOLEAN` | NO | `true` | | Thông báo còn hiệu lực hay đã ẩn |
| `expires_at` | `TIMESTAMPTZ` | YES | | | Hạn hiển thị thông báo |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm đăng thông báo |

#### Indexes & Constraints
- `idx_notif_tenant_category`: (`tenant_id`, `category`, `is_active`, `created_at` DESC)

---

### Bảng 8.2: `user_notifications` (Trạng Thái Hộp Thư Của Hội Viên)
Lưu vết trạng thái đã đọc/chưa đọc của từng khách hàng đối với từng thông báo (Figma `05.01`, `05.02`).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính bản ghi đọc tin |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE CASCADE | Thuộc thương hiệu |
| `user_id` | `UUID` | NO | | FK -> `users(id)` ON DELETE CASCADE | Khách hàng nhận thông báo |
| `notification_id` | `UUID` | NO | | FK -> `system_notifications(id)` ON DELETE CASCADE | Thông báo liên kết |
| `is_read` | `BOOLEAN` | NO | `false` | | Trạng thái đã đọc (true) hay chưa đọc (false) |
| `read_at` | `TIMESTAMPTZ` | YES | | | Thời điểm khách mở đọc tin |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm thông báo được chuyển vào hộp thư user |

#### Indexes & Constraints
- `uq_user_notifications`: UNIQUE (`tenant_id`, `user_id`, `notification_id`)
- `idx_user_notif_unread`: (`tenant_id`, `user_id`, `is_read`)

---

### Bảng 8.3: `support_inquiries` (Phiếu Yêu Cầu Hỗ Trợ Tra Cứu Điểm Thiếu)
Lưu trữ thông tin khách hàng gửi qua Modal `05.03` khi khiếu nại thất lạc điểm cũ sau đợt chuyển đổi hệ thống.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính phiếu hỗ trợ |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` ON DELETE CASCADE | Phân lập theo thương hiệu |
| `ticket_code` | `VARCHAR(32)` | NO | | UK | Mã phiếu đối soát (ví dụ: `INQ-20250317-009`) |
| `user_id` | `UUID` | YES | | FK -> `users(id)` ON DELETE SET NULL | Khách hàng đăng nhập (hoặc null nếu chưa đăng nhập) |
| `email` | `VARCHAR(255)` | NO | | | Địa chỉ Email khách để CSKH liên hệ phản hồi |
| `phone_number` | `VARCHAR(32)` | NO | | | Số điện thoại đã tích điểm trên hệ thống cũ |
| `receipt_code` | `VARCHAR(64)` | YES | | | Mã hóa đơn gần nhất (nếu có, ví dụ: `RCP-907`) |
| `order_date` | `DATE` | YES | | | Ngày mua hàng trên biên lai cũ |
| `bill_amount` | `NUMERIC(12,2)`| YES | | | Giá trị đơn hàng cũ đã thanh toán (NOK) |
| `note` | `TEXT` | NO | | | Nội dung chi tiết khách cần hỗ trợ kiểm tra |
| `receipt_photo_url`| `TEXT` | YES | | | Đường dẫn ảnh chụp biên nhận làm bằng chứng |
| `status` | `VARCHAR(32)` | NO | `'PENDING'` | | Trạng thái xử lý: `PENDING`, `IN_REVIEW`, `RESOLVED`, `REJECTED` |
| `admin_notes` | `TEXT` | YES | | | Ghi chú xử lý của nhân viên CSKH/Admin |
| `resolved_at` | `TIMESTAMPTZ` | YES | | | Thời điểm xử lý xong phiếu |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm gửi phiếu |

#### Indexes & Constraints
- `uq_support_inquiries_code`: UNIQUE (`tenant_id`, `ticket_code`)
- `idx_support_inquiries_status`: (`tenant_id`, `status`, `created_at` DESC)

---

## Tham Khảo DDL Flyway Mở Rộng (Migration Script Reference)

```sql
-- Flyway Script: V12__create_notifications_and_support.sql

-- 1. Bảng system_notifications
CREATE TABLE system_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    category VARCHAR(32) NOT NULL DEFAULT 'Messages',
    type VARCHAR(32) NOT NULL DEFAULT 'system_upgrade',
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

CREATE INDEX idx_notif_tenant_category ON system_notifications (tenant_id, category, is_active, created_at DESC);

-- 2. Bảng user_notifications
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

CREATE INDEX idx_user_notif_unread ON user_notifications (tenant_id, user_id, is_read);

-- 3. Bảng support_inquiries
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
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    admin_notes TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_support_inquiries_code UNIQUE (tenant_id, ticket_code)
);

CREATE INDEX idx_support_inquiries_status ON support_inquiries (tenant_id, status, created_at DESC);
```

