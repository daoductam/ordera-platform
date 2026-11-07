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
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` | Mã định danh thiết bị Kiosk |

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
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:50:00.000Z"
}
```

- **Thiếu Header `X-Kiosk-Device-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "DEVICE_HEADER_MISSING",
    "message": "Header 'X-Kiosk-Device-ID' is required to verify kiosk terminal.",
    "details": null
  },
  "timestamp": "2026-11-05T20:50:00.000Z"
}
```

- **Số điện thoại không hợp lệ (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_PHONE_NUMBER",
    "message": "Số điện thoại phải có ít nhất 8 chữ số và đúng định dạng quốc tế.",
    "details": {
      "field": "phoneNumber",
      "value": "123"
    }
  },
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
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` | Mã định danh thiết bị Kiosk |
| `X-Staff-PIN` | `String` | Tùy chọn | `1234` | Mã xác thực thu ngân nếu bật chế độ bảo vệ |

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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:50:30.000Z"
}
```

- **Mã Staff PIN không chính xác (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_STAFF_PIN",
    "message": "Mã PIN nhân viên thu ngân không chính xác.",
    "details": null
  },
  "timestamp": "2026-11-05T20:50:30.000Z"
}
```

- **Số tiền hóa đơn không hợp lệ (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_BILL_AMOUNT",
    "message": "Số tiền hóa đơn tích điểm tối thiểu phải từ 1.00 NOK trở lên.",
    "details": {
      "billAmount": 0.00
    }
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

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` | Mã định danh thiết bị Kiosk |
| `Content-Type` | `String` | **Có** | `application/json` | Định dạng dữ liệu |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `storeId` | `UUID` | Có | UUID hợp lệ | Chi nhánh cửa hàng |
| `kioskDeviceId` | `String` | Có | Mã thiết bị | Định danh Kiosk đang thực hiện |
| `customerPhone` | `String` | Có | Định dạng E.164 | SĐT khách hàng tích điểm |
| `customerName` | `String` | Không | Tối đa 128 ký tự | Tên khách hàng |
| `tipAmount` | `BigDecimal` | Không | `@DecimalMin("0.00")` | Tiền tip |
| `paymentMethod` | `String` | Có | `CARD`, `VIPPS` | Phương thức thanh toán |
| `items` | `List` | Có | Tối thiểu 1 món | Danh sách món ăn đặt |

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

#### 3. Phản hồi Thành công (201 Created)
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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:51:00.000Z"
}
```

- **Món ăn trong giỏ không hợp lệ hoặc ngừng bán (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Một hoặc nhiều món ăn đã chọn không còn tồn tại hoặc tạm ngưng phục vụ.",
    "details": null
  },
  "timestamp": "2026-11-05T20:51:00.000Z"
}
```

- **Lỗi xử lý đầu đọc thẻ POS (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "CARD_READER_ERROR",
    "message": "Giao dịch thanh toán thẻ không thành công hoặc bị từ chối bởi ngân hàng.",
    "details": {
      "terminalStatus": "DECLINED"
    }
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
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `X-Kiosk-Device-ID` | `String` | **Có** | `kiosk-pos-sentrum-01` | Mã định danh thiết bị Kiosk |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `storeId` | `UUID` | Có | UUID hợp lệ | Chi nhánh cửa hàng |
| `reason` | `String` | Có | `IDLE_TIMEOUT`, `USER_MANUAL_RESET`, `TRANSACTION_COMPLETED` | Nguyên nhân reset phiên |

```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "reason": "IDLE_TIMEOUT"
}
```

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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
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

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `deviceId` | `String` | Có | Định danh phần cứng Kiosk (VD: `kiosk-pos-sentrum-01`) |

#### 3. Phản hồi Thành công (200 OK)
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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:52:00.000Z"
}
```

- **Không tìm thấy thiết bị Kiosk (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "KIOSK_DEVICE_NOT_FOUND",
    "message": "Thiết bị Kiosk không tồn tại hoặc chưa được đăng ký trong hệ thống chi nhánh.",
    "details": {
      "deviceId": "kiosk-unknown-01"
    }
  },
  "timestamp": "2026-11-05T20:52:00.000Z"
}
```

---

## Ghi Chú Kỹ Thuật & Chu Trình Trạng Thái Kiosk (Technical & Kiosk State Machine Notes)

1. **Chu Trình Đếm Ngược & Tự Động Reset (60-second Idle Watchdog):**
   - Màn hình Kiosk duy trì đồng hồ đếm ngược 60 giây (`countdown`). Bất kỳ thao tác chạm màn hình nào sẽ reset thời gian chờ trở lại 60 giây.
   - Khi bộ đếm về 0 hoặc khách hàng nhấn nút `[Start Over]`, giao diện Frontend tự động gọi `POST /api/v1/kiosk/session/reset` để xóa sạch dữ liệu tạm trong Redis Cache và trở về màn hình chờ ban đầu.

2. **Quy Tắc Tích Điểm Nhanh Tại Quầy (`KIOSK_EARN`):**
   - Khi khách gọi món và trả tiền mặt/quẹt POS truyền thống tại quầy thu ngân, nhân viên xuất hóa đơn giấy và mời khách gõ số điện thoại lên màn hình Kiosk phụ (`07.02`).
   - Hệ thống tự động tính điểm theo hạng thẻ hiện tại của khách và ghi nhận trực tiếp vào sổ cái bất biến `points_ledger` với `transaction_type = 'KIOSK_EARN'`, `reference_type = 'EXTERNAL_RECEIPT'`, và `reference_id = externalReceiptRef`.

3. **Cơ Chế Giám Sát Phần Cứng & Heartbeat:**
   - Định kỳ mỗi 60 giây, ứng dụng Kiosk gửi tín hiệu kiểm tra tình trạng (`cardReaderStatus`, `printerPaperStatus`) về Backend để hiển thị cảnh báo cho quản lý nhà hàng nếu Kiosk hết giấy in bill hoặc mất kết nối máy quẹt thẻ.
