# Phân hệ 06: Đơn hàng, Thanh toán & Biên nhận E-Receipt API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Giỏ hàng, Ghi chú Đơn, Tính Thuế VAT (15%), Tips, Checkout Đặt món, Đặt lại (Reorder) & Gửi Hóa đơn Email**  
> **Ánh xạ Prototype:** `docs/prototype/6-gio-hang-don-hang-va-bien-nhan/` (`06.01` đến `06.10`)  
> **Ánh xạ Frontend:** `frontend/src/pages/CartPage.tsx`, `frontend/src/pages/OrdersPage.tsx`, `CheckoutView.tsx`, `ReorderCartView.tsx`  
> **Bảng Database:** `carts`, `cart_items`, `orders`, `order_items`, `order_item_modifiers`, `points_ledger`

---

### Danh sách Endpoint trong Phân hệ 06

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 6.1 | `/api/v1/cart` | `GET` | Lấy chi tiết giỏ hàng hiện tại (món ăn, size, topping, ghi chú) | `06.06` |
| 6.2 | `/api/v1/cart/items/{itemId}` | `PATCH` / `DELETE` | Cập nhật số lượng (+/-) hoặc xóa món khỏi giỏ | `06.06` |
| 6.3 | `/api/v1/cart/comment` | `PUT` | Cập nhật ghi chú đơn hàng (dị ứng, yêu cầu đặc biệt tối đa 200 ký tự) | `06.06` |
| 6.4 | `/api/v1/orders/checkout/preview` | `POST` | Dự toán bảng tính Checkout (VAT, Tips, Voucher, Quà tặng & Điểm thưởng dự kiến `+170 pts`) | `06.07`, `08.07`, `08.10` |
| 6.5 | `/api/v1/orders/checkout` | `POST` | Khởi tạo đơn hàng chính thức & xử lý thanh toán (Vipps / Card) | `06.07`, `08.10` |
| 6.6 | `/api/v1/orders` | `GET` | Danh sách đơn hàng phân chia theo tab (`Active Orders` vs `Past Orders`) | `06.02`, `06.03`, `06.04` |
| 6.7 | `/api/v1/orders/{orderId}` | `GET` | Chi tiết đơn hàng đầy đủ kèm mã số thứ tự nhận món (`#005`) | `06.05` |
| 6.8 | `/api/v1/orders/{orderId}/reorder` | `POST` | Đặt lại đơn hàng cũ nhanh chóng 1-click (Reorder flow) | `06.03`, `06.05`, `06.06` |
| 6.9 | `/api/v1/orders/{orderId}/receipt/send-email` | `POST` | Gửi biên nhận điện tử E-Receipt định dạng PDF tới Email của khách | `06.08`, `06.09`, `06.10` |

---

