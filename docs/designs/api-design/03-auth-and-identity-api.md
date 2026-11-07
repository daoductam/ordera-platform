# Phân hệ 03: Xác thực & Định danh API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Đăng nhập không mật khẩu, OTP Đa kênh (SMS & Email), Đăng ký 2 bước & Quản lý Phiên (Auth & JWT)**  
> **Ánh xạ Prototype:** `docs/prototype/2-dang-nhap-va-truy-cap-tai-khoan/` (`02.01` đến `02.07`), `docs/prototype/3-dang-ky-va-xac-thuc-thanh-vien/` (`03.01` đến `03.10`)  
> **Ánh xạ Frontend:** `frontend/src/pages/AuthFlowPage.tsx`, `LoginView.tsx`, `OtpVerificationModal.tsx`, `RegisterView.tsx`  
> **Bảng Database & Cache:** `users`, `otp_audit_logs`, `member_profiles`, `tier_definitions`, `Redis (OTP cache & Token Blacklist)`

---

### Danh sách Endpoint trong Phân hệ 03

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 3.1 | `/api/v1/auth/otp/send` | `POST` | Gửi mã OTP đăng nhập qua SMS (Cooldown 120s) | `02.03` |
| 3.2 | `/api/v1/auth/otp/verify` | `POST` | Xác thực mã OTP đăng nhập, cấp JWT, trả về cờ `isProfileComplete` | `02.04`, `02.05`, `02.07` |
| 3.3 | `/api/v1/auth/register/otp/send` | `POST` | Gửi mã OTP đăng ký mới (Hỗ trợ chọn kênh Email OTP hoặc SMS OTP) | `03.03`, `03.04` |
| 3.4 | `/api/v1/auth/register/otp/verify` | `POST` | Xác thực mã OTP đăng ký, khởi tạo tài khoản Member New (0 điểm) | `03.05`, `03.06`, `03.07`, `03.09` |
| 3.5 | `/api/v1/auth/refresh-token` | `POST` | Cấp mới Access Token bằng Refresh Token | Toàn hệ thống |
| 3.6 | `/api/v1/auth/logout` | `POST` | Đăng xuất, đưa token vào Redis Blacklist | `04.06` |

---

