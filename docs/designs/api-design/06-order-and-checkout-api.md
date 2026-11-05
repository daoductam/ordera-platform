# Phân hệ 06: Đơn hàng & Thanh toán API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Giỏ hàng, Tính Thuế VAT (15%), Tips, Checkout Đặt món & Hóa đơn Điện tử E-Receipt**  
> **Ánh xạ Frontend:** `frontend/src/pages/CartPage.tsx`, `frontend/src/pages/OrdersPage.tsx`, `CheckoutView.tsx`, `EReceiptModal.tsx`, `ActiveOrdersView.tsx`, `OrderHistoryView.tsx`  
> **Bảng Database:** `orders`, `order_items`, `order_item_modifiers`, `user_vouchers`, `points_ledger`

---

### API 6.1: Xem Trước & Tính Tiền Giỏ Hàng (Preview Checkout Calculation)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/preview`
- **Operation ID:** `previewOrderCalculation`
- **Mô tả:** Nhận danh sách món trong giỏ hàng, ID voucher áp dụng (nếu có) và số tiền tip để tính toán chính xác số tiền giảm trừ, tiền thuế VAT 15% và dự kiến số điểm tích lũy trước khi khách bấm nút Pay.
- **Frontend Gọi tại:** `CheckoutView.tsx`, `CartContext.tsx`

