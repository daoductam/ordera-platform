# Phân hệ 06: Đơn hàng, Thanh toán & Biên nhận E-Receipt API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Giỏ hàng, Ghi chú Đơn, Tính Thuế VAT (15%), Tips, Checkout Đặt món, Đặt lại (Reorder) & Gửi Hóa đơn Email**  
> **Ánh xạ Prototype:** `docs/prototype/6-gio-hang-don-hang-va-bien-nhan/` (`06.01` đến `06.10`)  
> **Ánh xạ Frontend:** `frontend/src/pages/CartPage.tsx`, `frontend/src/pages/OrdersPage.tsx`, `CheckoutView.tsx`, `ReorderCartView.tsx`, `EReceiptModal.tsx`  
> **Bảng Database:** `carts`, `cart_items`, `orders`, `order_items`, `order_item_modifiers`, `points_ledger`

---

### Danh sách Endpoint trong Phân hệ 06

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 6.1 | `/api/v1/cart` | `GET` | Lấy chi tiết giỏ hàng hiện tại (món ăn, size, topping, ghi chú) | `06.06` |
| 6.2 | `/api/v1/cart/items/{itemId}` | `PATCH` / `DELETE` | Cập nhật số lượng (+/-) hoặc xóa món khỏi giỏ | `06.06` |
| 6.3 | `/api/v1/cart/comment` | `PUT` | Cập nhật ghi chú đơn hàng (dị ứng, yêu cầu đặc biệt tối đa 200 ký tự) | `06.06` |
| 6.4 | `/api/v1/orders/checkout/preview` | `POST` | Dự toán bảng tính Checkout (VAT 15%, Tips, Voucher, Quà tặng & Điểm thưởng dự kiến `+170 pts`) | `06.07`, `08.07`, `08.10` |
| 6.5 | `/api/v1/orders/checkout` | `POST` | Khởi tạo đơn hàng chính thức & xử lý thanh toán (Vipps / Card) | `06.07`, `08.10` |
| 6.6 | `/api/v1/orders` | `GET` | Danh sách đơn hàng phân chia theo tab (`Active Orders` vs `Past Orders`) | `06.02`, `06.03` |
| 6.7 | `/api/v1/orders/{orderId}` | `GET` | Chi tiết đơn hàng đầy đủ kèm mã số thứ tự nhận món (`#005`) | `06.04`, `06.05` |
| 6.8 | `/api/v1/orders/{orderId}/reorder` | `POST` | Đặt lại đơn hàng cũ nhanh chóng 1-click (Reorder flow) | `06.02`, `06.03`, `06.05` |
| 6.9 | `/api/v1/orders/{orderId}/receipt/send-email` | `POST` | Gửi biên nhận điện tử E-Receipt định dạng PDF tới Email của khách | `06.08`, `06.09`, `06.10` |

---

### API 6.1: Lấy Giỏ Hàng Hiện Tại (Get My Cart)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/cart`
- **Operation ID:** `getMyCart`
- **Mô tả:** Lấy toàn bộ món ăn trong giỏ hàng hiện tại kèm các tùy chọn modifier (size, topping, mức đá, mức ngọt), số lượng và ghi chú đặc biệt phục vụ màn hình `06.06`.
- **Màn hình Prototype:** `06.06`
- **Frontend Gọi tại:** `CartPage.tsx`, `ReorderCartView.tsx`, `CartContext.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` | Token của khách đăng nhập (hoặc Session ID) |

#### 2. Phản hồi Thành công — Giỏ Hàng Có Món (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "storeName": "Linn Sushi Sentrum",
    "totalItemCount": 3,
    "items": [
      {
        "itemId": "ci1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
        "productName": "Aurora",
        "quantity": 2,
        "itemPrice": 89.00,
        "subtotalPrice": 178.00,
        "selectedOptions": {
          "size": "L",
          "toppings": ["Mango Popping", "Strawberry Popping", "Tapioca"],
          "sweetness": "Standard",
          "ice": "Less Ice"
        }
      },
      {
        "itemId": "ci2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "productId": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
        "productName": "Tuna Maki",
        "quantity": 1,
        "itemPrice": 72.00,
        "subtotalPrice": 72.00,
        "selectedOptions": {
          "size": "Standard (8 pcs)",
          "toppings": []
        }
      }
    ],
    "comment": "Special request, allergies, etc",
    "subtotalAmount": 250.00,
    "currency": "NOK"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Thành công — Giỏ Hàng Trống (`06.01`)
