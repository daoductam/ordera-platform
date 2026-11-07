# Phân hệ 04: Hạng Thành viên & Sổ cái Điểm API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Hồ sơ Khách hàng, 6 Cấp bậc Thẻ (New $\rightarrow$ Ruby), Tiến độ Thăng hạng & Khiếu nại Thiếu điểm**  
> **Ánh xạ Prototype:** `docs/prototype/4-ho-so-va-hang-thanh-vien/` (`04.01` đến `04.15`), `docs/prototype/5-thong-bao-va-ho-tro-diem/` (`05.03`)  
> **Ánh xạ Frontend:** `frontend/src/pages/AccountPage.tsx`, `frontend/src/pages/TierPage.tsx`, `ProfileDetailView.tsx`, `MilestoneStepper.tsx`, `MissingPointsClaimModal.tsx`  
> **Bảng Database:** `member_profiles`, `tier_definitions`, `points_ledger` (Partitioned), `support_claims`

---

### Danh sách Endpoint trong Phân hệ 04

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 4.1 | `/api/v1/loyalty/members/me` | `GET` | Xem hồ sơ hội viên, điểm khả dụng/trọn đời, hạng thẻ & huy hiệu bộ đếm | `04.01`, `04.02`, `04.03` |
| 4.2 | `/api/v1/loyalty/members/me` | `PUT` | Cập nhật thông tin cá nhân (Họ tên, Email, Ngày sinh, Giới tính) | `04.08`, `04.09` |
| 4.3 | `/api/v1/loyalty/members/complete-profile` | `POST` | Hoàn thiện hồ sơ bổ sung sau khi xác thực OTP thành công | `04.04`, `04.05` |
| 4.4 | `/api/v1/loyalty/members/me/avatar` | `POST` | Tải lên ảnh đại diện mới qua icon máy ảnh | `04.07` |
| 4.5 | `/api/v1/loyalty/tiers` | `GET` | Cấu hình 6 cấp bậc thẻ từ New đến Ruby (mốc điểm, hệ số, đặc quyền) | `04.10` đến `04.15` |
| 4.6 | `/api/v1/loyalty/members/me/points` | `GET` | Lịch sử sổ cái biến động điểm bất biến (`points_ledger`) | `04.02`, Quản lý điểm |
| 4.7 | `/api/v1/loyalty/claims` | `POST` | Gửi phiếu khiếu nại thiếu điểm hóa đơn kèm ảnh biên lai | `05.03` |

---

### API 4.1: Xem Hồ sơ & Thẻ Hội viên Tổng quan (Get Member Profile & Dashboard)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `getMyMemberProfile`
- **Mô tả:** Lấy toàn bộ thông tin thẻ thành viên của người dùng đăng nhập hiện tại: họ tên, SĐT, mã Barcode ảo quét tại quầy, điểm khả dụng (`currentPoints`), điểm trọn đời (`lifetimePoints`), bậc thẻ hiện tại, tiến độ lên bậc tiếp theo và các bộ đếm số lượng ưu đãi/đơn hàng để render huy hiệu đỏ trên các ô tác vụ nhanh (`04.03`).
- **Màn hình Prototype:** `04.01`, `04.02`, `04.03`
- **Frontend Gọi tại:** `AccountOverview.tsx`, `MemberCard.tsx`, `ProfileDetailView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token của hội viên |

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
    "email": "info@ordera.no",
    "dateOfBirth": "2000-08-23",
    "gender": "MALE",
    "avatarUrl": "https://storage.ordera.no/avatars/david-nguyen.jpg",
    "phoneVerified": true,
    "emailVerified": true,
    "barcodeToken": "ORD-MEM-97362748",
    "barcodeQrUrl": "https://api.ordera.no/v1/barcodes/ORD-MEM-97362748.png",
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
    "quickActionCounters": {
      "activeVouchersCount": 2,
      "availableGiftsCount": 1,
      "activeOrdersCount": 1,
      "unreadNotificationsCount": 1
    },
    "createdAt": "2026-11-05T00:00:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi (RFC 7807)
- **Chưa đăng nhập hoặc Token hết hạn (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Hội viên chưa xác thực hoặc phiên đăng nhập đã hết hạn.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Không tìm thấy hồ sơ hội viên trong Tenant này (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "MEMBER_PROFILE_NOT_FOUND",
    "message": "Không tìm thấy hồ sơ thành viên tương ứng với tài khoản người dùng trong thương hiệu này.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.2: Cập nhật Thông tin Cá nhân (Update Member Profile)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/loyalty/members/me`
