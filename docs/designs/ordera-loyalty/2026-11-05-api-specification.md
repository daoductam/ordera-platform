# REST API Contract Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform (v1.0)

> **Tài liệu Đặc tả Giao diện Lập trình Ứng dụng (API Contract Specification)**  
> **Phiên bản:** 1.0  
> **Tiêu chuẩn:** RESTful, JSON API, OpenAPI 3.0 / RFC 7807  
> **Nguyên tắc cốt lõi:** Frontend (React 19) và Backend (Java 21 Spring Boot) giao tiếp thông qua các hợp đồng dữ liệu chuẩn xác được định nghĩa trong tài liệu này.

---

## 1. Quy chuẩn Chung (Global API Conventions)

### 1.1 Base URL & Phiên bản
- Mọi endpoint đều có tiền tố: `/api/v1`
- Ví dụ: `http://localhost:8080/api/v1/stores`, `https://api.ordera.no/api/v1/loyalty/tiers`

### 1.2 Headers Bắt buộc (Mandatory Headers)
| Header Name | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---|:---|
| `X-Tenant-ID` | UUID | **Có (100% requests)** | Định danh thương hiệu/đại lý (vd: `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` - Linn Sushi) |
| `Authorization` | String | Khi gọi API yêu cầu đăng nhập | Định dạng: `Bearer <jwt_access_token>` |
| `Content-Type` | String | Cho POST / PUT | `application/json; charset=UTF-8` |
| `Accept` | String | Cho mọi request | `application/json` |

### 1.3 Cấu trúc Phong bì Dữ liệu Chuẩn (Common Response Envelope)

Mọi API thành công đều trả về theo định dạng `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

Khi có lỗi xảy ra, hệ thống trả về mã lỗi HTTP tương ứng kèm thân JSON chuẩn RFC 7807:
```json
{
  "success": false,
  "message": "Member points balance insufficient to redeem this voucher",
  "data": null,
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

### 1.4 Bảng Mã Lỗi Chuẩn (Standard HTTP Status Codes)
- `200 OK`: Truy vấn thành công, trả về dữ liệu.
- `201 Created`: Tạo mới thành công (Đơn hàng, Người dùng, Voucher redeem).
- `400 Bad Request`: Dữ liệu đầu vào sai validation, thiếu trường bắt buộc.
- `401 Unauthorized`: Chưa đăng nhập hoặc JWT Token hết hạn/không hợp lệ.
- `403 Forbidden`: Người dùng không có quyền truy cập tài nguyên hoặc khác `tenant_id`.
- `404 Not Found`: Không tìm thấy cửa hàng, món ăn, hóa đơn.
- `409 Conflict`: Trùng lặp dữ liệu (SĐT đã đăng ký, Voucher đã sử dụng).
- `429 Too Many Requests`: Vượt quá số lần gửi OTP (Rate limit).
- `500 Internal Server Error`: Lỗi hệ thống server không mong muốn.

---

## 2. Chi tiết Endpoints theo Phân hệ Nghiệp vụ

---

### PHÂN HỆ 1: TENANT & CỬA HÀNG (`/api/v1/stores`, `/api/v1/tenants`)

#### 1.1 Lấy danh sách Cửa hàng chi nhánh
- **Method:** `GET`
- **Path:** `/api/v1/stores`
- **Query Params:** `city` (optional, string), `activeOnly` (boolean, default: true)
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Linn Sushi Sentrum",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "city": "Oslo",
      "latitude": 59.9114910,
      "longitude": 10.7482320,
      "phoneNumber": "+4722334455",
      "openingHours": "10:00 - 22:00",
      "active": true,
      "acceptsPickup": true,
      "acceptsDinein": true
    }
  ],
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 1.2 Lấy thông tin chi tiết Thương hiệu hiện tại
- **Method:** `GET`
- **Path:** `/api/v1/tenants/current`
- **Headers:** `X-Tenant-ID`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "code": "linn_sushi",
    "name": "Linn Sushi",
    "logoUrl": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
    "primaryColor": "#F5B800",
    "secondaryColor": "#1E293B",
    "status": "ACTIVE"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

---

### PHÂN HỆ 2: THỰC ĐƠN & MÓN ĂN (`/api/v1/stores/{id}/menu`, `/api/v1/products`)

