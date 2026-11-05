# Phân hệ 05: Ví Voucher & Đổi Thưởng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Ví My Rewards, Danh mục Đổi quà, Đổi Voucher, Đổi Cốc Tumbler Hiện vật & Áp mã**  
> **Ánh xạ Frontend:** `frontend/src/pages/RewardsPage.tsx`, `MyRewardsView.tsx`, `GetMoreRewardsView.tsx`, `MyOffersDrawer.tsx`  
> **Bảng Database:** `vouchers`, `user_vouchers`, `loyalty_gifts`, `user_gift_redemptions`, `points_ledger`

---

### API 5.1: Ví Voucher Của Tôi (Get My Vouchers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/my-vouchers`
- **Operation ID:** `getMyVouchers`
- **Mô tả:** Lấy danh sách voucher mà thành viên đang sở hữu, phân chia theo trạng thái: `AVAILABLE` (Đang có hiệu lực), `IN_USE` (Đang gắn vào giỏ hàng chưa thanh toán), `USED` (Đã sử dụng), `EXPIRED` (Hết hạn).
- **Frontend Gọi tại:** `MyRewardsView.tsx` (Tabs: *Available / Expired & Used*)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Query Parameters
| Tham số | Kiểu | Mặc định | Mô tả |
|:---|:---|:---:|:---|
| `status` | `String` | `AVAILABLE` | Lọc trạng thái: `AVAILABLE`, `IN_USE`, `USED`, `EXPIRED` |

