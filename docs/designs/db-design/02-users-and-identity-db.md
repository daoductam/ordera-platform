# Phân hệ 02: Người Dùng & Xác Thực Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Tài khoản Khách hàng, Phân quyền & Nhật ký Xác thực OTP**  
> **Ánh xạ Frontend:** `LoginPage.tsx`, `RegisterPage.tsx`, `ProfilePage.tsx`  
> **File Migration:** `V2__create_users_and_identity.sql`  
> **JPA Entities:** `UserJpaEntity.java`, `OtpAuditLogJpaEntity.java`

---

### Bảng 2.1: `users` (Tài khoản Người dùng Hệ thống)
Lưu trữ thông tin định danh gốc (Figma Nhóm 02 & 03: Đăng nhập & Đăng ký qua SĐT/Email).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính định danh tài khoản người dùng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Khách thuộc thương hiệu/đại lý nào |
| `phone_number` | `VARCHAR(32)` | NO | | | Số điện thoại quốc tế đầy đủ (ví dụ: `+4797362748`) |
| `email` | `VARCHAR(255)` | NO | | | Địa chỉ email đăng ký (ví dụ: `info@ordera.no`, `abytran@ordera.no`) |
| `full_name` | `VARCHAR(128)` | NO | | | Họ và tên khách hàng (ví dụ: `David Nguyen`, `ABY TRAN`) |
| `password_hash` | `VARCHAR(255)`| YES | | | Mật khẩu băm (BCrypt) nếu khách sử dụng đăng nhập password |
| `is_phone_verified`| `BOOLEAN` | NO | `false` | | Trạng thái đã xác minh số điện thoại qua OTP thành công |
| `is_email_verified`| `BOOLEAN` | NO | `false` | | Trạng thái đã xác minh email qua OTP thành công |
| `role` | `VARCHAR(32)` | NO | `'CUSTOMER'` | | Vai trò: `CUSTOMER`, `STORE_STAFF`, `AGENCY_ADMIN`, `SYSTEM_SUPERADMIN` |
| `status` | `VARCHAR(32)` | NO | `'ACTIVE'` | | Trạng thái tài khoản: `ACTIVE`, `LOCKED`, `DELETED` |
| `last_login_at`| `TIMESTAMPTZ` | YES | | | Thời điểm đăng nhập gần nhất |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm đăng ký tài khoản |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm cập nhật thông tin gần nhất |

#### Indexes & Constraints
- `uq_users_tenant_phone`: UNIQUE (`tenant_id`, `phone_number`) — Đảm bảo tính độc nhất của SĐT trong phạm vi một thương hiệu.
- `uq_users_tenant_email`: UNIQUE (`tenant_id`, `email`) — Đảm bảo tính độc nhất của Email trong phạm vi một thương hiệu.
- `idx_users_tenant_role`: (`tenant_id`, `role`, `status`)

---

### Bảng 2.2: `otp_audit_logs` (Nhật ký Xác thực Mã OTP)
Lưu vết các lượt gửi mã OTP (SMS/Email) để kiểm soát cooldown 120s và chống gian lận brute-force (Figma 03.03, 03.04).

> **Lưu ý:** Bộ nhớ đệm OTP chính TTL 120s chạy trên Redis (`auth:otp:{tenantId}:{recipient}`). Bảng CSDL này dùng để lưu Audit trail phục vụ tra cứu bảo mật và đối soát cước viễn thông SMS.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính phiên OTP |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu |
| `channel` | `VARCHAR(16)` | NO | | | Kênh gửi OTP: `SMS`, `EMAIL` |
| `recipient` | `VARCHAR(255)`| NO | | | Số điện thoại hoặc email nhận mã |
| `otp_hash` | `VARCHAR(128)`| NO | | | Mã OTP 6 số đã được băm SHA-256 (bảo mật không lưu plain text) |
| `purpose` | `VARCHAR(32)` | NO | `'AUTH'` | | Mục đích gửi: `REGISTER`, `LOGIN`, `PASSWORD_RESET` |
| `attempt_count` | `INT` | NO | `0` | | Số lần nhập sai mã (tối đa 5 lần sẽ khóa phiên) |
| `is_verified` | `BOOLEAN` | NO | `false` | | Trạng thái xác thực thành công |
| `ip_address` | `VARCHAR(45)` | YES | | | Địa chỉ IP của client yêu cầu OTP |
| `expires_at` | `TIMESTAMPTZ` | NO | | | Thời điểm hết hạn của mã (thường là +120 giây) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian sinh mã |

#### Indexes & Constraints
- `idx_otp_recipient_time`: (`recipient`, `created_at` DESC)
- `idx_otp_tenant_created`: (`tenant_id`, `created_at` DESC)