- **Operation ID:** `updateMemberProfile`
- **Mô tả:** Chỉnh sửa họ tên, email, ngày sinh hoặc đổi ảnh đại diện trong màn hình `04.08` và `04.09`.
- **Màn hình Prototype:** `04.08`, `04.09`
- **Frontend Gọi tại:** `EditProfileView.tsx`, `EditProfileDrawer.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |
| `Content-Type` | `String` | **Có** | `application/json` | Định dạng payload |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `fullName` | `String` | Có | `@NotBlank`, `@Size(max=128)` | Họ và tên hiển thị mới |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email mới nhận hóa đơn điện tử |
| `dateOfBirth` | `LocalDate` | Không | `YYYY-MM-DD` | Ngày sinh để nhận ưu đãi sinh nhật |
| `gender` | `String` | Không | `MALE`, `FEMALE`, `OTHER` | Giới tính |
| `avatarUrl` | `String` | Không | URL hợp lệ | Đường dẫn ảnh đại diện |

```json
{
  "fullName": "David Nguyen",
  "email": "david.nguyen@ordera.no",
  "dateOfBirth": "2000-08-23",
  "gender": "MALE",
  "avatarUrl": "https://storage.ordera.no/avatars/david-nguyen.jpg"
}
```

#### 3. Phản hồi Thành công (200 OK)
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
    "avatarUrl": "https://storage.ordera.no/avatars/david-nguyen.jpg",
    "updatedAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi Chi tiết
- **Email đã được sử dụng bởi tài khoản khác trong cùng thương hiệu (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "Địa chỉ email này đã được liên kết với một tài khoản thành viên khác trong thương hiệu.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Dữ liệu không đúng định dạng (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Dữ liệu cập nhật hồ sơ không hợp lệ.",
    "details": [
      {
        "field": "email",
        "rejectedValue": "invalid-email",
        "message": "Email phải tuân thủ đúng định dạng chuẩn RFC 5322"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.3: Hoàn Thiện Hồ Sơ Sau OTP (Complete Profile)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/members/complete-profile`
- **Operation ID:** `completeMemberProfile`
- **Mô tả:** Được gọi sau khi người dùng xác thực OTP đăng nhập lần đầu và hệ thống phát hiện hồ sơ còn thiếu thông tin bắt buộc (họ tên, email, ngày sinh) trong màn hình `04.04` và `04.05`.
- **Màn hình Prototype:** `04.04`, `04.05`
- **Frontend Gọi tại:** `CompleteProfileModal.tsx`, `AuthFlowPage.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |
| `Content-Type` | `String` | **Có** | `application/json` | Định dạng dữ liệu |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `fullName` | `String` | Có | `@NotBlank`, `@Size(min=2, max=128)` | Họ tên đầy đủ của hội viên |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email nhận biên nhận đơn hàng |
| `dateOfBirth` | `LocalDate` | Không | `YYYY-MM-DD` | Ngày sinh nhật |
| `gender` | `String` | Không | `MALE`, `FEMALE`, `OTHER` | Giới tính |

```json
{
  "fullName": "ABY TRAN",
  "email": "abytran@ordera.no",
  "dateOfBirth": "2000-08-23",
  "gender": "FEMALE"
}
```

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Hoàn thiện hồ sơ thành công! Tài khoản hội viên đã sẵn sàng.",
  "data": {
    "memberId": "m2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
    "fullName": "ABY TRAN",
    "email": "abytran@ordera.no",
    "isProfileComplete": true,
    "currentTier": "New",
    "currentPoints": 0,
    "updatedAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu thông tin bắt buộc (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "MISSING_REQUIRED_FIELDS",
    "message": "Họ tên và email là thông tin bắt buộc để kích hoạt tài khoản.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.4: Tải Lên Ảnh Đại Diện Hội Viên (Upload Avatar)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/members/me/avatar`
