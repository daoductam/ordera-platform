# Phân hệ 04: Hạng Thành viên & Sổ cái Điểm API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Hồ sơ Khách hàng, 6 Cấp bậc Thẻ (New $\rightarrow$ Ruby), Tiến độ Thăng hạng & Huy hiệu Thao tác (Profile & Loyalty Dashboard)**  
> **Ánh xạ Prototype:** `docs/prototype/4-ho-so-va-hang-thanh-vien/` (`04.01` đến `04.15`)  
> **Ánh xạ Frontend:** `frontend/src/pages/AccountPage.tsx`, `frontend/src/pages/TierPage.tsx`, `ProfileDetailView.tsx`, `MilestoneStepper.tsx`  
> **Bảng Database:** `member_profiles`, `tier_definitions`, `points_ledger` (Partitioned), `support_claims`

---

### Danh sách Endpoint trong Phân hệ 04

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 4.1 | `/api/v1/loyalty/members/me` | `GET` | Xem hồ sơ hội viên, điểm khả dụng/trọn đời, hạng thẻ & huy hiệu bộ đếm | `04.01`, `04.02`, `04.03` |
| 4.2 | `/api/v1/loyalty/members/me` | `PUT` | Cập nhật thông tin cá nhân (Họ tên, Email, Ngày sinh) | `04.08`, `04.09` |
| 4.3 | `/api/v1/loyalty/members/complete-profile` | `POST` | Hoàn thiện hồ sơ bổ sung sau khi xác thực OTP | `04.04`, `04.05` |
| 4.4 | `/api/v1/loyalty/members/me/avatar` | `POST` | Tải lên ảnh đại diện mới qua icon máy ảnh | `04.07` |
| 4.5 | `/api/v1/loyalty/tiers` | `GET` | Cấu hình 6 cấp bậc thẻ từ New đến Ruby (mốc điểm, hệ số, đặc quyền) | `04.10` đến `04.15` |
| 4.6 | `/api/v1/loyalty/members/me/points` | `GET` | Lịch sử sổ cái biến động điểm bất biến (`points_ledger`) | Quản lý điểm |
| 4.7 | `/api/v1/loyalty/claims` | `POST` | Gửi phiếu khiếu nại thiếu điểm hóa đơn kèm ảnh biên lai | `05.03` |

---