#### 3. Phản hồi Thành công — Có Voucher (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "My vouchers retrieved successfully",
  "data": [
    {
      "userVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "voucherId": "v1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "code": "SAVE100",
      "title": "Voucher 100 NOK",
      "description": "Giảm 100 NOK cho đơn hàng từ 250 NOK tại Linn Sushi Sentrum",
      "discountType": "FIXED_AMOUNT",
      "discountValue": 100.00,
      "minOrderValue": 250.00,
      "validFrom": "2026-11-01T00:00:00Z",
      "validUntil": "2026-12-03T23:59:59Z",
      "status": "AVAILABLE",
      "appliedOrderId": null,
      "redeemedAt": "2026-11-05T14:35:00Z"
    },
    {
      "userVoucherId": "uv2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
      "voucherId": "v2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
      "code": "OFF30",
      "title": "-30% Giảm giá Toàn Hóa đơn",
      "description": "Giảm tối đa 150 NOK cho đơn hàng dùng tại bàn",
      "discountType": "PERCENTAGE",
      "discountValue": 30.00,
      "minOrderValue": 0.00,
      "validFrom": "2026-11-01T00:00:00Z",
      "validUntil": "2026-11-30T23:59:59Z",
      "status": "IN_USE",
      "appliedOrderId": "o1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
      "redeemedAt": "2026-11-04T10:00:00Z"
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Thành công — Ví Rỗng (`07.05`, `07.06`)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "No vouchers found for given status",
  "data": [],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.2: Danh mục Đổi Thưởng Điểm (Get More Rewards Catalog)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/catalog`
- **Operation ID:** `getRewardsCatalog`
- **Mô tả:** Lấy toàn bộ danh sách ưu đãi có thể đổi bằng điểm: các mẫu voucher giảm giá và các phần quà hiện vật (ly giữ nhiệt tumbler).
- **Frontend Gọi tại:** `GetMoreRewardsView.tsx`

#### 1. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Rewards catalog retrieved successfully",
  "data": {
    "memberAvailablePoints": 120,
    "vouchers": [
      {
        "id": "v1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "code": "SAVE100",
        "title": "Voucher 100 NOK",
        "description": "Giảm 100 NOK cho đơn hàng tối thiểu 250 NOK",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 100.00,
        "pointsCost": 100,
        "minOrderValue": 250.00,
        "validUntil": "2026-12-03T23:59:59Z",
        "canAfford": true,
        "pointsNeeded": 0
      },
      {
        "id": "v3eebc99-9c0b-4ef8-bb6d-6bb9bd380003",
        "code": "VIP300",
        "title": "Voucher 300 NOK VIP",
        "description": "Áp dụng cho đơn hàng tiệc gia đình từ 800 NOK",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 300.00,
        "pointsCost": 1000,
        "minOrderValue": 800.00,
        "validUntil": "2026-12-31T23:59:59Z",
        "canAfford": false,
        "pointsNeeded": 880
      }
    ],
    "gifts": [
      {
        "id": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Cốc Giữ Nhiệt Tumbler Ordera Pink",
        "description": "Ly inox cao cấp 500ml giữ nóng 12h, lạnh 24h. Xuất trình mã tại quầy để nhận quà.",
        "imageUrl": "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd",
        "pointsCost": 100,
        "stockQuantity": 10,
        "canAfford": true,
        "pointsNeeded": 0,
        "inStock": true
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.3: Đổi Điểm Nhận Voucher (Redeem Voucher)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/rewards/vouchers/{voucherId}/redeem`
- **Operation ID:** `redeemVoucher`
- **Khóa Phân Tán:** Chiếm giữ Redis Lock `lock:member:{memberId}` trong 3 giây.
- **Frontend Gọi tại:** `GetMoreRewardsView.tsx`, `RewardDetailModal.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `voucherId` | `UUID` | **Có** | ID mẫu voucher trong catalog muốn đổi |

#### 2. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đổi Voucher 100 NOK thành công! Mã đã được lưu vào ví My Rewards.",
  "data": {
    "userVoucherId": "uv3ebc99-9c0b-4ef8-bb6d-6bb9bd380003",
    "code": "SAVE100",
    "title": "Voucher 100 NOK",
    "pointsSpent": 100,
    "remainingPoints": 20,
    "validUntil": "2026-12-03T23:59:59Z",
    "status": "AVAILABLE"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Điểm khả dụng không đủ (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "POINTS_INSUFFICIENT",
    "message": "Số dư điểm khả dụng của bạn (120 Points) không đủ để đổi ưu đãi này (Yêu cầu: 1,000 Points).",
    "details": {
      "currentPoints": 120,
      "requiredPoints": 1000,
      "pointsShortage": 880
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Xung đột bấm đúp liên tục (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "CONCURRENT_TRANSACTION_LOCK",
    "message": "Một giao dịch đổi thưởng khác của bạn đang được xử lý. Vui lòng không bấm liên tục.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.4: Đổi Điểm Nhận Quà Hiện Vật (Redeem Physical Gift)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/rewards/gifts/{giftId}/redeem`
- **Operation ID:** `redeemPhysicalGift`
- **Mô tả:** Đổi điểm lấy quà hiện vật (Cốc Tumbler). Trừ điểm, trừ 1 tồn kho trong `loyalty_gifts`, và sinh mã nhận quà 8 chữ số độc nhất (`redemption_code`).
- **Khóa Phân Tán:** Chiếm giữ đồng thời `lock:member:{memberId}` và `lock:gift:{giftId}`.

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `giftId` | `UUID` | **Có** | ID quà tặng trong bảng `loyalty_gifts` |

#### 2. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đổi quà thành công! Hãy xuất trình mã này cho nhân viên quầy thu ngân để nhận cốc.",
  "data": {
    "redemptionId": "gr1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "giftTitle": "Cốc Giữ Nhiệt Tumbler Ordera Pink",
    "pointsSpent": 100,
    "remainingPoints": 20,
    "redemptionCode": "GIFT-8942-PINK",
    "status": "PENDING",
    "instruction": "Xuất trình mã này tại quầy thu ngân bất kỳ chi nhánh Linn Sushi để nhận quà.",
    "createdAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Quà đã hết số lượng tồn kho (`409 Conflict`):**
```json
{
  "success": false,
  "error": {
    "code": "GIFT_OUT_OF_STOCK",
    "message": "Rất tiếc! Món quà này đã vừa hết số lượng tồn kho tại hệ thống.",
    "details": {
      "stockQuantity": 0
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.5: Danh sách Voucher Đủ Điều Kiện Cho Đơn Hàng (Get Eligible Vouchers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/checkout-eligible`
- **Operation ID:** `getEligibleVouchersForCheckout`
- **Mô tả:** Truy vấn danh sách voucher của khách hàng và phân loại ngay voucher nào đủ điều kiện áp dụng cho số tiền giỏ hàng hiện tại (hiển thị trong Bottom Sheet Drawer `08.01`).
- **Frontend Gọi tại:** `MyOffersDrawer.tsx`

#### 1. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `subtotal` | `BigDecimal` | Có | Tiền hàng tạm tính của giỏ hàng (vd: `267.00`) |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Eligible vouchers evaluated successfully",
  "data": {
    "currentSubtotal": 267.00,
    "eligibleVouchers": [
      {
        "userVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "code": "SAVE100",
        "title": "Voucher 100 NOK",
        "discountAmount": 100.00,
        "isEligible": true,
        "ineligibilityReason": null
      }
    ],
    "ineligibleVouchers": [
      {
        "userVoucherId": "uv9ebc99-9c0b-4ef8-bb6d-6bb9bd380009",
        "code": "VIP500",
        "title": "Voucher 500 NOK",
        "discountAmount": 0.00,
        "isEligible": false,
        "ineligibilityReason": "Đơn hàng tối thiểu phải từ 600.00 Kr (Hiện tại: 267.00 Kr)"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