```json
{
  "success": true,
  "message": "Cart is currently empty",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "totalItemCount": 0,
    "items": [],
    "comment": null,
    "subtotalAmount": 0.00,
    "currency": "NOK"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.2: Cập Nhật Số Lượng hoặc Xóa Món trong Giỏ (Update / Delete Cart Item)

- **HTTP Method:** `PATCH` (Tăng/giảm số lượng) hoặc `DELETE` (Xóa món)
- **URL Path:** `/api/v1/cart/items/{itemId}`
- **Operation ID:** `updateOrDeleteCartItem`
- **Mô tả:** Khách hàng bấm nút `+` hoặc `-` tại màn hình `06.06`. Nếu giảm về 0 hoặc gọi HTTP DELETE, món ăn sẽ bị xóa hoàn toàn khỏi giỏ hàng.
- **Màn hình Prototype:** `06.06`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `itemId` | `UUID` | **Có** | ID dòng món trong bảng `cart_items` |

#### 3. Request Body Schema (Cho PATCH)
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `quantity` | `Integer` | Có | `@Min(1)`, `@Max(99)` | Số lượng mới sau khi thay đổi |

```json
{
  "quantity": 3
}
```

#### 4. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Cart item updated successfully",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "updatedItemId": "ci1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "newQuantity": 3,
    "itemSubtotalPrice": 267.00,
    "newCartSubtotal": 339.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 5. Phản hồi Lỗi
- **Không tìm thấy món ăn trong giỏ (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "CART_ITEM_NOT_FOUND",
    "message": "Món ăn này không còn tồn tại trong giỏ hàng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.3: Cập Nhật Ghi Chú Đơn Hàng (Update Cart Comment)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/cart/comment`
- **Operation ID:** `updateCartComment`
- **Mô tả:** Khách hàng gõ lưu ý đặc biệt hoặc thông tin dị ứng vào ô *"Special request, allergies, etc"* trong màn hình `06.06`.
- **Màn hình Prototype:** `06.06`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `comment` | `String` | Không | `@Size(max=200)` | Nội dung lưu ý tối đa 200 ký tự |

