# Phân hệ 03: Xác thực & Định danh API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Đăng nhập không mật khẩu, OTP Đa kênh (SMS & Email), Đăng ký 2 bước & Quản lý Phiên (Auth & JWT)**  
> **Ánh xạ Prototype:** `docs/prototype/2-dang-nhap-va-truy-cap-tai-khoan/` (`02.01` - `02.07`), `docs/prototype/3-dang-ky-va-xac-thuc-thanh-vien/` (`03.01` - `03.10`)  
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
- **Mô tả:** Tiếp nhận SĐT (mặc định mã vùng `+47`), sinh ngẫu nhiên mã số 6 chữ số, lưu vào Redis với TTL 120 giây (kèm cooldown 120 giây) và gửi SMS qua cổng viễn thông.
- **Màn hình Prototype:** `02.03`

#### 1. Request Body Schema
```json
{
  "channel": "SMS",
  "phoneNumber": "+4797362748"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Mã xác thực OTP đã được gửi thành công qua tin nhắn SMS.",
  "data": {
    "phoneNumber": "+4797362748",
    "maskedPhoneNumber": "** ***748",
    "channel": "SMS",
    "cooldownSeconds": 120
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.2: Xác thực Mã OTP Đăng nhập (Verify Login OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/otp/verify`
- **Operation ID:** `verifyLoginOtp`
- **Mô tả:** Kiểm tra mã 6 chữ số người dùng nhập vào (`387505`). Nếu đúng: Cấp JWT tokens và trả về cờ `isProfileComplete`:
  - `isProfileComplete == true`: Đủ họ tên & email $\rightarrow$ Điều hướng về **04.03 Tổng quan tài khoản**.
  - `isProfileComplete == false`: Thiếu họ tên/email $\rightarrow$ Điều hướng sang **04.04 Bổ sung thông tin**.
- **Màn hình Prototype:** `02.04`, `02.05`, `02.06`, `02.07`

#### 1. Request Body Schema
```json
{
  "phoneNumber": "+4797362748",
  "otpCode": "387505"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Xác thực mã OTP thành công.",
  "data": {
    "isNewUser": false,
    "isProfileComplete": true,
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "phoneNumber": "+4797362748",
      "fullName": "David Nguyen",
      "email": "david.nguyen@ordera.no",
      "currentTier": "Silver",
      "currentPoints": 120,
      "lifetimePoints": 300
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.3: Gửi Mã OTP Đăng ký Mới Đa Kênh (Send Registration OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/register/otp/send`
- **Operation ID:** `sendRegisterOtp`
- **Mô tả:** Tiếp nhận toàn bộ thông tin đăng ký ở Bước 1 (`Your name *`, `Email *`, `Phone number *`, `Date of birth`), lưu hồ sơ tạm thời, sau đó gửi OTP theo kênh người dùng chọn ở Bước 2 (`EMAIL` hoặc `SMS`).
- **Màn hình Prototype:** `03.01`, `03.02`, `03.03`, `03.04`

#### 1. Request Body Schema
```json
{
  "fullName": "ABY TRAN",
  "email": "abytran@ordera.no",
  "phoneNumber": "+4797362748",
  "dateOfBirth": "2000-08-23",
  "channel": "EMAIL"
}
```
*(Hoặc `"channel": "SMS"`)*

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Mã xác thực đã được gửi tới địa chỉ email của bạn.",
  "data": {
    "channel": "EMAIL",
    "maskedDestination": "in** ***gmail.com",
    "cooldownSeconds": 120
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 3.4: Xác thực OTP Đăng ký & Khởi tạo Tài khoản (Verify Registration OTP)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/register/otp/verify`
- **Operation ID:** `verifyRegisterOtp`
- **Mô tả:** Xác thực mã OTP 6 số để kích hoạt tài khoản hội viên mới (`New Member`), gán 0 điểm ban đầu, trả về JWT và chuẩn bị điều hướng tới màn hình `03.10`.
- **Màn hình Prototype:** `03.05`, `03.06`, `03.07`, `03.08`, `03.09`, `03.10`

#### 1. Request Body Schema
```json
{
  "phoneNumber": "+4797362748",
  "email": "abytran@ordera.no",
  "otpCode": "387505"
}
```

#### 2. Phản hồi Thành công (201 Created)
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

---

### API 3.5: Làm Mới Token (Refresh Access Token)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/refresh-token`
- **Operation ID:** `refreshToken`
- **Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZC5uZ3V5ZW4iLCJ0eXBlIjoicmVmcmVzaCJ9..."
}
```
- **Response 200 OK:** Trả về Access Token mới có thời hạn 24 giờ.

---

### API 3.6: Đăng xuất & Thu Hồi Phiên (Logout)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/auth/logout`
- **Operation ID:** `logout`
- **Mô tả:** Đưa token JWT hiện tại vào Blacklist trong Redis và thu hồi Refresh Token.
- **Màn hình Prototype:** `04.06` (Nút Logout dưới đáy trang)
