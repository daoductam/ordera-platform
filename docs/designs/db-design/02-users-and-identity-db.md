# Phân hệ 02: Người Dùng & Xác Thực Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Tài khoản Khách hàng, Phân quyền, Nhật ký Xác thực OTP & Cấu Trúc Cache Redis**  
> **Ánh xạ Prototype:** `docs/prototype/2-dang-nhap-va-truy-cap-tai-khoan/` (`02.01` đến `02.07`), `docs/prototype/3-dang-ky-va-xac-thuc-thanh-vien/` (`03.01` đến `03.10`)  
> **Ánh xạ Frontend:** `LoginPage.tsx`, `RegisterPage.tsx`, `OtpVerificationModal.tsx`, `AuthFlowPage.tsx`  
> **File Migration:** `V2__create_users_and_identity.sql`  
> **JPA Entities:** `UserJpaEntity.java`, `OtpAuditLogJpaEntity.java`

---

### Bảng 2.1: `users` (Tài khoản Người dùng Hệ thống)
Lưu trữ thông tin định danh gốc (Figma Nhóm 02 & 03: Đăng nhập & Đăng ký qua SĐT/Email).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính định danh tài khoản người dùng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Khách thuộc thương hiệu/đại lý nào (Multi-tenancy isolation) |
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

> **Lưu ý Kiến trúc:** Bộ nhớ đệm OTP chính TTL 120s chạy trên Redis (`auth:otp:{tenantId}:{recipient}`). Bảng CSDL này dùng để lưu Audit trail phục vụ tra cứu bảo mật, giải quyết tranh chấp và đối soát cước viễn thông SMS.

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

---

### 2.3: Cấu Trúc Bộ Nhớ Đệm Redis & Cơ Chế Xác Thực (Redis Keys & Security Specifications)

Hệ thống sử dụng Redis 7 làm phân tầng tốc độ cao phục vụ các thao tác xác thực mật độ lớn, giảm tải cho PostgreSQL:

| Không gian Khóa Redis (Key Template) | Kiểu dữ liệu | TTL (Thời gian sống) | Mục đích nghiệp vụ |
|---|:---:|:---:|---|
| `auth:otp:{tenantId}:{recipient}` | `String` | 120 giây | Chuỗi OTP 6 số ngẫu nhiên lưu dạng plain text tạm thời trong Redis |
| `auth:otp:cooldown:{tenantId}:{recipient}` | `String` | 120 giây | Cờ ngăn chặn người dùng bấm gửi lại mã liên tục (chống spam SMS) |
| `auth:otp:attempts:{tenantId}:{recipient}` | `Integer` | 120 giây | Đếm số lần nhập sai mã; khi $\ge 5$ lần sẽ tự động hủy mã và trả về lỗi 429 |
| `auth:temp_token:{tenantId}:{tempToken}` | `Hash` | 900 giây (15 phút) | Lưu trạng thái SĐT đã xác thực nhưng chưa hoàn thiện hồ sơ (`isProfileComplete = false`), cho phép gọi API 4.3 `complete-profile` |
| `auth:blacklist:{jti}` | `String` | Thời gian sống còn lại của Access Token (tối đa 86.400s) | Danh sách đen JWT ID thu hồi ngay lập tức khi người dùng bấm Đăng xuất (Logout) |
| `auth:refresh:{tenantId}:{userId}:{tokenId}` | `String` | 30 ngày | Lưu định danh Refresh Token hợp lệ phục vụ xoay vòng token (Token Rotation) |

---

### 2.4: Mối Quan Hệ Giữa Định Danh (Identity) & Hồ Sơ Hội Viên (Member Loyalty Profile)

- **Nguyên tắc Phân tầng:** Bảng `users` chỉ chịu trách nhiệm về mặt **Định danh & Quyền truy cập (Authentication & Authorization)**: quản lý tài khoản đăng nhập, số điện thoại, email, vai trò (`role`) và tình trạng khóa/mở tài khoản (`status`).
- **Thuộc tính Khách hàng Thân thiết:** Mọi thông tin mở rộng của hội viên phục vụ nghiệp vụ Loyalty (Ngày sinh `date_of_birth` dùng để gửi voucher sinh nhật, Giới tính `gender`, Hạng thẻ thành viên `current_tier_id`, Điểm khả dụng `current_points`, Điểm trọn đời `lifetime_points`, và ảnh đại diện `avatar_url`) được lưu trữ tại bảng `member_profiles` (tham chiếu qua `user_id`) theo mô hình Clean Architecture trong `03-loyalty-and-points-ledger-db.md`.