#### 1. Request Body Schema
```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "appliedVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
  "tipAmount": 10.00,
  "items": [
    {
      "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "quantity": 2,
      "modifierIds": [
        "f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01"
      ]
    },
    {
      "productId": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
      "quantity": 1,
      "modifierIds": []
    }
  ]
}
```

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Order calculation preview completed",
  "data": {
    "currency": "NOK",
    "subtotalAmount": 267.00,
    "discountAmount": 100.00,
    "tipAmount": 10.00,
    "vatAmount": 23.09,
    "totalAmount": 177.00,
    "pointsToEarn": 170,
    "appliedVoucher": {
      "code": "SAVE100",
      "discountValue": 100.00,
      "isValid": true
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.2: Tạo Đơn Hàng & Thanh Toán (Checkout Order)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/checkout`
- **Operation ID:** `checkoutOrder`
- **Mô tả:** Tiếp nhận thông tin giỏ hàng, thông tin người nhận (Takeaway/Table), khóa voucher sang `IN_USE` $\rightarrow$ `USED`, tạo đơn hàng `orders`, các dòng món `order_items` và chi tiết `order_item_modifiers`, ghi nhận điểm tích lũy vào `points_ledger` (TransactionType = `ORDER_EARN`).
- **Khóa Phân Tán:** Chiếm giữ Redis Lock `lock:order:checkout:{userId}` và `lock:voucher:{voucherId}` trong tối đa 5 giây.
- **Frontend Gọi tại:** `CheckoutView.tsx` (Nút: `Pay 177.00 Kr`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` (Null nếu khách vãng lai) |
| `Idempotency-Key`| `String` | Khuyến nghị | `idem-9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `storeId` | `UUID` | Có | Phải là Store hợp lệ | Chi nhánh cửa hàng chế biến món |
| `pickupMethod` | `String` | Có | `COUNTER` hoặc `TABLE` | Hình thức nhận món |
| `pickupTimeType` | `String` | Có | `ASAP` hoặc `SCHEDULED` | Thời gian nhận đồ |
| `scheduledPickupAt`| `Instant` | Khi chọn SCHEDULED | Tương lai trong ngày | Khung giờ hẹn |
| `tableNumber` | `String` | Khi chọn TABLE | `@Size(max=32)` | Số bàn ăn (vd: *Table 05*) |
| `pickupPersonName` | `String` | Có | `@NotBlank`, `@Size(max=128)` | Tên người nhận (vd: *David Nguyen*) |
| `pickupPersonPhone`| `String` | Có | Định dạng E.164 | SĐT người nhận để tích điểm |
| `appliedVoucherId` | `UUID` | Không | ID trong `user_vouchers` | Voucher áp dụng |
| `tipAmount` | `BigDecimal` | Không | `@DecimalMin("0.00")` | Tiền boa tip (Mặc định: 0.00) |
| `paymentMethod` | `String` | Có | `CARD`, `VIPPS`, `APPLE_PAY`, `COUNTER_CASH` | Phương thức thanh toán |
| `orderNotes` | `String` | Không | `@Size(max=255)` | Ghi chú đơn hàng |
| `items` | `List` | Có | `@NotEmpty` | Danh sách món ăn đặt |

```json
{
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "pickupMethod": "COUNTER",
  "pickupTimeType": "ASAP",
  "scheduledPickupAt": null,
  "tableNumber": null,
  "pickupPersonName": "David Nguyen",
  "pickupPersonPhone": "+4797362748",
  "appliedVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
  "tipAmount": 10.00,
  "paymentMethod": "VIPPS",
  "orderNotes": "Không cho gừng đỏ",
  "items": [
    {
      "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "quantity": 2,
      "specialInstruction": "Ít cay",
      "modifiers": [
        {
          "modifierGroupId": "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380e01",
          "modifierId": "f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01"
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

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đơn hàng đã được tạo và thanh toán thành công!",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "orderCode": "ORD-20261105-0891",
    "storeName": "Linn Sushi Sentrum",
    "pickupMethod": "COUNTER",
    "pickupPersonName": "David Nguyen",
    "subtotalAmount": 267.00,
    "discountAmount": 100.00,
    "tipAmount": 10.00,
    "vatAmount": 23.09,
    "totalAmount": 177.00,
    "pointsEarned": 170,
    "status": "CONFIRMED",
    "paymentStatus": "PAID",
    "paymentMethod": "VIPPS",
    "estimatedReadyMinutes": 20,
    "createdAt": "2026-11-05T14:32:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi Chi tiết
- **Voucher không đủ điều kiện đơn tối thiểu (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "MIN_ORDER_VALUE_NOT_MET",
    "message": "Đơn hàng chưa đạt giá trị tối thiểu 250.00 Kr của Voucher SAVE100 (Giá trị hiện tại: 180.00 Kr).",
    "details": {
      "currentSubtotal": 180.00,
      "requiredMinOrder": 250.00
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Voucher đã bị dùng ở phiên khác (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "VOUCHER_ALREADY_USED",
    "message": "Voucher này đã được áp dụng trong một đơn hàng khác hoặc không còn khả dụng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.3: Hóa Đơn Điện Tử E-Receipt (Get Order E-Receipt)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders/{orderCode}/receipt`
- **Operation ID:** `getOrderReceiptByCode`
- **Mô tả:** Lấy thông tin hóa đơn điện tử đầy đủ gồm mã Barcode / QR Code, danh sách món, chiết khấu, tip, thuế VAT đã bao gồm, và điểm thưởng đã cộng để hiển thị màn hình `06.03`.
- **Frontend Gọi tại:** `EReceiptModal.tsx`, `OrdersPage.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `orderCode` | `String` | **Có** | `ORD-20261105-0891` |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "E-Receipt retrieved successfully",
  "data": {
    "orderCode": "ORD-20261105-0891",
    "store": {
      "name": "Linn Sushi Sentrum",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "phoneNumber": "+4722334455",
      "orgNumber": "NO 987 654 321 MVA"
    },
    "pickupDetails": {
      "method": "COUNTER",
      "tableNumber": null,
      "personName": "David Nguyen",
      "personPhone": "+4797362748",
      "orderTime": "2026-11-05T14:32:00Z"
    },
    "items": [
      {
        "id": "oi1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "productName": "Aurora",
        "unitPrice": 89.00,
        "quantity": 2,
        "itemTotal": 178.00,
        "modifiersSummary": "Size: Tiêu chuẩn (8 miếng)",
        "specialInstruction": "Ít cay"
      },
      {
        "id": "oi2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "productName": "Tuna Maki",
        "unitPrice": 89.00,
        "quantity": 1,
        "itemTotal": 89.00,
        "modifiersSummary": "",
        "specialInstruction": null
      }
    ],
    "financialSummary": {
      "currency": "NOK",
      "subtotalAmount": 267.00,
      "discountAmount": 100.00,
      "appliedVoucherCode": "SAVE100",
      "tipAmount": 10.00,
      "vatAmount": 23.09,
      "totalAmount": 177.00,
      "paymentMethod": "VIPPS",
      "paymentStatus": "PAID"
    },
    "loyaltyAward": {
      "pointsEarned": 170,
      "memberNewBalance": 290
    },
    "barcodeUrl": "https://api.ordera.no/v1/barcodes/ORD-20261105-0891.png",
    "qrCodeUrl": "https://api.ordera.no/v1/qrcodes/ORD-20261105-0891.png"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.4: Danh Sách Đơn Hàng Của Tôi (List My Orders)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders/my-orders`
- **Operation ID:** `getMyOrders`
- **Mô tả:** Lấy danh sách các đơn hàng của thành viên đăng nhập, phân loại theo trạng thái: Đang xử lý (`Active`) hoặc Đã hoàn tất (`History`).
- **Frontend Gọi tại:** `OrdersPage.tsx`, `ActiveOrdersView.tsx`, `OrderHistoryView.tsx`

#### 1. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `statusGroup` | `String` | `ALL` | `ACTIVE` (Pending, Confirmed, Preparing, Ready) hoặc `HISTORY` (Completed, Cancelled) |
| `page` | `Integer` | `0` | Số trang |
| `size` | `Integer` | `10` | Số đơn trên một trang |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Customer orders retrieved successfully",
  "data": {
    "content": [
      {
        "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "orderCode": "ORD-20261105-0891",
        "storeName": "Linn Sushi Sentrum",
        "pickupMethod": "COUNTER",
        "totalAmount": 177.00,
        "itemCount": 3,
        "firstItemName": "Aurora",
        "status": "PREPARING",
        "createdAt": "2026-11-05T14:32:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
