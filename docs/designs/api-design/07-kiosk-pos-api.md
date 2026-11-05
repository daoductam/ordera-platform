# Phân hệ 07: Kiosk POS Tự Phục Vụ & Tích Điểm Tại Quầy API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Màn hình Kiosk Cảm ứng Toàn màn hình tại Quầy Thu ngân (Self-service / Points Accrual Kiosk)**  
> **Ánh xạ Frontend:** `frontend/src/pages/KioskPage.tsx`  
> **Bảng Database:** `stores`, `kiosk_devices`, `users`, `member_profiles`, `orders`, `order_items`, `points_ledger`

---

### API 7.1: Tra Cứu & Nhận Diện Thành Viên Qua Số Điện Thoại (Identify Customer by Phone)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/kiosk/identify`
- **Operation ID:** `kioskIdentifyCustomer`
- **Mô tả:** Khách hàng chạm số điện thoại trên bàn phím cảm ứng Kiosk. Hệ thống đối chiếu số điện thoại trong cơ sở dữ liệu tenant, trả về danh tính (tên gọi, hạng thẻ, số điểm hiện có). Nếu số điện thoại chưa tồn tại, hệ thống trả về cờ `isRegistered: false` cho phép khách bấm "Skip" hoặc tiếp tục tạo tài khoản tạm thời để tích lũy điểm ngay lập tức.
- **Frontend Gọi tại:** `KioskPage.tsx` (`handleLookup`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `phoneNumber` | `String` | Có | Định dạng E.164 (VD: `+4791234567` hoặc `0912345678`) | Số điện thoại khách nhập trên bàn phím số |
| `storeId` | `UUID` | Có | Phải là Store hợp lệ | ID chi nhánh đặt Kiosk |

```json
{
  "phoneNumber": "+4791234567",
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01"
}
```

#### 3. Phản hồi Thành công (200 OK)
##### Trường hợp A: Đã là thành viên đăng ký
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Customer identified successfully",
  "data": {
    "isRegistered": true,
    "userId": "e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e01",
    "name": "Đào Tâm Đức",
    "phoneNumber": "+4791234567",
    "currentTier": "Gold Member",
    "currentPoints": 2450,
    "lifetimePoints": 5200,
    "earnRatePercent": 2.0,
    "availableVoucherCount": 2
  },
  "timestamp": "2026-11-05T20:50:00.000Z"
}
```

##### Trường hợp B: Khách mới chưa đăng ký (Guest / New Member)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Customer not registered, temporary session created",
  "data": {
    "isRegistered": false,
    "userId": null,
    "name": "Khách tích điểm mới",
    "phoneNumber": "+4798888888",
    "currentTier": "New Member",
    "currentPoints": 0,
    "lifetimePoints": 0,
    "earnRatePercent": 1.0,
    "availableVoucherCount": 0
  },
  "timestamp": "2026-11-05T20:50:00.000Z"
}
```

#### 4. Phản hồi Lỗi (RFC 7807)
##### Lỗi 400 Bad Request — Số điện thoại không hợp lệ
```json
{
  "type": "https://api.orderaapp.com/errors/invalid-phone",
  "title": "Invalid Phone Number",
  "status": 400,
  "detail": "Phone number must have at least 8 digits",
  "instance": "/api/v1/kiosk/identify",
  "code": "INVALID_PHONE_NUMBER",
  "timestamp": "2026-11-05T20:50:00.000Z"
}
```

---

### API 7.2: Tích Điểm Nhanh Tại Quầy Không Cần Đặt Món (Kiosk Quick Points Accrual)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/kiosk/quick-earn`
- **Operation ID:** `kioskQuickEarnPoints`
- **Mô tả:** Được sử dụng khi khách gọi món và thanh toán trực tiếp với thu ngân bằng tiền mặt/máy POS ngoài, sau đó khách nhập số điện thoại tại màn hình Kiosk phụ để tích điểm. Hệ thống tự động ghi sổ cái `points_ledger` (TransactionType = `KIOSK_EARN`) theo tỷ lệ hạng thẻ của khách.
- **Frontend Gọi tại:** `KioskPage.tsx` (`handleCompletePayment`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` |
| `X-Staff-PIN` | `String` | Tùy chọn | `1234` (Mã xác thực thu ngân nếu bật chế độ bảo vệ) |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `storeId` | `UUID` | Có | Phải là Store hợp lệ | ID cửa hàng giao dịch |
| `phoneNumber` | `String` | Có | Định dạng E.164 | SĐT khách hàng nhận điểm |
| `billAmount` | `BigDecimal` | Có | `@DecimalMin("1.00")` | Tổng giá trị hóa đơn giấy (VD: `93.00`) |
| `externalReceiptRef` | `String` | Không | `@Size(max=64)` | Mã hóa đơn POS của thu ngân |

```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "phoneNumber": "+4791234567",
  "billAmount": 93.00,
  "externalReceiptRef": "POS-20261105-0891"
}
```

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Points credited successfully",
  "data": {
    "transactionId": "tx1ebc99-9c0b-4ef8-bb6d-6bb9bd380tx1",
    "phoneNumber": "+4791234567",
    "memberName": "Đào Tâm Đức",
    "billAmount": 93.00,
    "currency": "NOK",
    "pointsEarned": 140,
    "newTotalPoints": 2590,
    "currentTier": "Gold Member",
    "queueNumber": "#A-08"
  },
  "timestamp": "2026-11-05T20:50:30.000Z"
}
```

