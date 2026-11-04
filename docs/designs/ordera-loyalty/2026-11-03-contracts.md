Started at:  2026/11/03 23:38:00
Finished at: 2026/11/03 23:42:00
Total time: 4 minutes
---

# Contracts: Ordera Loyalty Program 2.0

## Source
- **Architecture:** [2026-11-03-architecture.md](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-architecture.md)

---

## 1. API Contracts (RESTful Endpoints)

### 1.1 Chuẩn giao tiếp & Định dạng phản hồi chuẩn (ApiResponse Wrapper Pattern)
- **Base URL:** `/api/v1`
- **Authentication:** `Authorization: Bearer <jwt_access_token>` cho các endpoint người dùng.
- **Header Kiosk:** `X-Kiosk-Device-Id: <uuid>` cho phiên thiết bị tại quầy.

#### Chuẩn vỏ bọc thành công (Standard Success Envelope - `ApiResponse<T>`):
```json
{
  "success": true,
  "data": { ... },
  "message": "Thao tác thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

#### Chuẩn vỏ bọc danh sách có phân trang (`ApiPaginatedResponse<T>`):
```json
{
  "success": true,
  "data": [ ... ],
  "meta": {
    "page": 1,
    "limit": 20,
    "totalItems": 105,
    "totalPages": 6,
    "hasNext": true
  },
  "message": "Lấy danh sách thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

#### Chuẩn vỏ bọc lỗi (Standard Error Envelope - `ApiErrorResponse`):
```json
{
  "success": false,
  "error": {
    "code": "LOYALTY_INSUFFICIENT_POINTS",
    "message": "Khách hàng chỉ có 350 điểm khả dụng, không đủ để đổi voucher yêu cầu 500 điểm.",
    "details": null
  },
  "timestamp": "2026-11-03T23:44:00Z"
}
```

---

### 1.2 Danh sách Endpoints chi tiết

#### A. Auth & Kiosk Identity (`/api/v1/auth`)

| Phương thức | Đường dẫn (Path) | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `POST` | `/api/v1/auth/otp/send` | Gửi mã OTP 6 chữ số qua SMS hoặc Email | Public (Rate-limited) |
| `POST` | `/api/v1/auth/otp/verify` | Xác thực OTP & Cấp Token (Đăng nhập / Đăng ký) | Public |
| `POST` | `/api/v1/auth/kiosk/lookup` | Tra cứu nhanh thông tin thành viên qua SĐT tại Kiosk | Kiosk Token |
| `POST` | `/api/v1/auth/refresh-token` | Làm mới Access Token | Refresh Token |
| `POST` | `/api/v1/auth/logout` | Đăng xuất, hủy phiên làm việc (Web Mobile & Kiosk) | Bearer Token / Kiosk |

##### Schema mẫu `POST /api/v1/auth/otp/verify`:
```json
// Request Body:
{
  "identifier": "0987654321", // Phone hoặc Email
  "channel": "SMS",           // "SMS" | "EMAIL"
  "otp": "654321",
  "fullName": "Nguyễn Văn A"  // (Tùy chọn, điền khi đăng ký mới)
}

// Response Body (200 OK):
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "d7a8c9b2...",
    "expiresIn": 900,
    "user": {
      "id": "018f2d5e-7a4b-7c12-9b24-9b5a8e27c101",
      "phone": "0987654321",
      "email": null,
      "profile": {
        "fullName": "Nguyễn Văn A",
        "tier": "SILVER",
        "currentPoints": 750,
        "lifetimePoints": 1200
      }
    }
  },
  "message": "Xác thực OTP thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

---

#### B. Member Profile & Loyalty Tiers (`/api/v1/members` & `/api/v1/loyalty`)

| Phương thức | Đường dẫn (Path) | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `GET` | `/api/v1/members/me` | Lấy đầy đủ thông tin hồ sơ, hạng thẻ và tiến trình thăng hạng | Bearer Token |
| `PUT` | `/api/v1/members/me` | Cập nhật hồ sơ (Họ tên, ngày sinh, tùy chọn nhận tin) | Bearer Token |
| `GET` | `/api/v1/members/me/points-ledger` | Lấy lịch sử biến động điểm (Phân trang Cursor) | Bearer Token |
| `POST` | `/api/v1/members/me/claim-missing-points`| Gửi ticket khiếu nại "Thiếu điểm loyalty" | Bearer Token |
| `GET` | `/api/v1/loyalty/tiers` | Lấy danh sách 6 hạng thẻ & chi tiết đặc quyền (Perks) | Public |

##### Schema mẫu `GET /api/v1/members/me`:
```json
{
  "success": true,
  "data": {
    "profileId": "018f2d5e-7a4b-7c12-9b24-9b5a8e27c102",
    "fullName": "Nguyễn Văn A",
    "phone": "0987654321",
    "email": "vana@example.com",
    "dob": "1995-10-20",
    "currentPoints": 850,
    "lifetimePoints": 1450,
    "tier": {
      "code": "SILVER",
      "name": "Silver Member",
      "earnRate": 1.2,
      "minPoints": 500,
      "maxPoints": 1499,
      "nextTier": {
        "code": "GOLD",
        "name": "Gold Member",
        "pointsRequired": 1500,
        "pointsRemaining": 50,
        "progressPercentage": 95.0
      }
    }
  },
  "message": "Lấy thông tin thành viên thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

---

#### C. Rewards & Voucher Engine (`/api/v1/rewards`)

| Phương thức | Đường dẫn (Path) | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `GET` | `/api/v1/rewards/catalog` | Danh mục quà/voucher có thể đổi bằng điểm | Bearer Token / Public |
| `POST` | `/api/v1/rewards/redeem` | Đổi điểm khả dụng lấy Voucher | Bearer Token |
| `GET` | `/api/v1/rewards/my-vouchers` | Danh sách voucher sở hữu (`ACTIVE`, `USED`, `EXPIRED`) | Bearer Token |

##### Schema mẫu `POST /api/v1/rewards/redeem`:
```json
// Request Body:
{
  "voucherId": "018f2d5e-7a4b-7c12-9b24-9b5a8e27c201"
}

// Response Body (201 Created):
{
  "success": true,
  "data": {
    "userVoucherId": "018f2d5e-7a4b-7c12-9b24-9b5a8e27c301",
    "code": "ORD-50K-XYZ",
    "title": "Giảm 50.000đ cho đơn từ 150.000đ",
    "pointsDeducted": 500,
    "newPointsBalance": 350,
    "validTo": "2026-12-31T23:59:59Z",
    "status": "ACTIVE"
  },
  "message": "Đổi voucher thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

---

#### D. Stores, Catalog & Notifications (`/api/v1/stores` & `/api/v1/notifications`)

| Phương thức | Đường dẫn (Path) | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `GET` | `/api/v1/stores` | Danh sách cửa hàng (Khoảng cách, Takeaway / Delivery) | Public |
| `GET` | `/api/v1/stores/{storeId}/menu` | Danh mục & món ăn theo cửa hàng đã chọn | Public |
| `GET` | `/api/v1/notifications` | Danh sách thông báo (Biến động điểm, khuyến mãi, thăng hạng) | Bearer Token |

---

#### E. Orders, Cart & Checkout (`/api/v1/checkout` & `/api/v1/orders`)

| Phương thức | Đường dẫn (Path) | Mô tả | Yêu cầu Auth |
|---|---|---|---|
| `POST` | `/api/v1/checkout/validate-voucher`| Kiểm tra tính hợp lệ của voucher đối với giỏ hàng hiện tại | Optional Bearer |
| `POST` | `/api/v1/checkout/create-order` | Đặt hàng, áp voucher, tính điểm tích lũy & in biên nhận | Optional Bearer / Kiosk |
| `GET` | `/api/v1/orders` | Danh sách lịch sử đơn hàng (Phục vụ bấm Đặt lại / Re-order) | Bearer Token |
| `GET` | `/api/v1/orders/{orderId}` | Lấy chi tiết đơn hàng & hóa đơn điện tử (E-receipt) | Optional Bearer |
| `POST` | `/api/v1/orders/{orderId}/e-receipt`| Gửi hóa đơn điện tử qua Email / SMS | Optional Bearer |

##### Schema mẫu `POST /api/v1/checkout/validate-voucher`:
```json
// Request Body:
{
  "storeId": "018f2d5e-7a4b-7c12-9b24-9b5a8e27c001",
  "subtotal": 210000,
  "voucherCode": "ORD-50K-XYZ",
  "items": [
    { "productId": "PROD-01", "quantity": 2, "price": 80000 },
    { "productId": "PROD-02", "quantity": 1, "price": 50000 }
  ]
}

// Response Body (200 OK):
{
  "success": true,
  "data": {
    "isValid": true,
    "discountAmount": 50000,
    "finalTotal": 160000,
    "voucher": {
      "code": "ORD-50K-XYZ",
      "title": "Giảm 50.000đ",
      "discountType": "FIXED_AMOUNT"
    }
  },
  "message": "Áp dụng mã ưu đãi thành công",
  "timestamp": "2026-11-03T23:44:00Z"
}
```

---

## 2. Data Contracts (PostgreSQL DDL Schemas)

```sql
-- Kích hoạt extension UUID v7 (hoặc sử dụng hàm sinh UUID)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. BẢNG TIÊU CHUẨN CẤP BẬC HẠNG THÀNH VIÊN
CREATE TABLE tier_levels (
    code VARCHAR(32) PRIMARY KEY, -- NEW, SILVER, GOLD, PLATINUM, DIAMOND, RUBY
    name VARCHAR(64) NOT NULL,
    min_points INT NOT NULL,
    max_points INT NOT NULL,
    earn_rate NUMERIC(3, 2) NOT NULL DEFAULT 1.0, -- 1.0, 1.2, 1.5...
    color_hex VARCHAR(16) NOT NULL,               -- Phục vụ render thẻ đúng màu Figma
    perks JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed Data cho 6 Hạng thẻ chuẩn Figma Ordera Loyalty 2.0
INSERT INTO tier_levels (code, name, min_points, max_points, earn_rate, color_hex, perks) VALUES
('NEW', 'New Member', 0, 499, 1.00, '#64748B', '["Tích lũy 1% giá trị đơn hàng", "Ưu đãi thành viên mới"]'),
('SILVER', 'Silver Member', 500, 1499, 1.20, '#94A3B8', '["Tích lũy 1.2% giá trị đơn hàng", "Voucher sinh nhật 20k"]'),
('GOLD', 'Gold Member', 1500, 3999, 1.50, '#EAB308', '["Tích lũy 1.5% giá trị đơn hàng", "Voucher sinh nhật 50k", "Miễn phí đổi topping"]'),
('PLATINUM', 'Platinum Member', 4000, 7999, 2.00, '#06B6D4', '["Tích lũy 2.0% giá trị đơn hàng", "Quà tặng định kỳ hàng quý", "Ưu tiên phục vụ tại quầy"]'),
('DIAMOND', 'Diamond Member', 8000, 14999, 2.50, '#3B82F6', '["Tích lũy 2.5% giá trị đơn hàng", "Nhân đôi điểm vào ngày vàng", "Hotline VIP hỗ trợ riêng"]'),
('RUBY', 'Ruby VIP', 15000, 999999, 3.00, '#E11D48', '["Tích lũy 3.0% giá trị đơn hàng", "Tặng quà độc quyền thương hiệu", "Duy trì hạng trọn đời"]');

-- 2. BẢNG NGƯỜI DÙNG TÀI KHOẢN
CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(128) UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);

-- 3. BẢNG HỒ SƠ THÀNH VIÊN
CREATE TABLE member_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(128),
    dob DATE,
    tier_code VARCHAR(32) NOT NULL REFERENCES tier_levels(code) DEFAULT 'NEW',
    current_points INT NOT NULL DEFAULT 0 CHECK (current_points >= 0),
    lifetime_points INT NOT NULL DEFAULT 0 CHECK (lifetime_points >= 0),
    tier_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_profiles_user_id ON member_profiles(user_id);
CREATE INDEX idx_profiles_tier_code ON member_profiles(tier_code);

-- 4. BẢNG SỔ CÁI ĐIỂM THƯỞNG (POINTS LEDGER - BẤT BIẾN)
CREATE TABLE points_ledger (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE RESTRICT,
    transaction_type VARCHAR(32) NOT NULL, -- EARN, REDEEM, EXPIRE, ADJUST_ADD, ADJUST_SUB
    points_delta INT NOT NULL,             -- Dương khi cộng, âm khi trừ
    balance_after INT NOT NULL,            -- Số dư khả dụng sau giao dịch
    reference_id VARCHAR(128),             -- ID đơn hàng hoặc ID voucher
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_points_ledger_profile ON points_ledger(profile_id, created_at DESC);

-- 5. BẢNG VOUCHERS / ƯU ĐÃI
CREATE TABLE vouchers (
    id UUID PRIMARY KEY,
    code VARCHAR(64) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(32) NOT NULL, -- PERCENT, FIXED_AMOUNT, FREE_ITEM
    discount_value NUMERIC(12, 2) NOT NULL,
    points_cost INT NOT NULL DEFAULT 0,  -- 0 nếu là voucher phát miễn phí, > 0 nếu phải đổi điểm
    min_order_value NUMERIC(12, 2) DEFAULT 0,
    max_discount_amount NUMERIC(12, 2),
    total_quota INT,
    used_count INT NOT NULL DEFAULT 0,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. BẢNG VÍ VOUCHER THÀNH VIÊN
CREATE TABLE user_vouchers (
    id UUID PRIMARY KEY,
    voucher_id UUID NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES member_profiles(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, USED, EXPIRED
    used_in_order_id UUID,
    redeemed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_user_vouchers_profile ON user_vouchers(profile_id, status);

-- 7. BẢNG CỬA HÀNG
CREATE TABLE stores (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    phone VARCHAR(32),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 8. BẢNG ĐƠN HÀNG
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_code VARCHAR(32) UNIQUE NOT NULL, -- Ví dụ: #ORD-9821
    user_id UUID REFERENCES users(id),
    store_id UUID NOT NULL REFERENCES stores(id),
    order_type VARCHAR(32) NOT NULL DEFAULT 'TAKEAWAY', -- TAKEAWAY, DELIVERY, KIOSK
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',      -- PENDING, PAID, PREPARING, COMPLETED, CANCELLED
    subtotal NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tip_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL,
    points_earned INT NOT NULL DEFAULT 0,
    user_voucher_id UUID REFERENCES user_vouchers(id),
    customer_phone VARCHAR(20),
    customer_name VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_orders_user ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_store ON orders(store_id, created_at DESC);
```

---

## 3. Integration Contracts (Shared TypeScript Interfaces)

Các interface này được định nghĩa chuẩn để dùng chung cho Frontend React và Backend:

```typescript
// ==========================================
// 1. CHUẨN VỎ BỌC API (API RESPONSE ENVELOPES)
// ==========================================
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface PaginationMeta {
  page: number;
  limit: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev?: boolean;
}

export interface ApiPaginatedResponse<T> {
  success: boolean;
  data: T[];
  meta: PaginationMeta;
  message?: string;
  timestamp: string;
}

export interface ApiErrorDetail {
  field?: string;
  message: string;
}

export interface ApiErrorPayload {
  code: string;
  message: string;
  details?: ApiErrorDetail[] | null;
}

export interface ApiErrorResponse {
  success: false;
  error: ApiErrorPayload;
  timestamp: string;
}

// ==========================================
// 2. DTOs NGHIỆP VỤ LOYALTY & ORDERING
// ==========================================
// Định danh 6 Hạng thẻ
export type TierCode = 'NEW' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND' | 'RUBY';

export interface TierLevelDto {
  code: TierCode;
  name: string;
  minPoints: number;
  maxPoints: number;
  earnRate: number;
  colorHex: string;
  perks: string[];
}

export interface NextTierProgressDto {
  code: TierCode;
  name: string;
  pointsRequired: number;
  pointsRemaining: number;
  progressPercentage: number;
}

export interface MemberProfileDto {
  id: string;
  userId: string;
  fullName: string;
  phone: string;
  email?: string;
  dob?: string;
  currentPoints: number;
  lifetimePoints: number;
  tier: TierLevelDto;
  nextTier?: NextTierProgressDto;
}

export type PointsTransactionType = 'EARN' | 'REDEEM' | 'EXPIRE' | 'ADJUST_ADD' | 'ADJUST_SUB';

export interface PointsLedgerEntryDto {
  id: string;
  transactionType: PointsTransactionType;
  pointsDelta: number;
  balanceAfter: number;
  referenceId?: string;
  note: string;
  createdAt: string;
}

export type DiscountType = 'PERCENT' | 'FIXED_AMOUNT' | 'FREE_ITEM';

export interface VoucherDto {
  id: string;
  code: string;
  title: string;
  description?: string;
  discountType: DiscountType;
  discountValue: number;
  pointsCost: number;
  minOrderValue: number;
  validFrom: string;
  validTo: string;
  status?: 'ACTIVE' | 'USED' | 'EXPIRED';
}

export interface CartItemDto {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  image?: string;
  selectedOptions?: string[];
  note?: string;
}

export interface CheckoutRequestDto {
  storeId: string;
  orderType: 'TAKEAWAY' | 'DELIVERY' | 'KIOSK';
  items: CartItemDto[];
  voucherCode?: string;
  tipAmount?: number;
  customerPhone?: string;
  customerName?: string;
}

export interface OrderResultDto {
  orderId: string;
  orderCode: string;
  subtotal: number;
  discountAmount: number;
  tipAmount: number;
  totalAmount: number;
  pointsEarned: number;
  status: string;
  createdAt: string;
}
```

---

## 4. Domain Skills Referenced
- **`api-design`:** Chuẩn hóa URI, phân trang cursor-based cho Ledger, định dạng lỗi RFC 7807, bảo mật HTTP Header.
- **`database-design`:** DDL PostgreSQL hoàn chỉnh với kiểu dữ liệu chuẩn, ràng buộc toàn vẹn khóa ngoại, Index B-tree tối ưu truy vấn theo thời gian và bảng mã màu cho 6 Hạng thẻ.

