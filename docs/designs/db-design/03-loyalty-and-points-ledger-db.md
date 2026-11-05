# Phân hệ 03: Hạng Thành Viên & Sổ Cái Điểm Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Bậc Thẻ Thành Viên, Kế Toán Điểm Bất Biến & Khiếu Nại Điểm Sót**  
> **Ánh xạ Frontend:** `ProfilePage.tsx`, `TiersModal.tsx`, `MissingPointsModal.tsx`  
> **File Migration:** `V3__create_loyalty_and_tiers.sql`, `V4__create_points_ledger.sql`  
> **JPA Entities:** `TierDefinitionJpaEntity.java`, `MemberProfileJpaEntity.java`, `PointsLedgerJpaEntity.java`, `SupportClaimJpaEntity.java`

---

### Bảng 3.1: `tier_definitions` (Định nghĩa 6 Hạng Thẻ Thành viên)
Lưu cấu hình chuẩn của 6 bậc thành viên trong Figma Nhóm 04 (New $\rightarrow$ Silver $\rightarrow$ Gold $\rightarrow$ Platinum $\rightarrow$ Diamond $\rightarrow$ Ruby).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính cấu hình bậc |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu sở hữu chính sách |
| `tier_name` | `VARCHAR(32)` | NO | | | Tên cấp bậc: `New`, `Silver`, `Gold`, `Platinum`, `Diamond`, `Ruby` |
| `tier_order` | `INT` | NO | | | Thứ tự thăng tiến: 1 (New), 2 (Silver), ..., 6 (Ruby) |
| `min_points_required`| `INT` | NO | `0` | | Mốc điểm tích lũy tối thiểu để đạt hạng (0, 200, 400, 1000, 2000, 4000) |
| `theme_color_hex` | `VARCHAR(16)`| NO | | | Mã màu thẻ Figma: New (`#1EB759`), Silver (`#00A3FF`), Gold (`#F5B800`), Platinum (`#FF5B4D`), Diamond (`#7C3AED`), Ruby (`#D9148E`) |
| `earn_points_rate` | `NUMERIC(6,4)`| NO | `1.0000` | | Hệ số nhân tích điểm (ví dụ: Silver x1.0, Diamond x1.5, Ruby x2.0) |
| `benefits_summary` | `JSONB` | YES | `'[]'` | | Danh sách đặc quyền của hạng thẻ lưu dạng JSON |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo cấu hình |

#### Indexes & Constraints
- `uq_tier_tenant_name`: UNIQUE (`tenant_id`, `tier_name`)
- `uq_tier_tenant_order`: UNIQUE (`tenant_id`, `tier_order`)

---

### Bảng 3.2: `member_profiles` (Hồ sơ Thành viên & Thẻ Khách hàng)
Lưu trữ thông tin điểm và cấp bậc hiện tại của khách (Figma 04.01 - 04.12: David Nguyen - Hạng Silver, 120 điểm).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính hồ sơ thành viên |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu nào |
| `user_id` | `UUID` | NO | | FK -> `users(id)` ON DELETE CASCADE, UK | Tài khoản người dùng liên kết (1-1) |
| `current_tier_id` | `UUID` | NO | | FK -> `tier_definitions(id)` | Cấp bậc hiện tại của thành viên |
| `current_points` | `INT` | NO | `0` | | Số điểm khả dụng có thể dùng để đổi quà/voucher (120 điểm) |
| `lifetime_points`| `INT` | NO | `0` | | Tổng điểm tích luỹ trọn đời để xét hạng thẻ (300 điểm) |
| `date_of_birth` | `DATE` | YES | | | Ngày sinh nhật khách hàng (dùng để tặng quà sinh nhật: `23/08/2000`) |
| `gender` | `VARCHAR(16)` | YES | | | Giới tính: `MALE`, `FEMALE`, `OTHER` |
| `avatar_url` | `TEXT` | YES | | | Đường dẫn ảnh đại diện avatar |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian mở thẻ thành viên |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm cập nhật hồ sơ gần nhất |

#### Indexes & Constraints
- `uq_member_profiles_user`: UNIQUE (`user_id`)
- `idx_member_profiles_tier`: (`tenant_id`, `current_tier_id`)

---