#### 2.1 Lấy toàn bộ Thực đơn của Chi nhánh
- **Method:** `GET`
- **Path:** `/api/v1/stores/{storeId}/menu`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "categories": [
      {
        "id": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
        "name": "Sushi Rolls",
        "displayOrder": 1,
        "active": true,
        "products": [
          {
            "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
            "name": "Aurora",
            "description": "Cá hồi tươi Na Uy phủ bơ béo ngậy, sốt teriyaki đặc biệt",
            "basePrice": 89.00,
            "imageUrl": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
            "available": true,
            "vatRate": 15.00
          },
          {
            "id": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
            "name": "Tuna Maki",
            "description": "Cá ngừ đại dương tươi rói cuộn rong biển truyền thống",
            "basePrice": 89.00,
            "imageUrl": "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56",
            "available": true,
            "vatRate": 15.00
          }
        ]
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 2.2 Lấy danh sách Nhóm tùy chọn (Modifiers) của Món ăn
- **Method:** `GET`
- **Path:** `/api/v1/products/{productId}/modifiers`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380e01",
      "name": "Size",
      "minSelection": 1,
      "maxSelection": 1,
      "displayOrder": 1,
      "modifiers": [
        {
          "id": "f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01",
          "name": "Regular (6 Pieces)",
          "extraPrice": 0.00,
          "isDefault": true
        },
        {
          "id": "f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02",
          "name": "Large (10 Pieces)",
          "extraPrice": 45.00,
          "isDefault": false
        }
      ]
    }
  ],
  "timestamp": "2026-11-05T20:45:00Z"
}
```

---

### PHÂN HỆ 3: XÁC THỰC & ĐĂNG NHẬP (`/api/v1/auth`)

#### 3.1 Gửi mã OTP xác thực
- **Method:** `POST`
- **Path:** `/api/v1/auth/otp/send`
- **Request Body:**
```json
{
  "channel": "SMS",
  "recipient": "+4797362748",
  "purpose": "AUTH"
}
```
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "OTP has been sent successfully. Valid for 120 seconds.",
  "data": {
    "cooldownSeconds": 120,
    "recipient": "+4797362748"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 3.2 Xác thực mã OTP
- **Method:** `POST`
- **Path:** `/api/v1/auth/otp/verify`
- **Request Body:**
```json
{
  "recipient": "+4797362748",
  "code": "849201"
}
```
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "isNewUser": false,
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 86400,
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "phoneNumber": "+4797362748",
      "fullName": "David Nguyen",
      "role": "CUSTOMER"
    }
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 3.3 Đăng ký thông tin Thành viên mới
- **Method:** `POST`
- **Path:** `/api/v1/auth/register`
- **Request Body:**
```json
{
  "phoneNumber": "+4797362748",
  "fullName": "David Nguyen",
  "email": "david.nguyen@example.com",
  "dateOfBirth": "2000-08-23",
  "gender": "MALE"
}
```
- **Response 201 Created:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": "u1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "fullName": "David Nguyen",
      "email": "david.nguyen@example.com"
    }
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

---

### PHÂN HỆ 4: THÀNH VIÊN & HẠNG THẺ (`/api/v1/loyalty`)

#### 4.1 Lấy thông tin Hồ sơ Thành viên & Thẻ ảo
- **Method:** `GET`
- **Path:** `/api/v1/loyalty/members/me`
- **Headers:** `Authorization: Bearer <token>`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "memberId": "m1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "fullName": "David Nguyen",
    "phoneNumber": "+4797362748",
    "currentTier": {
      "name": "Gold",
      "order": 3,
      "themeColorHex": "#F5B800",
      "earnPointsRate": 1.5000
    },
    "currentPoints": 120,
    "lifetimePoints": 300,
    "barcodeValue": "ORD-MEM-97362748",
    "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 4.2 Lấy Tiến độ Thăng hạng Tiếp theo (Stepper)
- **Method:** `GET`
- **Path:** `/api/v1/loyalty/members/{memberId}/progress`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "nextTierName": "Platinum",
    "nextTierRequiredPoints": 1000,
    "pointsNeeded": 700,
    "progressPercentage": 30.0
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 4.3 Xem Lịch sử Điểm Sổ cái (Points Ledger)
- **Method:** `GET`
- **Path:** `/api/v1/loyalty/members/me/points?page=0&size=20`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "l1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "transactionType": "ORDER_EARN",
        "pointsDelta": 170,
        "balanceAfter": 290,
        "referenceId": "ORD-20261105-0891",
        "description": "Tích điểm từ đơn hàng Linn Sushi Sentrum",
        "createdAt": "2026-11-05T14:32:00Z"
      },
      {
        "id": "l2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "transactionType": "VOUCHER_REDEEM",
        "pointsDelta": -100,
        "balanceAfter": 120,
        "referenceId": "VOUCH-SAVE100",
        "description": "Đổi điểm nhận Voucher 100 NOK",
        "createdAt": "2026-11-05T14:35:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

---

### PHÂN HỆ 5: VÍ VOUCHER & ĐỔI QUÀ (`/api/v1/rewards`)

#### 5.1 Lấy danh sách Voucher của Khách (My Rewards)
- **Method:** `GET`
- **Path:** `/api/v1/rewards/my-vouchers?status=AVAILABLE`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "code": "SAVE100",
      "title": "Voucher 100 NOK",
      "description": "Giảm 100 NOK cho đơn hàng tối thiểu từ 250 NOK",
      "discountType": "FIXED_AMOUNT",
      "discountValue": 100.00,
      "minOrderValue": 250.00,
      "validUntil": "2026-12-03T23:59:59Z",
      "status": "AVAILABLE"
    }
  ],
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 5.2 Đổi Điểm Nhận Voucher hoặc Quà Hiện vật
- **Method:** `POST`
- **Path:** `/api/v1/rewards/vouchers/{voucherId}/redeem`
- **Headers:** `Authorization: Bearer <token>`
- **Response 201 Created:**
```json
{
  "success": true,
  "message": "Voucher redeemed successfully",
  "data": {
    "userVoucherId": "uv2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
    "pointsDeducted": 100,
    "newBalance": 20,
    "voucherCode": "SAVE100",
    "validUntil": "2026-12-03T23:59:59Z"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

---

### PHÂN HỆ 6: ĐẶT HÀNG & THANH TOÁN (`/api/v1/orders`)

#### 6.1 Tạo Đơn hàng & Thanh toán (Checkout)
- **Method:** `POST`
- **Path:** `/api/v1/orders/checkout`
- **Request Body:**
```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "pickupMethod": "COUNTER",
  "pickupTimeType": "ASAP",
  "tableNumber": null,
  "pickupPersonName": "David Nguyen",
  "pickupPersonPhone": "+4797362748",
  "appliedVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
  "tipAmount": 10.00,
  "paymentMethod": "VIPPS",
  "orderNotes": "Không kèm gừng đỏ",
  "items": [
    {
      "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "quantity": 2,
      "specialInstruction": "Ít cay",
      "modifiers": [
        {
          "modifierGroupName": "Size",
          "modifierName": "Regular (6 Pieces)",
          "extraPrice": 0.00
        }
      ]
    },
    {
      "productId": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
      "quantity": 1,
      "specialInstruction": null,
      "modifiers": []
    }
  ]
}
```
- **Response 201 Created:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "orderCode": "ORD-20261105-0891",
    "subtotalAmount": 267.00,
    "discountAmount": 100.00,
    "tipAmount": 10.00,
    "vatAmount": 23.09,
    "totalAmount": 177.00,
    "pointsEarned": 170,
    "status": "CONFIRMED",
    "paymentStatus": "PAID",
    "estimatedReadyAt": "2026-11-05T15:10:00Z"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

#### 6.2 Lấy Hóa đơn Điện tử E-Receipt
- **Method:** `GET`
- **Path:** `/api/v1/orders/{orderCode}/receipt`
- **Response 200 OK:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "orderCode": "ORD-20261105-0891",
    "storeName": "Linn Sushi Sentrum",
    "storeAddress": "Prinsens gate 10C, 0153 Oslo",
    "pickupMethod": "COUNTER",
    "pickupPersonName": "David Nguyen",
    "pickupPersonPhone": "+4797362748",
    "createdAt": "2026-11-05T14:32:00Z",
    "items": [
      {
        "productName": "Aurora",
        "quantity": 2,
        "unitPrice": 89.00,
        "itemTotal": 178.00,
        "modifiersSummary": "Regular"
      },
      {
        "productName": "Tuna Maki",
        "quantity": 1,
        "unitPrice": 89.00,
        "itemTotal": 89.00,
        "modifiersSummary": ""
      }
    ],
    "subtotalAmount": 267.00,
    "discountAmount": 100.00,
    "tipAmount": 10.00,
    "vatAmount": 23.09,
    "totalAmount": 177.00,
    "pointsEarned": 170,
    "barcodeUrl": "https://api.ordera.no/barcodes/ORD-20261105-0891.png"
  },
  "timestamp": "2026-11-05T20:45:00Z"
}
```