### API 4.1: Xem Hồ sơ & Thẻ Hội viên Tổng quan (Get Member Profile & Dashboard)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `getMyMemberProfile`
- **Mô tả:** Lấy toàn bộ thông tin tài khoản, điểm số, tiến trình thăng hạng và các bộ đếm số lượng ưu đãi/đơn hàng để render huy hiệu đỏ trên các ô tác vụ nhanh (`04.03`).
- **Màn hình Prototype:** `04.01`, `04.02`, `04.03`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Member profile retrieved successfully",
  "data": {
    "id": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "fullName": "David Nguyen",
    "phoneNumber": "+4797362748",
    "email": "info@ordera.no",
    "dateOfBirth": "2000-08-23",
    "avatarUrl": "https://storage.ordera.no/avatars/david-nguyen.jpg",
    "phoneVerified": true,
    "loyalty": {
      "currentTier": "Silver",
      "tierOrder": 2,
      "themeColorHex": "#00A3FF",
      "earnPointsRate": 1.2000,
      "currentPoints": 120,
      "lifetimePoints": 300,
      "pointsToNextTier": 100,
      "nextTierName": "Gold",
      "nextTierMinPoints": 400,
      "progressPercentage": 50.0
    },
    "counters": {
      "activeVouchersCount": 2,
      "activeGiftsCount": 1,
      "activeOrdersCount": 1,
      "unreadNotificationsCount": 1
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.2: Chỉnh sửa Hồ sơ Cá nhân (Update Profile)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `updateMemberProfile`
- **Mô tả:** Chỉnh sửa thông tin cá nhân. Nút `[Done]` trên giao diện `04.09` sẽ kích hoạt gọi API này.

#### 1. Request Body Schema
```json
{
  "fullName": "David Nguyen",
  "email": "info@ordera.no",
  "dateOfBirth": "2000-08-23"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Cập nhật thông tin cá nhân thành công",
  "data": {
    "id": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "fullName": "David Nguyen",
    "email": "info@ordera.no",
    "dateOfBirth": "2000-08-23",
    "updatedAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.3: Bổ sung Hoàn thiện Hồ sơ (Complete Profile)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/members/complete-profile`
- **Operation ID:** `completeProfile`
- **Mô tả:** Dành cho người dùng mới đăng nhập lần đầu cần cập nhật Họ tên và Email trên màn hình `04.04 Bổ sung thông tin`.

#### 1. Request Body Schema
```json
{
  "fullName": "David Nguyen",
  "email": "info@ordera.no",
  "dateOfBirth": "2000-08-23"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Hồ sơ của bạn đã được hoàn thiện",
  "data": {
    "isProfileComplete": true,
    "fullName": "David Nguyen",
    "email": "info@ordera.no"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.4: Tải lên Ảnh Đại diện (Upload Profile Avatar)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/members/me/avatar`
- **Operation ID:** `uploadAvatar`
- **Mô tả:** Tải lên ảnh avatar người dùng khi bấm vào icon máy ảnh tròn màu vàng tại `04.07`.
- **Content-Type:** `multipart/form-data`

#### 1. Request Body
- `file`: Binary file ảnh (JPG, PNG, WEBP, tối đa 5MB).

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Ảnh đại diện đã được cập nhật thành công",
  "data": {
    "avatarUrl": "https://storage.ordera.no/avatars/u1-avatar-2026.jpg"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.5: Cấu hình 6 Cấp Bậc Hạng Thẻ & Đặc quyền (Get Loyalty Tiers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/tiers`
- **Operation ID:** `getAllTierDefinitions`
- **Mô tả:** Lấy danh sách 6 hạng thẻ từ New đến Ruby kèm mã màu sắc hiển thị trên Prototype `04.10` đến `04.15`.

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "All 6 tier definitions retrieved successfully",
  "data": [
    {
      "tierName": "New",
      "tierOrder": 1,
      "minPointsRequired": 0,
      "themeColorHex": "#1EB759",
      "earnPointsRate": 1.0000,
      "benefits": ["Tích 1 điểm cho mỗi 10 Kr chi tiêu", "Tặng voucher chào mừng"]
    },
    {
      "tierName": "Silver",
      "tierOrder": 2,
      "minPointsRequired": 200,
      "themeColorHex": "#00A3FF",
      "earnPointsRate": 1.2000,
      "benefits": ["Hệ số tích điểm x1.2", "Ưu tiên nhận món tại quầy"]
    },
    {
      "tierName": "Gold",
      "tierOrder": 3,
      "minPointsRequired": 400,
      "themeColorHex": "#F5B800",
      "earnPointsRate": 1.5000,
      "benefits": ["Hệ số tích điểm x1.5", "Tặng quà bánh ngọt sinh nhật"]
    },
    {
      "tierName": "Platinum",
      "tierOrder": 4,
      "minPointsRequired": 1000,
      "themeColorHex": "#FF5B4D",
      "earnPointsRate": 1.8000,
      "benefits": ["Hệ số tích điểm x1.8", "Voucher 100 NOK thăng hạng"]
    },
    {
      "tierName": "Diamond",
      "tierOrder": 5,
      "minPointsRequired": 2000,
      "themeColorHex": "#7C3AED",
      "earnPointsRate": 2.0000,
      "benefits": ["Hệ số tích điểm x2.0", "Miễn phí nâng cỡ đồ uống"]
    },
    {
      "tierName": "Ruby",
      "tierOrder": 6,
      "minPointsRequired": 4000,
      "themeColorHex": "#D9148E",
      "earnPointsRate": 2.5000,
      "benefits": ["Hệ số tích điểm tối đa x2.5", "Tặng Cốc Tumbler Ruby phiên bản giới hạn", "Bàn ăn VIP đặt trước"]
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.6: Lịch sử Sổ cái Biến động Điểm (Points Ledger History)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me/points`
- **Operation ID:** `getMyPointsLedgerHistory`
- **Mô tả:** Truy vấn dữ liệu phân trang từ bảng phân vùng `points_ledger` theo thời gian giảm dần.

---

### API 4.7: Gửi Khiếu nại Thiếu Điểm Hóa đơn (Submit Support Claim)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/claims`
- **Operation ID:** `submitSupportClaim`
- **Mô tả:** Gửi mã hóa đơn và ảnh biên lai để khiếu nại cộng điểm khi nhân viên quên tích điểm tại quầy (`05.03`).
