# Phân hệ 05: Ví Voucher & Đổi Thưởng Database Design
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Kho Voucher, Ví Thành Viên, Quà Hiện Vật & Lịch Sử Đổi Điểm**  
> **Ánh xạ Frontend:** `RewardsPage.tsx`, `VoucherSelectDrawer.tsx`  
> **File Migration:** `V6__create_vouchers_and_rewards.sql`  
> **JPA Entities:** `VoucherJpaEntity.java`, `UserVoucherJpaEntity.java`, `LoyaltyGiftJpaEntity.java`, `UserGiftRedemptionJpaEntity.java`

---

### Bảng 5.1: `vouchers` (Định nghĩa Mã Giảm Giá & Ưu Đãi)
Lưu cấu hình kho voucher (Figma Nhóm 07: Voucher 100 NOK, Giảm -30%).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính voucher template |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Định danh thương hiệu phát hành |
| `code` | `VARCHAR(64)` | NO | | | Mã khuyến mãi nhập tay (ví dụ: `SAVE100`, `OFF30`) |
| `title` | `VARCHAR(128)` | NO | | | Tên hiển thị (ví dụ: `Voucher Name`) |
| `description` | `VARCHAR(255)` | NO | | | Mô tả ưu đãi (ví dụ: `Giảm 100 NOK giá trị cho Order của bạn`) |
| `discount_type`| `VARCHAR(16)` | NO | | | Loại giảm giá: `FIXED_AMOUNT` (trừ tiền mặt), `PERCENTAGE` (trừ phần trăm) |
| `discount_value`| `NUMERIC(12,2)`| NO | | | Giá trị giảm: `100.00` (NOK) hoặc `30.00` (%) |
| `min_order_value`| `NUMERIC(12,2)`| NO | `0.00` | | Giá trị đơn hàng tối thiểu để được áp dụng (ví dụ: `250.00` NOK hoặc `0.00` NOK) |
| `points_cost` | `INT` | NO | `0` | | Số điểm cần để đổi voucher trong Get More Rewards (ví dụ: 100 Points, 1000 Points) |
| `valid_from` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian bắt đầu có hiệu lực |
| `valid_until` | `TIMESTAMPTZ` | NO | | | Thời gian hết hạn áp dụng (`3 dec 2025`) |
| `max_issuance` | `INT` | YES | | | Tổng số lượng voucher tối đa phát hành (null: không giới hạn) |
| `is_active` | `BOOLEAN` | NO | `true` | | Voucher còn mở cho đổi/sử dụng không |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo voucher |

#### Indexes & Constraints
- `uq_vouchers_tenant_code`: UNIQUE (`tenant_id`, `code`)
- `idx_vouchers_active`: (`tenant_id`, `is_active`, `valid_until`)

---

### Bảng 5.2: `user_vouchers` (Ví Voucher Sở hữu của Thành viên)
Quản lý các voucher mà từng khách hàng đang có trong ví (Figma 07.03, 07.07, 07.08).

> **Chu trình Trạng thái (Voucher State Machine):**  
> `AVAILABLE` $\xrightarrow{\text{Thêm vào giỏ}}$ `IN_USE` $\xrightarrow{\text{Thanh toán thành công}}$ `USED`  
> `IN_USE` $\xrightarrow{\text{Bỏ chọn / Hủy đơn}}$ `AVAILABLE`

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính bản ghi ví voucher |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu nào |
| `member_id` | `UUID` | NO | | FK -> `member_profiles(id)` ON DELETE CASCADE | Khách hàng sở hữu |
| `voucher_id` | `UUID` | NO | | FK -> `vouchers(id)` | Mẫu voucher được sở hữu |
| `status` | `VARCHAR(32)` | NO | `'AVAILABLE'` | | Trạng thái: `AVAILABLE` (khả dụng), `IN_USE` (đang được gán vào giỏ hàng), `USED` (đã sử dụng thanh toán xong), `EXPIRED` (hết hạn) |
| `applied_order_id`| `UUID` | YES | | | Đơn hàng đang áp dụng voucher này (Figma 07.08 In Use) |
| `redeemed_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm thành viên đổi điểm lấy voucher này |
| `used_at` | `TIMESTAMPTZ` | YES | | | Thời điểm voucher được thanh toán cùng đơn hàng |

#### Indexes & Constraints
- `idx_user_vouchers_member_status`: (`member_id`, `status`)
- `idx_user_vouchers_applied_order`: (`applied_order_id`)

---

### Bảng 5.3: `loyalty_gifts` (Kho Quà tặng Hiện vật Loyalty)
Quản lý quà tặng vật lý như cốc giữ nhiệt Tumbler (Figma Nhóm 07: Tumbler cup gift).

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính quà tặng |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `title` | `VARCHAR(128)` | NO | | | Tên quà (ví dụ: `Gift Name` - Cốc Tumbler Hồng) |
| `description` | `TEXT` | YES | | | Mô tả quà tặng và điều kiện nhận quà của Admin |
| `image_url` | `TEXT` | YES | | | Đường dẫn ảnh ly giữ nhiệt |
| `points_cost` | `INT` | NO | `100` | | Số điểm cần để đổi (100 Points) |
| `stock_quantity`| `INT` | NO | `10` | | Số lượng tồn kho hiện tại (`Quantity left: 10 Pieces`) |
| `is_active` | `BOOLEAN` | NO | `true` | | Quà còn trong chương trình đổi thưởng không |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời gian tạo quà |

---

### Bảng 5.4: `user_gift_redemptions` (Lịch sử Nhận Quà Hiện vật)
Lưu trữ việc thành viên đổi điểm nhận quà vật lý tại quầy thu ngân.

| Cột | Kiểu dữ liệu | Nullable | Mặc định | Ràng buộc | Chú thích chi tiết (Remark) |
|---|---|---|---|---|---|
| `id` | `UUID` | NO | `gen_random_uuid()` | PK | Khóa chính lượt đổi quà |
| `tenant_id` | `UUID` | NO | | FK -> `tenants(id)` | Thuộc thương hiệu |
| `member_id` | `UUID` | NO | | FK -> `member_profiles(id)` | Thành viên đổi quà |
| `gift_id` | `UUID` | NO | | FK -> `loyalty_gifts(id)` | Món quà được đổi |
| `points_spent` | `INT` | NO | | | Số điểm đã trừ |
| `redemption_code`| `VARCHAR(32)`| NO | | UK | Mã nhận quà 8 ký tự để khách đưa cho nhân viên quầy quét |
| `status` | `VARCHAR(32)` | NO | `'PENDING'` | | Trạng thái: `PENDING` (chờ nhận tại quầy), `CLAIMED` (đã nhận cốc), `CANCELLED` (hủy) |
| `claimed_at` | `TIMESTAMPTZ` | YES | | | Thời điểm nhân viên quầy xác nhận trao quà |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | | Thời điểm bấm Get now đổi quà |

#### Indexes & Constraints
- `uq_gift_redemption_code`: UNIQUE (`redemption_code`)
- `idx_gift_redemption_member`: (`member_id`, `status`)

