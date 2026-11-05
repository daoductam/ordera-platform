# Phân hệ 04: Hạng Thành viên & Sổ cái Điểm API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Hồ sơ Khách hàng, 6 Hạng thẻ (New $\rightarrow$ Ruby), Tiến độ Thăng hạng & Khiếu nại Thiếu điểm**  
> **Ánh xạ Frontend:** `frontend/src/pages/AccountPage.tsx`, `frontend/src/pages/TierPage.tsx`, `ProfileDetailView.tsx`, `MilestoneStepper.tsx`, `MissingPointsBanner.tsx`  
> **Bảng Database:** `member_profiles`, `tier_definitions`, `points_ledger` (Partitioned), `support_claims`

---

### API 4.1: Xem Hồ sơ & Thẻ Thành viên Ảo (Get Member Profile)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `getMyMemberProfile`
- **Mô tả:** Lấy toàn bộ thông tin thẻ thành viên của người dùng đăng nhập hiện tại: họ tên, SĐT, mã Barcode ảo quét tại quầy, điểm khả dụng (`currentPoints`), điểm trọn đời (`lifetimePoints`), và bậc thẻ hiện tại.
- **Frontend Gọi tại:** `AccountOverview.tsx`, `MemberCard.tsx`, `ProfileDetailView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Member profile retrieved successfully",
  "data": {
    "id": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "userId": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "fullName": "David Nguyen",
    "phoneNumber": "+4797362748",
    "email": "david.nguyen@example.com",
    "dateOfBirth": "2000-08-23",
    "gender": "MALE",
    "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
    "barcodeToken": "ORD-MEM-97362748",
    "currentPoints": 120,
    "lifetimePoints": 300,
    "currentTier": {
      "id": "t3eebc99-9c0b-4ef8-bb6d-6bb9bd380t03",
      "name": "Gold",
      "order": 3,
      "themeColorHex": "#F5B800",
      "earnPointsRate": 1.5000,
      "minPointsRequired": 400
    },
    "createdAt": "2026-11-05T00:00:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Chưa đăng nhập hoặc Token hết hạn (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Full authentication is required to access this resource.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.2: Cập nhật Thông tin Cá nhân (Update Profile)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `updateMemberProfile`
- **Mô tả:** Chỉnh sửa họ tên, email, ngày sinh hoặc đổi ảnh đại diện (Figma Screen `04.03 - Edit Profile`).
- **Frontend Gọi tại:** `EditProfileView.tsx`, `EditProfileDrawer.tsx`

#### 1. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `fullName` | `String` | Có | `@NotBlank`, `@Size(max=128)` | Tên hiển thị mới |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email mới nhận hóa đơn điện tử |
| `dateOfBirth` | `LocalDate` | Không | `YYYY-MM-DD` | Ngày sinh |
| `gender` | `String` | Không | `MALE`, `FEMALE`, `OTHER` | Giới tính |
| `avatarUrl` | `String` | Không | URL hợp lệ | Đường dẫn ảnh đại diện |

```json
{
  "fullName": "David Nguyen",
  "email": "david.nguyen@ordera.no",
  "dateOfBirth": "2000-08-23",
  "gender": "MALE",
  "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
}
```

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Cập nhật thông tin cá nhân thành công",
  "data": {
    "id": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "fullName": "David Nguyen",
    "email": "david.nguyen@ordera.no",
    "dateOfBirth": "2000-08-23",
    "gender": "MALE",
    "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
    "updatedAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.3: Tiến độ Thăng hạng Tiếp theo (Get Tier Upgrade Progress)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/{memberId}/progress`
- **Operation ID:** `getTierProgress`
- **Mô tả:** Tính toán số điểm cần tích thêm và tỷ lệ % hoàn thành để hiển thị thanh Stepper trên màn hình Thẻ và Chi tiết Cấp bậc (Figma Screen `04.01`, `05.01`).
- **Frontend Gọi tại:** `MilestoneStepper.tsx`, `TierPage.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `memberId` | `UUID` | **Có** | ID hồ sơ thành viên |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Tier progression details calculated successfully",
  "data": {
    "currentTierName": "Gold",
    "currentLifetimePoints": 300,
    "nextTierName": "Platinum",
    "nextTierRequiredPoints": 1000,
    "pointsNeeded": 700,
    "progressPercentage": 30.0
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.4: Bảng Cấu hình 6 Hạng Thẻ & Đặc quyền (Get All Tiers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/tiers`
- **Operation ID:** `getAllTierDefinitions`
- **Mô tả:** Lấy danh sách 6 hạng thẻ từ New đến Ruby kèm mã màu HEX và danh sách các đặc quyền tương ứng.
- **Frontend Gọi tại:** `TierPage.tsx`, `TierDetailModal.tsx`