### API 6.1: Lấy Giỏ Hàng Hiện Tại (Get Cart)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/cart`
- **Operation ID:** `getMyCart`
- **Mô tả:** Lấy toàn bộ món ăn trong giỏ hàng kèm các tùy chọn size, topping, mức đá, mức ngọt và ghi chú đơn hàng.
- **Màn hình Prototype:** `06.06`

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "totalItemCount": 3,
    "items": [
      {
        "itemId": "ci1",
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
        "itemId": "ci2",
        "productId": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
        "productName": "Tuna Maki",
        "quantity": 1,
        "itemPrice": 89.00,
        "subtotalPrice": 89.00,
        "selectedOptions": {
          "size": "L",
          "toppings": ["Mango Popping", "Strawberry Popping", "Tapioca"],
          "sweetness": "Standard",
          "ice": "Less Ice"
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

---

### API 6.2: Cập Nhật Số Lượng Món (Update Cart Item Quantity)

- **HTTP Method:** `PATCH`
- **URL Path:** `/api/v1/cart/items/{itemId}`
- **Operation ID:** `updateCartItemQuantity`
- **Mô tả:** Tăng giảm số lượng qua nút `[-]` hoặc `[+]` tại trang giỏ hàng `06.06`.

#### 1. Request Body Schema
```json
{
  "quantity": 2
}
```

---

### API 6.3: Cập Nhật Ghi Chú Đơn Hàng (Update Cart Comment)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/cart/comment`
- **Operation ID:** `updateCartComment`
- **Mô tả:** Lưu ghi chú đơn hàng của khách hàng (giới hạn 200 ký tự) trên màn hình `06.06`.

#### 1. Request Body Schema
```json
{
  "comment": "Không cho hành lá và gừng đỏ, ít sốt cay"
}
```

---

### API 6.4: Dự Toán Bảng Tính Checkout (Preview Checkout Calculation)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/checkout/preview`
- **Operation ID:** `previewCheckoutCalculation`
- **Mô tả:** Dự toán bảng tính tiền chi tiết, áp mã voucher, quà tặng, tiền tip và tính toán số điểm thưởng hội viên sẽ nhận được (`+170 points`).
- **Màn hình Prototype:** `06.07`, `08.07`, `08.10`

#### 1. Request Body Schema
```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "pickupMethod": "COUNTER",
  "tipOption": "PERCENT_10",
  "customTipAmount": null,
  "voucherCode": "DISCOUNT30",
  "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Checkout preview calculated successfully",
  "data": {
    "currency": "NOK",
    "subtotalAmount": 267.00,
    "voucherDiscountAmount": 80.10,
    "giftItem": {
      "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
      "title": "Cốc giữ nhiệt",
      "price": 0.00
    },
    "tipAmount": 17.00,
    "vatAmount": 26.50,
    "vatRatePercent": 15.00,
    "finalTotalAmount": 203.90,
    "estimatedPointsEarned": 170,
    "earnRateApplied": 1.2
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.5: Khởi Tạo Đơn Hàng & Thanh Toán (Place & Pay Order)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/checkout`
- **Operation ID:** `checkoutAndPayOrder`
- **Mô tả:** Chốt đơn hàng chính thức từ giỏ hàng. Hỗ trợ nhận tại quầy (`COUNTER`) hoặc tại bàn (`TABLE`), thời gian (`ASAP` / `SCHEDULED`), đồng ý tích điểm cho khách vãng lai, và chọn cổng thanh toán Vipps / Stripe.
- **Màn hình Prototype:** `06.07`, `08.10`

#### 1. Request Body Schema
```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
  "pickupMethod": "COUNTER",
  "tableNumber": null,
  "pickupTimeType": "ASAP",
  "scheduledTime": null,
  "tipAmount": 0.00,
  "voucherCode": "DISCOUNT30",
  "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
  "customerName": "David Nguyen",
  "customerPhone": "+4797362748",
  "agreeToLoyaltyTerms": true,
  "paymentMethod": "VIPPS"
}
```

#### 2. Phản hồi Thành công (201 Created)
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "orderNumber": "#005",
    "orderCode": "ORD-20250331-005",
    "storeName": "Linn Sushi Sentrum",
    "pickupMethod": "Takeaway",
    "totalAmount": 267.00,
    "pointsEarned": 170,
    "status": "CONFIRMED",
    "paymentStatus": "PAID",
    "paymentUrl": null,
    "createdAt": "2025-03-31T14:59:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.6: Danh Sách Đơn Hàng Phân Tab (List Orders by Tab)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders`
- **Operation ID:** `listOrders`
- **Mô tả:** Lấy danh sách đơn hàng lọc theo tab: `ACTIVE` (đang xử lý) hoặc `PAST` (đã xong).
- **Màn hình Prototype:** `06.02`, `06.03`, `06.04`

#### 1. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `statusGroup` | `String` | `ACTIVE` | `ACTIVE` hoặc `PAST` |
| `page` | `Integer` | `0` | Số trang |
| `size` | `Integer` | `10` | Kích thước trang |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "content": [
      {
        "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "orderNumber": "#907",
        "storeName": "Shop 1",
        "storeCoverUrl": "https://storage.ordera.no/stores/shop1.jpg",
        "placedAtText": "Placed at 17 mar 2025 , 17:01",
        "totalAmountText": "290 NOK",
        "itemsSummary": "Dish name 1; Dish name 2",
        "status": "PREPARING"
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

### API 6.7: Chi Tiết Đơn Hàng (Get Order Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/orders/{orderId}`
- **Operation ID:** `getOrderDetails`
- **Mô tả:** Lấy thông tin đơn hàng đầy đủ để hiển thị màn hình số thứ tự quầy `#005` (`06.05`).

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Order details retrieved",
  "data": {
    "orderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "displayOrderNumber": "#005",
    "fullOrderReference": "Naomi - #005",
    "placedAt": "31 Mar, 2025 14:59",
    "storeName": "Shop name 1",
    "deliveryMethod": "Takeaway",
    "totalAmountText": "250, 00 NOK",
    "itemCount": 3,
    "items": [
      { "name": "Aurora", "quantity": 2, "price": 178.00 },
      { "name": "Tuna Maki", "quantity": 1, "price": 89.00 }
    ],
    "status": "COMPLETED"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 6.8: Đặt Lại Đơn Hàng Nhanh 1-Click (Reorder Past Order)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/orders/{orderId}/reorder`
- **Operation ID:** `reorderPastOrder`
- **Mô tả:** Sao chép toàn bộ món ăn và tùy chọn của đơn cũ vào giỏ hàng mới. Nút `[Reorder]` tại `06.03` và `06.05` sẽ kích hoạt API này và chuyển người dùng vào trang giỏ hàng `06.06`.

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Items reloaded into your cart",
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
- **Mô tả:** Khách hàng nhập email trong popup `06.08` và nhấn `[Send]`. Backend tạo file PDF hóa đơn điện tử và gửi qua hệ thống SMTP. Sau khi thành công, chuyển sang popup `06.10 Email sent`.

#### 1. Request Body Schema
```json
{
  "email": "info@ordera.no"
}
```

#### 2. Phản hồi Thành công (200 OK)
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