### API 3.1: Gửi Mã OTP Đăng nhập (Send Login OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/otp/send`
- **Operation ID:** `sendLoginOtp`
- **Mô tả:** Tiếp nhận SĐT (mặc định mã vùng Na Uy `+47`), sinh ngẫu nhiên mã số bảo mật 6 chữ số, lưu vào Redis với TTL 120 giây (kèm cooldown 120 giây) và gửi SMS qua cổng viễn thông.
- **Màn hình Prototype:** `02.03`
- **Frontend Gọi tại:** `LoginView.tsx`
- **Rate Limit:** Tối đa 3 lần / 10 phút trên cùng 1 SĐT / IP.

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Content-Type`| `String` | **Có** | `application/json` | Định dạng dữ liệu |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc & Validation | Mô tả |
|:---|:---|:---:|:---|:---|
| `channel` | `String` | Có | `SMS` | Kênh gửi mã OTP (mặc định: SMS cho đăng nhập) |
| `phoneNumber` | `String` | Có | Định dạng E.164 (`^\+[1-9]\d{7,14}$`) | Số điện thoại nhận mã (vd: `+4797362748`) |

```json
{
  "channel": "SMS",
  "phoneNumber": "+4797362748"
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
    "phoneNumber": "+4797362748",
    "maskedPhoneNumber": "** ***748",
    "channel": "SMS",
    "cooldownSeconds": 120,
    "resendAllowedAfterSeconds": 120
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
        "field": "phoneNumber",
        "rejectedValue": "097362748",
        "message": "Số điện thoại phải tuân thủ định dạng quốc tế E.164 (bắt đầu bằng dấu +, ví dụ: +4797362748)"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Spam gửi lại OTP khi chưa hết thời gian Cooldown (`429 Too Many Requests`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_COOLDOWN_ACTIVE",
    "message": "Vui lòng đợi thêm 74 giây trước khi yêu cầu gửi lại mã OTP mới.",
    "details": {
      "retryAfterSeconds": 74
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.2: Xác thực Mã OTP Đăng nhập (Verify Login OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/otp/verify`
- **Operation ID:** `verifyLoginOtp`
- **Mô tả:** Kiểm tra mã 6 chữ số người dùng nhập vào. Nếu đúng, kiểm tra tài khoản: nếu đã đủ thông tin cá nhân thì trả về `isProfileComplete = true` và cặp JWT để đăng nhập; nếu là khách mới hoặc chưa hoàn thiện hồ sơ thì trả về `isProfileComplete = false` để Frontend mở tiếp màn hình `04.04 Complete Profile`.
- **Màn hình Prototype:** `02.04`, `02.05`, `02.07`
- **Frontend Gọi tại:** `OtpVerificationModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type`| `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `phoneNumber` | `String` | Có | Định dạng E.164 | SĐT đã nhận mã |
| `otpCode` | `String` | Có | Đúng 6 ký tự số (`^\d{6}$`) | Mã xác thực 6 số |

```json
{
  "phoneNumber": "+4797362748",
  "otpCode": "849201"
}
```

#### 3. Phản hồi Thành công — Người Dùng Đã Có Hồ Sơ Đầy Đủ (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Xác thực mã OTP thành công. Đăng nhập hoàn tất.",
  "data": {
    "isProfileComplete": true,
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0ZW5hbnRJZCI6ImEwZWViYzk5LTljMGItNGVmOC1iYjZkLTZiYjliZDM4MGExMSIsInJvbGUiOiJDVVNUT01FUiIsImV4cCI6MTgwMTkwMDUwMH0...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0eXBlIjoicmVmcmVzaCIsImV4cCI6MTgwMjQxODkwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "phoneNumber": "+4797362748",
      "email": "david.nguyen@ordera.no",
      "fullName": "David Nguyen",
      "role": "CUSTOMER",
      "phoneVerified": true,
      "emailVerified": true,
      "tierName": "Silver",
      "currentPoints": 120,
      "lifetimePoints": 300
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Thành công — Cần Hoàn Thiện Hồ Sơ (`02.07`, `04.04`)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Xác thực thành công. Vui lòng hoàn tất thông tin cá nhân của bạn.",
  "data": {
    "isProfileComplete": false,
    "tempAuthToken": "temp_auth_98fbc10293da4cba829",
    "phoneNumber": "+4797362748",
    "user": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 5. Phản hồi Lỗi Chi tiết
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

### API 3.3: Gửi Mã OTP Đăng Ký Mới (Send Registration OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/register/otp/send`
- **Operation ID:** `sendRegisterOtp`
- **Mô tả:** Tiếp nhận thông tin đăng ký của khách hàng (SĐT, Email, Họ tên) và gửi mã OTP 6 số qua kênh người dùng tùy chọn: Email (`03.03`) hoặc SMS (`03.04`).
- **Màn hình Prototype:** `03.03`, `03.04`
- **Frontend Gọi tại:** `RegisterView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type`| `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc & Validation | Mô tả |
|:---|:---|:---:|:---|:---|
| `channel` | `String` | Có | `SMS` hoặc `EMAIL` | Kênh khách hàng chọn nhận OTP |
| `phoneNumber` | `String` | Có | Định dạng E.164 | Số điện thoại đăng ký |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Địa chỉ email đăng ký |
| `fullName` | `String` | Có | `@NotBlank`, `@Size(min=2, max=128)` | Họ tên của khách hàng (vd: *ABY TRAN*) |

```json
{
  "channel": "EMAIL",
  "phoneNumber": "+4797362748",
  "email": "abytran@ordera.no",
  "fullName": "ABY TRAN"
}
```

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Mã xác thực đã được gửi thành công qua Email abytran@ordera.no",
  "data": {
    "channel": "EMAIL",
    "recipientMasked": "a***n@ordera.no",
    "cooldownSeconds": 120
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Số điện thoại hoặc Email đã tồn tại (`409 Conflict`):**
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

### API 3.4: Xác Thực OTP Đăng Ký & Khởi Tạo Tài Khoản (Verify Registration OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/register/otp/verify`
- **Operation ID:** `verifyRegisterOtp`
- **Mô tả:** Xác thực mã OTP 6 số để kích hoạt tài khoản hội viên mới (`New Member`), gán 0 điểm ban đầu, cấp phát cặp JWT và chuẩn bị điều hướng tới màn hình chào mừng `03.10`.
- **Màn hình Prototype:** `03.05`, `03.06`, `03.07`, `03.09`, `03.10`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type`| `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `phoneNumber` | `String` | Có | Định dạng E.164 | SĐT đã đăng ký |
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email đã đăng ký |
| `otpCode` | `String` | Có | Đúng 6 ký tự số | Mã OTP 6 số (vd: *387505*) |

```json
{
  "phoneNumber": "+4797362748",
  "email": "abytran@ordera.no",
  "otpCode": "387505"
}
```

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đăng ký thành viên thành công! Chào mừng ABY TRAN đến với Linn Sushi.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "u2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
      "fullName": "ABY TRAN",
      "email": "abytran@ordera.no",
      "phoneNumber": "+4797362748",
      "dateOfBirth": "2000-08-23"
    },
    "loyalty": {
      "tierName": "New",
      "tierOrder": 1,
      "themeColorHex": "#1EB759",
      "currentPoints": 0,
      "lifetimePoints": 0,
      "pointsToNextTier": 200,
      "nextTierName": "Silver"
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Mã OTP sai (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "OTP_INVALID",
    "message": "Mã xác thực không đúng. Vui lòng kiểm tra lại tin nhắn hoặc email.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.5: Cấp Lại Token (Refresh Access Token)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/refresh-token`
- **Operation ID:** `refreshToken`
- **Mô tả:** Sử dụng Refresh Token để cấp phát một Access Token mới mà không yêu cầu người dùng phải nhập lại mã OTP.
- **Frontend Gọi tại:** `apiClient.ts` (Interceptor bắt lỗi 401 tự động refresh token)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type`| `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `refreshToken` | `String` | Có | `@NotBlank` | Chuỗi Refresh Token JWT |

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0eXBlIjoicmVmcmVzaCJ9..."
}
```

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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

#### 4. Phản hồi Lỗi
- **Refresh Token hết hạn hoặc không hợp lệ (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "REFRESH_TOKEN_EXPIRED",
    "message": "Phiên làm việc dài hạn đã hết hạn. Vui lòng đăng nhập lại.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.6: Đăng Xuất & Thu Hồi Phiên (Logout)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/logout`
- **Operation ID:** `logout`
- **Mô tả:** Đưa token JWT hiện tại vào Blacklist trong Redis cho đến khi token tự hết hạn để ngăn chặn tái sử dụng và thu hồi Refresh Token.
- **Màn hình Prototype:** `04.06` (Nút Logout dưới đáy trang tài khoản)
- **Frontend Gọi tại:** `AccountPage.tsx`, `AuthContext.tsx`

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
  "message": "Đăng xuất thành công. Phiên làm việc đã bị hủy.",
  "data": null,
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