#### 1. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "All 6 tier definitions retrieved successfully",
  "data": [
    {
      "id": "t1eebc99-9c0b-4ef8-bb6d-6bb9bd380t01",
      "tierName": "New",
      "tierOrder": 1,
      "minPointsRequired": 0,
      "themeColorHex": "#1EB759",
      "earnPointsRate": 1.0000,
      "benefitsSummary": [
        "Tích 1 điểm cho mỗi 10 Kr chi tiêu",
        "Tặng mã chào mừng thành viên mới"
      ]
    },
    {
      "id": "t2eebc99-9c0b-4ef8-bb6d-6bb9bd380t02",
      "tierName": "Silver",
      "tierOrder": 2,
      "minPointsRequired": 200,
      "themeColorHex": "#00A3FF",
      "earnPointsRate": 1.2000,
      "benefitsSummary": [
        "Hệ số tích điểm x1.2",
        "Ưu tiên nhận món tại quầy"
      ]
    },
    {
      "id": "t3eebc99-9c0b-4ef8-bb6d-6bb9bd380t03",
      "tierName": "Gold",
      "tierOrder": 3,
      "minPointsRequired": 400,
      "themeColorHex": "#F5B800",
      "earnPointsRate": 1.5000,
      "benefitsSummary": [
        "Hệ số tích điểm x1.5",
        "Tặng voucher bánh ngọt ngày sinh nhật"
      ]
    },
    {
      "id": "t4eebc99-9c0b-4ef8-bb6d-6bb9bd380t04",
      "tierName": "Platinum",
      "tierOrder": 4,
      "minPointsRequired": 1000,
      "themeColorHex": "#FF5B4D",
      "earnPointsRate": 1.8000,
      "benefitsSummary": [
        "Hệ số tích điểm x1.8",
        "Tặng Voucher 100 NOK khi lên hạng"
      ]
    },
    {
      "id": "t5eebc99-9c0b-4ef8-bb6d-6bb9bd380t05",
      "tierName": "Diamond",
      "tierOrder": 5,
      "minPointsRequired": 2000,
      "themeColorHex": "#7C3AED",
      "earnPointsRate": 2.0000,
      "benefitsSummary": [
        "Hệ số tích điểm x2.0",
        "Miễn phí nâng cỡ lớn (Upsize) cho mọi món đồ uống"
      ]
    },
    {
      "id": "t6eebc99-9c0b-4ef8-bb6d-6bb9bd380t06",
      "tierName": "Ruby",
      "tierOrder": 6,
      "minPointsRequired": 4000,
      "themeColorHex": "#D9148E",
      "earnPointsRate": 2.5000,
      "benefitsSummary": [
        "Hệ số tích điểm tối đa x2.5",
        "Tặng Cốc Tumbler Ruby phiên bản giới hạn",
        "Bàn ăn VIP đặt trước không cần chờ"
      ]
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.5: Lịch sử Sổ cái Biến động Điểm (Points Ledger History)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me/points`
- **Operation ID:** `getMyPointsLedgerHistory`
- **Mô tả:** Truy vấn dữ liệu phân trang từ bảng phân vùng `points_ledger` theo thời gian giảm dần, đảm bảo đối soát tính bất biến của điểm thưởng.
- **Frontend Gọi tại:** `PointsHistoryView.tsx`

#### 1. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `page` | `Integer` | `0` | Số trang (bắt đầu từ 0) |
| `size` | `Integer` | `20` | Số lượng bản ghi trên một trang (tối đa 50) |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Points ledger entries retrieved successfully",
  "data": {
    "content": [
      {
        "id": "l1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "transactionType": "ORDER_EARN",
        "pointsDelta": 170,
        "balanceAfter": 290,
        "referenceType": "ORDERS",
        "referenceId": "ORD-20261105-0891",
        "description": "Tích điểm từ đơn hàng Linn Sushi Sentrum",
        "createdAt": "2026-11-05T14:32:00Z"
      },
      {
        "id": "l2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "transactionType": "VOUCHER_REDEEM",
        "pointsDelta": -100,
        "balanceAfter": 120,
        "referenceType": "USER_VOUCHERS",
        "referenceId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "description": "Đổi điểm nhận Voucher 100 NOK",
        "createdAt": "2026-11-05T14:35:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.6: Gửi Khiếu nại Thiếu Điểm Hóa đơn (Submit Support Claim)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/claims`
- **Operation ID:** `submitSupportClaim`
- **Mô tả:** Khách hàng nhập mã phiếu tính tiền và ảnh chụp biên lai để yêu cầu cộng bù điểm nếu khi mua tại quầy bị nhân viên quên tích điểm (Figma Screen `05.02`).
- **Frontend Gọi tại:** `MissingPointsClaimModal.tsx`

#### 1. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `receiptCode` | `String` | Có | `@NotBlank`, `@Size(max=64)` | Mã hóa đơn in trên phiếu (vd: *RCP-8921-X*) |
| `orderDate` | `LocalDate` | Có | Không quá 30 ngày | Ngày thanh toán trên hóa đơn |
| `billAmount` | `BigDecimal` | Có | `@DecimalMin("1.00")` | Tổng số tiền đã thanh toán (NOK) |
| `billPhotoUrl`| `String` | Không | URL ảnh tải lên | Đường dẫn ảnh chụp hóa đơn |

```json
{
  "receiptCode": "RCP-8921-X",
  "orderDate": "2026-11-04",
  "billAmount": 350.00,
  "billPhotoUrl": "https://storage.ordera.no/claims/receipt-8921.jpg"
}
```

#### 2. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Yêu cầu khiếu nại đã được ghi nhận. Quản trị viên sẽ xử lý trong vòng 24 giờ.",
  "data": {
    "claimId": "sc1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "receiptCode": "RCP-8921-X",
    "billAmount": 350.00,
    "status": "PENDING",
    "createdAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Mã hóa đơn này đã từng gửi khiếu nại hoặc đã tích điểm rồi (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "CLAIM_ALREADY_EXISTS",
    "message": "Mã hóa đơn này đã được gửi khiếu nại trước đó hoặc đã được cộng điểm trong hệ thống.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