```json
{
  "comment": "Dị ứng đậu phộng, vui lòng không cho mè rắc lên sushi."
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Cart comment updated successfully",
  "data": {
    "comment": "Dị ứng đậu phộng, vui lòng không cho mè rắc lên sushi."
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.4: Dự Toán Bảng Tính Checkout (Preview Checkout Calculation)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/checkout/preview`
- **Operation ID:** `previewOrderCalculation`
- **Mô tả:** Nhận ID giỏ hàng, mã voucher áp dụng, quà tặng tri ân và số tiền tip để tính toán chính xác số tiền giảm trừ, tiền thuế VAT 15% (bao gồm trong giá) và dự kiến số điểm tích lũy (`+170 pts`) trước khi khách bấm nút Pay (`06.07`, `08.07`, `08.10`).
- **Màn hình Prototype:** `06.07`, `08.07`, `08.10`
- **Frontend Gọi tại:** `CheckoutView.tsx`, `CartContext.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng |
| `voucherCode` | `String` | Không | Mã hợp lệ | Mã voucher nếu có (vd: `DISCOUNT30`) |
| `rewardGiftId` | `UUID` | Không | UUID quà tặng | Món quà tri ân nếu chọn |
| `tipAmount` | `BigDecimal` | Không | `@DecimalMin("0.00")` | Tiền tip lựa chọn (`0`, `5.00`, `10.00`, `Custom`) |
| `pickupMethod` | `String` | Có | `DINE_IN` hoặc `TAKEAWAY` | Hình thức dùng tại quán hoặc mang đi |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "voucherCode": "DISCOUNT30",
  "rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
  "tipAmount": 10.00,
  "pickupMethod": "DINE_IN"
}
```

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Bảng dự toán chi phí đơn hàng",
  "data": {
    "subtotalAmount": 250.00,
    "discountAmount": 75.00,
    "tipAmount": 10.00,
    "vatAmount": 22.83,
    "vatPercentage": 15.0,
    "finalTotalAmount": 185.00,
    "currency": "NOK",
    "pointsToEarn": 170,
    "appliedVoucher": {
      "code": "DISCOUNT30",
      "title": "DISCOUNT30%",
      "discountValueText": "-30 %",
      "discountAmount": 75.00
    },
    "selectedGift": {
      "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
      "title": "Cốc giữ nhiệt"
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.5: Khởi Tạo Đơn Hàng Chính Thức & Thanh Toán (Checkout Order)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/checkout`
- **Operation ID:** `checkoutOrder`
- **Mô tả:** Tiếp nhận thông tin giỏ hàng, thông tin người nhận (Bàn ăn / Quầy nhận), khóa voucher sang `USED`, tạo đơn hàng `orders`, các dòng món `order_items`, ghi nhận điểm tích lũy vào `points_ledger` (TransactionType = `ORDER_EARN`) và sinh mã nhận món `#005`.
- **Khóa Phân Tán:** Chiếm giữ Redis Lock `lock:order:checkout:{userId}` và `lock:voucher:{voucherCode}` trong 5 giây.
- **Màn hình Prototype:** `06.07`, `08.10`
- **Frontend Gọi tại:** `CheckoutView.tsx` (Nút: `Pay 185.00 Kr`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` | Token của khách |
| `Idempotency-Key`| `String` | Khuyến nghị | `idem-9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d` | Ngăn chặn trừ tiền trùng lặp |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng đặt món |
| `pickupMethod` | `String` | Có | `DINE_IN` hoặc `TAKEAWAY` | Hình thức nhận món |
| `tableNumber` | `String` | Khi chọn DINE_IN | `@Size(max=32)` | Số bàn ăn (vd: *Table 05*) |
| `pickupPersonName` | `String` | Có | `@NotBlank`, `@Size(max=128)` | Tên người nhận (vd: *David Nguyen*) |
| `pickupPersonPhone`| `String` | Có | Định dạng E.164 | SĐT nhận thông báo SMS và tích điểm |
| `voucherCode` | `String` | Không | Chuỗi mã | Mã voucher áp dụng |
| `rewardGiftId` | `UUID` | Không | UUID quà tặng | Món quà tri ân |
| `tipAmount` | `BigDecimal` | Không | `@DecimalMin("0.00")` | Tiền boa tip |
| `paymentMethod` | `String` | Có | `VIPPS`, `CARD`, `APPLE_PAY` | Cổng thanh toán |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "pickupMethod": "DINE_IN",
  "tableNumber": "Table 05",
  "pickupPersonName": "David Nguyen",
  "pickupPersonPhone": "+4797362748",
  "voucherCode": "DISCOUNT30",
  "rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
  "tipAmount": 10.00,
  "paymentMethod": "VIPPS"
}
```

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đơn hàng của bạn đã được tiếp nhận và thanh toán thành công qua Vipps",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "orderNumber": "#005",
    "orderCode": "ORD-20261105-0891",
    "pickupMethod": "DINE_IN",
    "tableNumber": "Table 05",
    "totalAmount": 185.00,
    "pointsEarned": 170,
    "status": "PREPARING",
    "estimatedReadyMinutes": 10,
    "paymentStatus": "PAID",
    "paymentMethod": "VIPPS",
    "createdAt": "2026-11-05T14:32:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Voucher không đủ điều kiện đơn tối thiểu (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "MIN_ORDER_VALUE_NOT_MET",
    "message": "Đơn hàng chưa đạt giá trị tối thiểu 250.00 NOK của Voucher (Hiện tại: 180.00 NOK).",
    "details": {
      "currentSubtotal": 180.00,
      "requiredMinOrder": 250.00
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Xung đột thanh toán lặp lại (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "IDEMPOTENT_OPERATION_CONFLICT",
    "message": "Yêu cầu thanh toán đơn hàng này đang được xử lý, vui lòng không nhấn gửi lại.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.6: Danh Sách Đơn Hàng Của Tôi (List My Orders)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders`
- **Operation ID:** `getMyOrdersList`
- **Mô tả:** Lấy danh sách các đơn hàng của thành viên đăng nhập, phân chia theo 2 tab chính: `Active Orders` (đang chế biến, sẵn sàng nhận món) hoặc `Past Orders` (đã hoàn tất hoặc hủy) theo prototype `06.02` và `06.03`.
- **Màn hình Prototype:** `06.02`, `06.03`
- **Frontend Gọi tại:** `OrdersPage.tsx`, `ActiveOrdersView.tsx`, `OrderHistoryView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `tab` | `String` | `ACTIVE` | Lọc theo tab: `ACTIVE` hoặc `PAST` |
| `page` | `Integer` | `0` | Số trang (bắt đầu từ 0) |
| `size` | `Integer` | `10` | Số đơn trên một trang |

#### 3. Phản hồi Thành công (200 OK)
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
        "orderNumber": "#005",
        "orderCode": "ORD-20261105-0891",
        "storeName": "Linn Sushi Sentrum",
        "pickupMethod": "COUNTER",
        "totalAmount": 185.00,
        "itemCount": 3,
        "firstItemName": "Aurora",
        "status": "PREPARING",
        "estimatedReadyMinutes": 10,
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

---

### API 6.7: Chi Tiết Đơn Hàng & Tiến Trình Bếp (Get Order Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders/{orderId}`
- **Operation ID:** `getOrderDetails`
- **Mô tả:** Lấy thông tin chi tiết một đơn hàng kèm số thứ tự nhận món (`#005`), các bước tiến trình (`Placed` $\rightarrow$ `In kitchen` $\rightarrow$ `Ready for pickup`), địa chỉ cửa hàng và danh sách món ăn (`06.04`, `06.05`).
- **Màn hình Prototype:** `06.04`, `06.05`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `orderId` | `UUID` | **Có** | `o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001` |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Order details retrieved successfully",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "orderNumber": "#005",
    "orderCode": "ORD-20261105-0891",
    "store": {
      "name": "Linn Sushi Sentrum",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "phoneNumber": "+4722334455"
    },
    "status": "PREPARING",
    "timeline": [
      { "step": "PLACED", "time": "14:32", "isDone": true },
      { "step": "IN_KITCHEN", "time": "14:34", "isDone": true },
      { "step": "READY_FOR_PICKUP", "time": "14:44", "isDone": false }
    ],
    "estimatedReadyMinutes": 10,
    "pickupMethod": "COUNTER",
    "tableNumber": null,
    "items": [
      {
        "productName": "Aurora",
        "quantity": 2,
        "unitPrice": 89.00,
        "modifiersSummary": "Size: L, Topping: Mango Popping"
      },
      {
        "productName": "Tuna Maki",
        "quantity": 1,
        "unitPrice": 72.00,
        "modifiersSummary": "Size: Standard"
      }
    ],
    "financialSummary": {
      "subtotalAmount": 250.00,
      "discountAmount": 75.00,
      "tipAmount": 10.00,
      "vatAmount": 22.83,
      "totalAmount": 185.00,
      "paymentMethod": "VIPPS",
      "paymentStatus": "PAID"
    },
    "pointsEarned": 170
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.8: Đặt Lại Đơn Hàng Nhanh 1-Click (Reorder Past Order)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/{orderId}/reorder`
- **Operation ID:** `reorderPastOrder`
- **Mô tả:** Sao chép toàn bộ món ăn và tùy chọn của đơn cũ vào giỏ hàng mới. Nút `[Reorder]` tại `06.02`, `06.03` và `06.05` sẽ kích hoạt API này và điều hướng người dùng thẳng tới màn hình giỏ hàng `06.06`.
- **Màn hình Prototype:** `06.02`, `06.03`, `06.05`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `orderId` | `UUID` | **Có** | `o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001` |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Các món từ đơn cũ đã được tải lại vào giỏ hàng của bạn.",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "itemCount": 3,
    "subtotalAmount": 250.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.9: Gửi Biên Nhận Điện Tử E-Receipt qua Email (Send E-Receipt to Email)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/{orderId}/receipt/send-email`
- **Operation ID:** `sendReceiptToEmail`
- **Mô tả:** Khách hàng nhập email trong popup `06.08` và nhấn `[Send]`. Backend tạo file PDF hóa đơn điện tử E-Receipt và gửi qua dịch vụ SMTP. Sau khi gửi thành công, Frontend chuyển sang popup `06.10 Email sent`.
- **Màn hình Prototype:** `06.08`, `06.09`, `06.10`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `orderId` | `UUID` | **Có** | `o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001` |

#### 3. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `email` | `String` | Có | `@NotBlank`, `@Email` | Địa chỉ email nhận hóa đơn PDF |

```json
{
  "email": "info@ordera.no"
}
```

#### 4. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Email sent. The receipt is sent to the email.",
  "data": {
    "sentToEmail": "info@ordera.no",
    "orderNumber": "#005",
    "dispatchedAt": "2026-11-05T20:46:00Z"
  },
  "timestamp": "2026-11-05T20:46:00.000Z"
}
```

#### 5. Phản hồi Lỗi
- **Email sai định dạng (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_EMAIL_FORMAT",
    "message": "Địa chỉ email không đúng định dạng nhận thư.",
    "details": null
  },
  "timestamp": "2026-11-05T20:46:00.000Z"
}
```