### Bảng 3.3: `points_ledger` (Sổ cái Biến động Điểm thưởng - IMMUTABLE)
**Bảng quan trọng nhất của hệ thống Loyalty:** Lưu lại toàn bộ lịch sử biến động điểm bất biến, không thể sửa xóa (Audit Log chuẩn ngân hàng).

> **Quy tắc Nghiệp vụ:** Tuyệt đối không bao giờ chạy lệnh `UPDATE points_ledger`. Bảng sử dụng khóa chính kết hợp `(id, created_at)` để đáp ứng chuẩn PostgreSQL Declarative RANGE Partitioning theo năm.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK (1/2) | Khóa chính của dòng sổ cái |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | PK (2/2) | Dấu thời gian ghi nhận bất biến (Partition Key) |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu |
| `member_id` | `UUID` | NO | | FK -> `member_profiles(id)` ON DELETE RESTRICT | Thành viên nhận biến động điểm |
| `transaction_type`| `VARCHAR(32)`| NO | | | Loại biến động: `ORDER_EARN` (cộng điểm từ đơn), `VOUCHER_REDEEM` (trừ điểm đổi mã), `GIFT_REDEEM` (trừ điểm đổi quà), `TIER_BONUS` (thưởng thăng hạng), `ADMIN_ADJUST` (điều chỉnh tay), `CLAIM_APPROVED` (duyệt khiếu nại), `KIOSK_EARN` (tích điểm tại quầy) |
| `points_delta` | `INT` | NO | | | Số điểm biến động: số dương là cộng (`+170`), số âm là trừ (`-100`) |
| `balance_after` | `INT` | NO | | | Số dư điểm khả dụng ngay sau khi thực hiện giao dịch này |
| `reference_type` | `VARCHAR(32)` | YES | | | Bảng tham chiếu liên kết: `ORDERS`, `USER_VOUCHERS`, `SUPPORT_CLAIMS` |
| `reference_id` | `VARCHAR(64)` | YES | | | Mã ID tham chiếu của đơn hàng hoặc voucher |
| `description` | `VARCHAR(255)`| NO | | | Diễn giải tiếng Việt ngắn gọn (ví dụ: `Tích điểm từ đơn hàng ORD-001`, `Đổi Voucher Name 100 NOK`) |

#### Indexes & Constraints
- `pk_points_ledger`: PRIMARY KEY (`id`, `created_at`)
- `idx_points_ledger_member_time`: (`member_id`, `created_at` DESC)
- `idx_points_ledger_ref`: (`reference_type`, `reference_id`)
- `idx_points_ledger_tenant`: (`tenant_id`, `created_at` DESC)

---

### Bảng 3.4: `support_claims` (Khiếu nại Thiếu điểm - Claim Missing Points)
Phục vụ màn hình hỗ trợ khiếu nại điểm đơn hàng tại quầy (Figma Nhóm 04 & 05: Missing Points Claim).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính của ticket khiếu nại |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu |
| `member_id` | `UUID` | NO | | FK -> `member_profiles(id)` | Thành viên gửi khiếu nại |
| `receipt_code` | `VARCHAR(64)` | NO | | | Mã hóa đơn in trên phiếu tính tiền mà khách bị thiếu điểm |
| `order_date` | `DATE` | NO | | | Ngày mua hàng trên hóa đơn |
| `bill_amount` | `NUMERIC(12,2)`| NO | | | Số tiền ghi trên hóa đơn |
| `bill_photo_url`| `TEXT` | YES | | | Đường dẫn ảnh chụp hóa đơn khách upload đính kèm |
| `status` | `VARCHAR(32)` | NO | `'PENDING'` | | Trạng thái duyệt: `PENDING`, `APPROVED`, `REJECTED` |
| `points_awarded`| `INT` | YES | | | Số điểm được cộng bù sau khi admin phê duyệt |
| `reviewer_note` | `TEXT` | YES | | | Ghi chú của quản lý khi từ chối hoặc phê duyệt |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo yêu cầu |
| `resolved_at` | `TIMESTAMPTZ` | YES | | | Thời gian quản lý xử lý yêu cầu |

#### Indexes & Constraints
- `idx_support_claims_member`: (`member_id`, `status`)
- `idx_support_claims_tenant_pending`: (`tenant_id`, `status`) WHERE `status = 'PENDING'`