---

### API 7.3: Tự Đặt Món & Thanh Toán Trực Tiếp Tại Kiosk (Kiosk Direct Self-Checkout)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/kiosk/checkout`
- **Operation ID:** `kioskCheckoutOrder`
- **Mô tả:** Khách hàng tự bấm chọn món tại quầy Kiosk, nhập SĐT để tích điểm, chọn tip (hoặc 0 NOK), thanh toán thẻ qua máy quẹt thẻ tích hợp Kiosk (Card Reader Terminal / Vipps). Hệ thống sinh đơn hàng, xuất số thứ tự `#A-08` và kích hoạt lệnh in nhiệt.

#### 1. Request Body Schema
```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "kioskDeviceId": "kiosk-pos-sentrum-01",
  "customerPhone": "+4791234567",
  "customerName": "Đào Tâm Đức",
  "tipAmount": 0.00,
  "paymentMethod": "CARD",
  "items": [
    {
      "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "quantity": 1,
      "modifierIds": []
    },
    {
      "productId": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
      "quantity": 1,
      "modifierIds": []
    }
  ]
}
```

#### 2. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Kiosk order placed successfully",
  "data": {
    "orderId": "ord-20261105-k008",
    "orderCode": "A-08",
    "queueNumber": "#A-08",
    "status": "PAID",
    "subtotalAmount": 93.00,
    "tipAmount": 0.00,
    "vatAmount": 12.13,
    "totalAmount": 93.00,
    "currency": "NOK",
    "pointsEarned": 140,
    "printCommandDispatched": true,
    "resetTimeoutSeconds": 5
  },
  "timestamp": "2026-11-05T20:51:00.000Z"
}
```

---

### API 7.4: Đặt Lại Phiên & Tự Động Timeout Sau 60 Giây (Kiosk Session Reset / Start Over)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/kiosk/session/reset`
- **Operation ID:** `resetKioskSession`
- **Mô tả:** Được gọi khi khách bấm nút "Start Over" hoặc bộ đếm thời gian 60 giây hết hạn do khách bỏ đi mà không tương tác. Hệ thống xóa trắng session dữ liệu tạm trên Kiosk bộ nhớ cache Redis, đưa Kiosk về màn hình chào mặc định.
- **Frontend Gọi tại:** `KioskPage.tsx` (`handleStartOver`, đếm ngược `countdown <= 1`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` |

#### 2. Request Body Schema
```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "reason": "IDLE_TIMEOUT"
}
```
*Ghi chú `reason`: `IDLE_TIMEOUT`, `USER_MANUAL_RESET`, `TRANSACTION_COMPLETED`.*

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Kiosk session cleared and reset to standby",
  "data": {
    "kioskStatus": "STANDBY",
    "countdownDefaultSeconds": 60
  },
  "timestamp": "2026-11-05T20:51:30.000Z"
}
```

---

### API 7.5: Cấu Hình Thiết Bị & Khởi Tạo Trạm Kiosk (Get Kiosk Device Configuration)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/kiosk/config/{deviceId}`
- **Operation ID:** `getKioskDeviceConfig`
- **Mô tả:** Khi Kiosk khởi động máy tính cảm ứng, ứng dụng Frontend gọi API này để tải cấu hình chi nhánh, tên trạm, cài đặt máy in hóa đơn và chu kỳ idle timeout.

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `deviceId` | `String` | Có | Định danh phần cứng Kiosk (VD: `kiosk-pos-sentrum-01`) |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Kiosk configuration retrieved",
  "data": {
    "deviceId": "kiosk-pos-sentrum-01",
    "deviceName": "Counter Touch Terminal 01",
    "store": {
      "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
      "name": "Linn Sushi Sentrum",
      "currency": "NOK",
      "vatRatePercent": 15.00
    },
    "idleTimeoutSeconds": 60,
    "successScreenSeconds": 5,
    "printerConnected": true,
    "printerPaperStatus": "OK",
    "cardReaderStatus": "ONLINE"
  },
  "timestamp": "2026-11-05T20:52:00.000Z"
}
```
