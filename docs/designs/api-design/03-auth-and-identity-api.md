# Phân hệ 03: Xác thực & Định danh API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Đăng nhập, Gửi OTP SMS/Email, Đăng ký Thành viên & Quản lý Phiên (Auth & JWT)**  
> **Ánh xạ Frontend:** `frontend/src/pages/AuthFlowPage.tsx`, `LoginView.tsx`, `OtpVerificationModal.tsx`, `RegisterView.tsx`  
> **Bảng Database & Cache:** `users`, `otp_audit_logs`, `member_profiles`, `tier_definitions`, `Redis (OTP cache & Token Blacklist)`

---

### API 3.1: Gửi Mã OTP Xác thực (Send OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/otp/send`
- **Operation ID:** `sendOtp`
- **Mô tả:** Tiếp nhận SĐT (hoặc Email) của khách hàng, sinh ngẫu nhiên mã số bảo mật 6 chữ số, lưu vào Redis với TTL 120 giây (kèm cooldown 60 giây) và gửi tin nhắn SMS / Email qua cổng dịch vụ.
- **Frontend Gọi tại:** `LoginView.tsx`
- **Rate Limit:** Tối đa 3 lần / 10 phút trên cùng 1 SĐT / IP.

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type`| `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc & Validation | Mô tả |
|:---|:---|:---:|:---|:---|
| `channel` | `String` | Có | `SMS` hoặc `EMAIL` | Kênh gửi mã |
| `recipient` | `String` | Có | SĐT E.164 (`^\+[1-9]\d{7,14}$`) hoặc chuẩn Email RFC 5322 | Số điện thoại (+4797362748) hoặc Email |
| `purpose` | `String` | Không | `AUTH`, `REGISTER`, `PASSWORD_RESET` (Mặc định: `AUTH`) | Mục đích gửi mã |