- **Operation ID:** `uploadMemberAvatar`
- **Mô tả:** Khách hàng chạm vào biểu tượng máy ảnh trên ảnh đại diện tại màn hình `04.07` để tải lên ảnh chân dung mới (hỗ trợ JPG, PNG, WebP tối đa 5MB).
- **Màn hình Prototype:** `04.07`
- **Frontend Gọi tại:** `ProfileDetailView.tsx`, `AvatarUploader.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |
| `Content-Type` | `String` | **Có** | `multipart/form-data` | Upload tệp nhị phân |

#### 2. Request Body (Form Data)
| Tham số | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `file` | `Binary File` | Có | Định dạng: `image/jpeg`, `image/png`, `image/webp`. Dung lượng tối đa: 5MB | Tệp ảnh đại diện |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Ảnh đại diện đã được cập nhật thành công.",
  "data": {
    "avatarUrl": "https://storage.ordera.no/avatars/david-nguyen-updated.jpg",
    "uploadedAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Tệp vượt quá dung lượng cho phép (`413 Payload Too Large`):**
```json
{
  "success": false,
  "error": {
    "code": "FILE_TOO_LARGE",
    "message": "Dung lượng ảnh vượt quá giới hạn tối đa cho phép (5MB).",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Định dạng ảnh không được hỗ trợ (`415 Unsupported Media Type`):**
```json
{
  "success": false,
  "error": {
    "code": "UNSUPPORTED_IMAGE_FORMAT",
    "message": "Chỉ chấp nhận các tệp ảnh định dạng JPEG, PNG hoặc WebP.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.5: Danh sách 6 Cấp bậc Thẻ & Đặc quyền (Get Tier Definitions)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/tiers`
- **Operation ID:** `getTierDefinitions`
- **Mô tả:** Lấy danh sách toàn bộ 6 cấp bậc thành viên (`New` $\rightarrow$ `Silver` $\rightarrow$ `Gold` $\rightarrow$ `Platinum` $\rightarrow$ `Diamond` $\rightarrow$ `Ruby`) cùng hệ số tích điểm, mốc điểm tối thiểu và danh sách đặc quyền tương ứng trong các màn hình `04.10` đến `04.15`.
- **Màn hình Prototype:** `04.10`, `04.11`, `04.12`, `04.13`, `04.14`, `04.15`
- **Frontend Gọi tại:** `TierPage.tsx`, `TierCardSlider.tsx`, `MilestoneStepper.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Tier definitions retrieved successfully",
  "data": [
    {
      "id": "t1eebc99-9c0b-4ef8-bb6d-6bb9bd380t01",
      "tierName": "New",
      "tierOrder": 1,
      "minPointsRequired": 0,
      "themeColorHex": "#1EB759",
      "earnPointsRate": 1.0000,
      "benefitsSummary": [
        "Tích lũy 1 điểm cho mỗi 10 NOK chi tiêu",
        "Nhận thông báo ưu đãi và sự kiện sớm nhất"
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
        "Hệ số tích điểm x1.2 cho mỗi đơn hàng",
        "Ưu tiên nhận bàn vào giờ cao điểm"
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
        "Hệ số tích điểm x1.5 cho mọi hóa đơn",
        "Tặng món tráng miệng miễn phí vào tháng sinh nhật",
        "Đổi điểm nhận Voucher 100 NOK độc quyền"
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

### API 4.6: Lịch sử Sổ cái Biến động Điểm (Points Ledger History)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/loyalty/members/me/points`
- **Operation ID:** `getMyPointsLedgerHistory`
- **Mô tả:** Truy vấn dữ liệu phân trang từ bảng phân vùng `points_ledger` theo thời gian giảm dần, đảm bảo đối soát tính bất biến của điểm thưởng và cung cấp lịch sử cộng/trừ điểm chi tiết.
- **Màn hình Prototype:** `04.02`, Quản lý điểm
- **Frontend Gọi tại:** `PointsHistoryView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |

#### 2. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `page` | `Integer` | `0` | Số trang (bắt đầu từ 0) |
| `size` | `Integer` | `20` | Số lượng bản ghi trên một trang (tối đa 50) |

#### 3. Phản hồi Thành công (200 OK)
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
      },
      {
        "id": "l3eebc99-9c0b-4ef8-bb6d-6bb9bd380003",
        "transactionType": "CLAIM_CREDIT",
        "pointsDelta": 35,
        "balanceAfter": 220,
        "referenceType": "SUPPORT_CLAIMS",
        "referenceId": "sc1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "description": "Cộng điểm bổ sung từ khiếu nại hóa đơn RCP-8921-X",
        "createdAt": "2026-11-04T18:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để truy cập lịch sử điểm thưởng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 4.7: Gửi Khiếu nại Thiếu Điểm Hóa đơn (Submit Support Claim)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/loyalty/claims`
- **Operation ID:** `submitSupportClaim`
- **Mô tả:** Khách hàng nhập mã phiếu tính tiền và ảnh chụp biên lai để yêu cầu cộng bù điểm nếu khi mua tại quầy bị nhân viên quên tích điểm (`05.03`).
- **Màn hình Prototype:** `05.03`
- **Frontend Gọi tại:** `MissingPointsClaimModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |
| `Content-Type` | `String` | **Có** | `application/json` | Định dạng dữ liệu |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `receiptCode` | `String` | Có | `@NotBlank`, `@Size(max=64)` | Mã hóa đơn in trên phiếu (vd: *RCP-8921-X*) |
| `orderDate` | `LocalDate` | Có | Không quá 30 ngày | Ngày thanh toán trên hóa đơn |
| `billAmount` | `BigDecimal` | Có | `@DecimalMin("1.00")` | Tổng số tiền đã thanh toán (NOK) |
| `billPhotoUrl`| `String` | Không | URL ảnh tải lên | Đường dẫn ảnh chụp hóa đơn |
| `note` | `String` | Không | Tối đa 500 ký tự | Ghi chú thêm cho nhân viên đối soát |

```json
{
  "receiptCode": "RCP-8921-X",
  "orderDate": "2026-11-04",
  "billAmount": 350.00,
  "billPhotoUrl": "https://storage.ordera.no/claims/receipt-8921.jpg",
  "note": "Hóa đơn mua tại quầy Linn Sushi Sentrum lúc trưa 12h30 nhưng thu ngân chưa quét thẻ."
}
```

#### 3. Phản hồi Thành công (201 Created)
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

#### 4. Phản hồi Lỗi
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
- **Hóa đơn vượt quá thời hạn 30 ngày cho phép khiếu nại (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "RECEIPT_EXPIRED",
    "message": "Hệ thống chỉ hỗ trợ tiếp nhận khiếu nại cộng điểm cho hóa đơn phát sinh trong vòng 30 ngày gần nhất.",
    "details": {
      "orderDate": "2026-09-15",
      "maxAllowedDays": 30
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