```json
{
  "channel": "SMS",
  "recipient": "+4797362748",
  "purpose": "AUTH"
}
```

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Mã xác thực OTP đã được gửi thành công qua tin nhắn SMS.",
  "data": {
    "recipient": "+4797362748",
    "channel": "SMS",
    "cooldownSeconds": 120,
    "resendAllowedAfterSeconds": 60
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi Chi tiết
- **Số điện thoại sai định dạng E.164 (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Dữ liệu đầu vào không hợp lệ.",
    "details": [
      {
        "field": "recipient",
        "rejectedValue": "097362748",
        "message": "Số điện thoại phải tuân thủ định dạng quốc tế E.164 (bắt đầu bằng dấu +, ví dụ: +4797362748)"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Spam gửi lại OTP khi chưa hết 60 giây Cooldown (`429 Too Many Requests`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_COOLDOWN_ACTIVE",
    "message": "Vui lòng đợi thêm 42 giây trước khi yêu cầu gửi lại mã OTP.",
    "details": {
      "retryAfterSeconds": 42
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.2: Xác thực Mã OTP (Verify OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/otp/verify`
- **Operation ID:** `verifyOtp`
- **Mô tả:** Kiểm tra mã 6 chữ số người dùng nhập vào. Nếu đúng, kiểm tra tài khoản đã tồn tại chưa: nếu có thì trả về cặp JWT Token; nếu chưa thì trả về `isNewUser = true` để Frontend mở màn hình Đăng ký `03.01`.
- **Frontend Gọi tại:** `OtpVerificationModal.tsx`

#### 1. Request Body
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `recipient` | `String` | Có | SĐT hoặc Email đã gửi ở bước trước | Khóa định danh nhận mã |
| `code` | `String` | Có | Đúng 6 ký tự số (`^\d{6}$`) | Mã OTP 6 số |

```json
{
  "recipient": "+4797362748",
  "code": "849201"
}
```

#### 2. Phản hồi Thành công — Người dùng Đã có Tài khoản (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Xác thực mã OTP thành công. Đăng nhập hoàn tất.",
  "data": {
    "isNewUser": false,
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0ZW5hbnRJZCI6ImEwZWViYzk5LTljMGItNGVmOC1iYjZkLTZiYjliZDM4MGExMSIsInJvbGUiOiJDVVNUT01FUiIsImV4cCI6MTgwMTkwMDUwMH0...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0eXBlIjoicmVmcmVzaCIsImV4cCI6MTgwMjQxODkwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "phoneNumber": "+4797362748",
      "email": "david.nguyen@example.com",
      "fullName": "David Nguyen",
      "role": "CUSTOMER",
      "phoneVerified": true,
      "emailVerified": true,
      "tierName": "Gold",
      "currentPoints": 120,
      "lifetimePoints": 300
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Thành công — Khách Mới Chưa có Hồ sơ (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Mã OTP chính xác. Số điện thoại chưa được đăng ký trong hệ thống.",
  "data": {
    "isNewUser": true,
    "registrationToken": "reg_token_f92b7c6182e04f9812a",
    "phoneNumber": "+4797362748",
    "user": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi Chi tiết
- **Mã OTP sai (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_INVALID",
    "message": "Mã OTP không chính xác. Bạn còn 3 lần thử.",
    "details": {
      "remainingAttempts": 3
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Mã OTP đã hết hạn 120 giây (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_EXPIRED",
    "message": "Mã xác thực đã hết hạn hiệu lực. Vui lòng bấm gửi lại mã mới.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Nhập sai quá 5 lần — Khóa phiên (`429 Too Many Requests`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_MAX_ATTEMPTS_EXCEEDED",
    "message": "Bạn đã nhập sai mã xác thực quá 5 lần. Phiên xác thực này đã bị hủy vì lý do bảo mật.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.3: Đăng ký Thành viên Mới (Register Member)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/register`
- **Operation ID:** `registerMember`
- **Mô tả:** Khách hàng mới sau khi xác thực OTP thành công sẽ gửi thông tin cá nhân (Họ tên, Email, Ngày sinh, Giới tính) để mở tài khoản và cấp thẻ thành viên hạng `New Member` (0 điểm).
- **Frontend Gọi tại:** `RegisterView.tsx`

#### 1. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc & Validation | Mô tả |
|:---|:---|:---:|:---|:---|
| `phoneNumber` | `String` | Có | Định dạng E.164 | SĐT đã xác thực OTP |
| `fullName` | `String` | Có | `@NotBlank`, `@Size(min=2, max=128)` | Họ và tên khách hàng (vd: *David Nguyen*) |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email liên hệ nhận hóa đơn điện tử |
| `dateOfBirth` | `LocalDate` | Không | Định dạng `YYYY-MM-DD` | Ngày sinh để nhận ưu đãi sinh nhật |
| `gender` | `String` | Không | `MALE`, `FEMALE`, `OTHER` | Giới tính |

```json
{
  "phoneNumber": "+4797362748",
  "fullName": "David Nguyen",
  "email": "david.nguyen@example.com",
  "dateOfBirth": "2000-08-23",
  "gender": "MALE"
}
```

#### 2. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đăng ký thành viên thành công! Chào mừng bạn đến với Linn Sushi.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "phoneNumber": "+4797362748",
      "email": "david.nguyen@example.com",
      "fullName": "David Nguyen",
      "role": "CUSTOMER"
    },
    "memberProfile": {
      "id": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "tierName": "New",
      "tierOrder": 1,
      "themeColorHex": "#1EB759",
      "currentPoints": 0,
      "lifetimePoints": 0
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Số điện thoại hoặc Email đã tồn tại trong đại lý này (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "USER_ALREADY_EXISTS",
    "message": "Số điện thoại hoặc Email này đã được đăng ký tài khoản tại thương hiệu.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.4: Làm Mới Token (Refresh Access Token)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/refresh-token`
- **Operation ID:** `refreshToken`
- **Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0eXBlIjoicmVmcmVzaCJ9..."
}
```
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Cấp mới Access Token thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJleHAiOjE4MDE5ODAwMDB9...",
    "expiresIn": 86400
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.5: Đăng xuất & Hủy Phiên (Logout)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/logout`
- **Operation ID:** `logout`
- **Mô tả:** Đưa token JWT hiện tại vào Blacklist trong Redis cho đến khi token tự hết hạn để ngăn chặn tái sử dụng.
- **Headers:** `Authorization: Bearer <token>`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Đăng xuất thành công. Phiên làm việc đã bị hủy.",
  "data": null,
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
