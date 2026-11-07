# Phân hệ 05: Ví Voucher & Đổi Thưởng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Ví My Rewards, Danh mục Đổi Thưởng Điểm, Đổi Cốc Tumbler Hiện vật, Áp mã Voucher & Quà trong Checkout**  
> **Ánh xạ Prototype:** `docs/prototype/7-uu-dai-va-doi-thuong/` (`07.01` đến `07.08`), `docs/prototype/8-ap-ma-va-qua-trong-checkout/` (`08.01` đến `08.12`)  
> **Ánh xạ Frontend:** `frontend/src/pages/RewardsPage.tsx`, `MyRewardsView.tsx`, `GetMoreRewardsView.tsx`, `MyOffersDrawer.tsx`  
> **Bảng Database:** `vouchers`, `user_vouchers`, `loyalty_gifts`, `user_gift_redemptions`, `points_ledger`

---

### Danh sách Endpoint trong Phân hệ 05

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 5.1 | `/api/v1/rewards/my-wallet` | `GET` | Xem ví ưu đãi cá nhân (Voucher 100 NOK, -30% & Quà tặng Cốc giữ nhiệt) | `07.03`, `07.08` |
| 5.2 | `/api/v1/rewards/catalog` | `GET` | Danh mục phần thưởng đổi bằng điểm (100 pts, 1000 pts) | `07.04` |
| 5.3 | `/api/v1/rewards/vouchers/{voucherId}/redeem` | `POST` | Đổi điểm lấy mã giảm giá Voucher (Ghi sổ cái trừ điểm) | `07.04` |
| 5.4 | `/api/v1/rewards/gifts/{giftId}/redeem` | `POST` | Đổi điểm nhận quà hiện vật (Cốc Tumbler giữ nhiệt) | `07.04` |
| 5.5 | `/api/v1/rewards/summary` | `GET` | Lấy số lượng voucher và quà tặng đang có để render huy hiệu đỏ tab | `07.02` |
| 5.6 | `/api/v1/cart/available-offers` | `GET` | Lấy danh sách ưu đãi đủ điều kiện áp dụng cho đơn hàng hiện tại | `08.02`, `08.04` |
| 5.7 | `/api/v1/cart/promo/validate` | `POST` | Kiểm tra mã khuyến mãi nhập tay qua bàn phím ảo | `08.05`, `08.11`, `08.12` |
| 5.8 | `/api/v1/cart/voucher/apply` | `POST` | Áp dụng mã voucher vào giỏ hàng (`In Use`) | `08.06`, `08.07` |
| 5.9 | `/api/v1/cart/voucher/remove` | `DELETE` | Gỡ bỏ mã voucher khỏi giỏ hàng (`Remove from the Cart`) | `08.06`, `08.08` |
| 5.10 | `/api/v1/cart/gift/apply` | `POST` | Thêm quà tặng tri ân vật phẩm vào giỏ hàng (`Selected`) | `08.09`, `08.10` |
| 5.11 | `/api/v1/cart/gift/remove` | `DELETE` | Gỡ bỏ quà tặng tri ân khỏi giỏ hàng | `08.09` |

---

### API 5.1: Ví Ưu đãi Của Tôi (Get My Rewards Wallet)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/my-wallet`
- **Operation ID:** `getMyRewardsWallet`
- **Mô tả:** Lấy toàn bộ voucher giảm giá và quà tặng vật phẩm đang có hiệu lực trong ví cá nhân của hội viên. Hỗ trợ phân trang và lọc theo trạng thái hoặc loại quà tặng (`VOUCHER`, `GIFT`, `ALL`).
- **Màn hình Prototype:** `07.03`, `07.08`
- **Frontend Gọi tại:** `MyRewardsView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---|:---|
| `status` | `String` | Không | `AVAILABLE` | Trạng thái: `AVAILABLE`, `IN_USE`, `USED`, `EXPIRED` |
| `type` | `String` | Không | `ALL` | Loại phần thưởng: `ALL`, `VOUCHER`, `GIFT` |

#### 3. Phản hồi Thành công — Có Ưu đãi (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "My rewards retrieved successfully",
  "data": {
    "totalCount": 2,
    "vouchers": [
      {
        "userVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "voucherId": "v1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "code": "Giam100NOK",
        "title": "Voucher Name",
        "description": "Giảm 100 NOK cho đơn hàng từ 250 NOK tại Linn Sushi Sentrum",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 100.00,
        "discountValueText": "100 NOK",
        "minOrderValue": 250.00,
        "validFrom": "2026-11-01T00:00:00Z",
        "validUntil": "2026-12-03T23:59:59Z",
        "status": "AVAILABLE"
      },
      {
        "userVoucherId": "uv2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "voucherId": "v2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "code": "DISCOUNT30",
        "title": "DISCOUNT30%",
        "description": "Giảm 30% tối đa 150 NOK cho đơn hàng dùng tại bàn",
        "discountType": "PERCENTAGE",
        "discountValue": 30.00,
        "discountValueText": "-30 %",
        "minOrderValue": 0.00,
        "validFrom": "2026-11-01T00:00:00Z",
        "validUntil": "2026-11-30T23:59:59Z",
        "status": "IN_USE"
      }
    ],
    "gifts": [
      {
        "userGiftId": "ug1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Cốc giữ nhiệt",
        "description": "Cốc giữ nhiệt Ordera inox 304 500ml",
        "imageUrl": "https://storage.ordera.no/gifts/tumbler-pink.png",
        "redemptionCode": "GIFT-8942-PINK",
        "status": "AVAILABLE",
        "validUntil": "2026-12-31T23:59:59Z"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Thành công — Trạng thái Rỗng (`07.05`, `07.06`)
```json
{
  "success": true,
  "message": "Không có ưu đãi nào trong ví",
  "data": {
    "totalCount": 0,
    "vouchers": [],
    "gifts": []
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 5. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để xem ví ưu đãi của bạn.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.2: Danh mục Đổi Thưởng Điểm (Get Rewards Catalog)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/catalog`
- **Operation ID:** `getRewardsCatalog`
- **Mô tả:** Lấy toàn bộ danh sách ưu đãi có thể đổi bằng điểm trong tab *"Get More Rewards"* (`07.04`), bao gồm mốc điểm yêu cầu (100 pts, 1000 pts) và đánh giá thành viên có đủ điểm đổi hay không.
- **Màn hình Prototype:** `07.04`
- **Frontend Gọi tại:** `GetMoreRewardsView.tsx`

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
        "imageUrl": "https://storage.ordera.no/gifts/tumbler-pink.png",
        "pointsCost": 1000,
        "stockQuantity": 48,
        "canAfford": false,
        "pointsNeeded": 880
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để xem danh mục đổi thưởng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.3: Đổi Điểm Lấy Voucher Giảm Giá (Redeem Voucher Reward)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/rewards/vouchers/{voucherId}/redeem`
- **Operation ID:** `redeemVoucherReward`
- **Mô tả:** Thành viên dùng điểm tích lũy để đổi lấy mã voucher giảm giá (Figma Screen `07.04`). Hệ thống kiểm tra số dư điểm, ghi sổ cái trừ điểm (`points_ledger`) và thêm bản ghi vào `user_vouchers`.
- **Màn hình Prototype:** `07.04`
- **Frontend Gọi tại:** `GetMoreRewardsView.tsx`, `RewardDetailModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `voucherId` | `UUID` | **Có** | ID mẫu voucher trong catalog muốn đổi |

#### 3. Phản hồi Thành công (201 Created)
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

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để đổi voucher ưu đãi.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy voucher (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "VOUCHER_NOT_FOUND",
    "message": "Voucher không tồn tại hoặc đã bị gỡ khỏi danh mục đổi thưởng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

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
- **Màn hình Prototype:** `07.04`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `giftId` | `UUID` | **Có** | ID quà tặng trong bảng `loyalty_gifts` |

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Đổi quà thành công! Hãy xuất trình mã này cho nhân viên quầy thu ngân để nhận cốc.",
  "data": {
    "redemptionId": "gr1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "giftTitle": "Cốc Giữ Nhiệt Tumbler Ordera Pink",
    "pointsSpent": 1000,
    "remainingPoints": 20,
    "redemptionCode": "GIFT-8942-PINK",
    "status": "PENDING",
    "instruction": "Xuất trình mã này tại quầy thu ngân bất kỳ chi nhánh Linn Sushi để nhận quà.",
    "createdAt": "2026-11-05T20:45:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để đổi quà tặng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy quà tặng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "GIFT_NOT_FOUND",
    "message": "Món quà này không tồn tại hoặc đã ngừng chương trình tặng quà.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Điểm khả dụng không đủ (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "POINTS_INSUFFICIENT",
    "message": "Số dư điểm khả dụng không đủ để đổi quà tặng này.",
    "details": {
      "currentPoints": 120,
      "requiredPoints": 1000,
      "pointsShortage": 880
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

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

### API 5.5: Số Lượng Ưu Đãi Đang Có (Get Rewards Summary Counters)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/summary`
- **Operation ID:** `getRewardsSummary`
- **Mô tả:** Lấy số lượng voucher và quà tặng đang có hiệu lực trong ví cá nhân để render huy hiệu đỏ trên tab *"My Rewards"* (`07.02`).
- **Màn hình Prototype:** `07.02`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Rewards summary retrieved",
  "data": {
    "totalAvailableRewards": 3,
    "activeVouchersCount": 2,
    "availableGiftsCount": 1
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để xem thông tin tổng quan ưu đãi.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.6: Ưu Đãi Đủ Điều Kiện Cho Giỏ Hàng (Get Available Offers for Cart)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/cart/available-offers`
- **Operation ID:** `getAvailableOffersForCart`
- **Mô tả:** Được gọi khi khách bấm vào ô *"My Offers"* trong màn hình Checkout (`08.02`, `08.04`). API đánh giá giá trị tạm tính của giỏ hàng và trả về danh sách voucher/quà tặng kèm trạng thái khả dụng (`isApplicable`).
- **Màn hình Prototype:** `08.02`, `08.04`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `cartId` | `UUID` | Có | ID giỏ hàng hiện tại đang xem xét áp dụng ưu đãi |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Available offers evaluated successfully",
  "data": {
    "subtotalAmount": 250.00,
    "vouchers": [
      {
        "code": "Giam100NOK",
        "title": "Voucher Name",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 100.00,
        "discountValueText": "100 NOK",
        "minOrderValue": 250.00,
        "isApplicable": true,
        "ineligibilityReason": null,
        "status": "AVAILABLE"
      },
      {
        "code": "DISCOUNT30",
        "title": "DISCOUNT30%",
        "discountType": "PERCENTAGE",
        "discountValue": 30.00,
        "discountValueText": "-30 %",
        "minOrderValue": 0.00,
        "isApplicable": true,
        "ineligibilityReason": null,
        "status": "IN_USE"
      },
      {
        "code": "VIP500",
        "title": "Voucher 500 NOK",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 500.00,
        "discountValueText": "500 NOK",
        "minOrderValue": 600.00,
        "isApplicable": false,
        "ineligibilityReason": "Đơn hàng tối thiểu phải từ 600.00 NOK (Hiện tại: 250.00 NOK)",
        "status": "AVAILABLE"
      }
    ],
    "gifts": [
      {
        "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Cốc giữ nhiệt",
        "imageUrl": "https://storage.ordera.no/gifts/tumbler-pink.png",
        "isApplicable": true,
        "status": "AVAILABLE"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để xem danh sách ưu đãi áp dụng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy giỏ hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "CART_NOT_FOUND",
    "message": "Giỏ hàng không tồn tại hoặc đã hết hạn phiên làm việc.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.7: Kiểm tra Mã Khuyến Mãi Nhập Tay (Validate Manual Promo Code)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/cart/promo/validate`
- **Operation ID:** `validateManualPromoCode`
- **Mô tả:** Kiểm tra tính hợp lệ của mã khuyến mãi khách hàng gõ từ bàn phím ảo (`08.05`, `08.11`, `08.12`). Nếu hợp lệ, trả về số tiền được giảm tương ứng với giỏ hàng.
- **Màn hình Prototype:** `08.05`, `08.11`, `08.12`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `code` | `String` | Có | `@NotBlank`, `@Size(max=32)` | Mã khuyến mãi người dùng nhập tay |
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng hiện tại |

```json
{
  "code": "DISCOUNT30",
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99"
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Mã khuyến mãi hợp lệ",
  "data": {
    "code": "DISCOUNT30",
    "discountType": "PERCENTAGE",
    "discountValue": 30.00,
    "discountAmount": 75.00,
    "minOrderValue": 0.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để kiểm tra mã khuyến mãi.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Mã không tồn tại hoặc đã hết hạn (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "PROMO_CODE_NOT_FOUND",
    "message": "Mã khuyến mãi không hợp lệ hoặc đã hết hạn sử dụng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Đơn hàng chưa đạt điều kiện áp dụng mã (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "PROMO_CODE_INELIGIBLE",
    "message": "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã khuyến mãi này.",
    "details": {
      "currentSubtotal": 120.00,
      "minOrderRequired": 200.00
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.8: Áp Dụng Mã Voucher Vào Giỏ Hàng (Apply Voucher to Cart)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/cart/voucher/apply`
- **Operation ID:** `applyVoucherToCart`
- **Mô tả:** Khách hàng chọn voucher từ Drawer và bấm áp dụng (`08.06`, `08.07`). Hệ thống chuyển trạng thái voucher sang `IN_USE`, gắn vào giỏ hàng và tính toán lại bảng tổng chi phí.
- **Màn hình Prototype:** `08.06`, `08.07`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng |
| `voucherCode` | `String` | Có | `@NotBlank` | Mã voucher áp dụng (vd: `DISCOUNT30`) |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "voucherCode": "DISCOUNT30"
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Áp dụng voucher thành công",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "appliedVoucher": {
      "code": "DISCOUNT30",
      "title": "DISCOUNT30%",
      "discountAmount": 75.00
    },
    "subtotalAmount": 250.00,
    "discountAmount": 75.00,
    "vatAmount": 22.83,
    "finalTotalAmount": 175.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để áp dụng voucher.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy voucher trong ví (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "VOUCHER_NOT_FOUND",
    "message": "Voucher không tồn tại trong ví hoặc không thuộc về người dùng này.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Đơn hàng chưa đạt giá trị tối thiểu (`422 Unprocessable Entity`):**
```json
{
  "success": false,
  "error": {
    "code": "MIN_ORDER_NOT_MET",
    "message": "Đơn hàng phải có giá trị tối thiểu từ 250.00 NOK để áp dụng mã giảm giá này.",
    "details": {
      "currentSubtotal": 180.00,
      "minOrderRequired": 250.00
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.9: Gỡ Bỏ Mã Voucher Khỏi Giỏ Hàng (Remove Voucher from Cart)

- **HTTP Method:** `DELETE`
- **URL Path:** `/api/v1/cart/voucher/remove`
- **Operation ID:** `removeVoucherFromCart`
- **Mô tả:** Khách hàng nhấn nút `[Remove from the Cart]` tại màn hình `08.06` hoặc `08.08`. Hệ thống giải phóng mã voucher trở lại trạng thái `AVAILABLE`.
- **Màn hình Prototype:** `08.06`, `08.08`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng |
| `voucherCode` | `String` | Có | `@NotBlank` | Mã voucher muốn gỡ bỏ |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "voucherCode": "DISCOUNT30"
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Đã gỡ bỏ voucher khỏi giỏ hàng",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "subtotalAmount": 250.00,
    "discountAmount": 0.00,
    "vatAmount": 32.61,
    "finalTotalAmount": 250.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để gỡ bỏ voucher khỏi giỏ hàng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy giỏ hàng hoặc voucher trong giỏ (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "CART_OR_VOUCHER_NOT_FOUND",
    "message": "Giỏ hàng không tồn tại hoặc mã voucher chưa từng được áp dụng vào giỏ hàng này.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.10: Áp Dụng Quà Tặng Hội Viên Vào Giỏ Hàng (Apply Gift to Cart)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/cart/gift/apply`
- **Operation ID:** `applyGiftToCart`
- **Mô tả:** Khách hàng nhấn chọn quà tặng (Cốc giữ nhiệt) trong drawer để nhận quà cùng với đơn hàng (`08.09`, `08.10`). Nút chuyển sang trạng thái `Selected`.
- **Màn hình Prototype:** `08.09`, `08.10`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng |
| `rewardGiftId` | `UUID` | Có | UUID quà tặng | ID của món quà tặng trong ví |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01"
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Quà tặng tri ân đã được thêm vào đơn hàng của bạn",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "selectedGift": {
      "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
      "title": "Cốc giữ nhiệt",
      "status": "SELECTED"
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để chọn quà tặng tri ân.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy quà tặng hợp lệ trong ví (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "GIFT_NOT_FOUND",
    "message": "Quà tặng không tồn tại trong ví đổi thưởng của bạn hoặc đã được sử dụng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.11: Gỡ Bỏ Quà Tặng Khỏi Giỏ Hàng (Remove Gift from Cart)

- **HTTP Method:** `DELETE`
- **URL Path:** `/api/v1/cart/gift/remove`
- **Operation ID:** `removeGiftFromCart`
- **Mô tả:** Khách hàng bỏ chọn quà tặng tri ân khỏi giỏ hàng trước khi đặt hàng (`08.09`).
- **Màn hình Prototype:** `08.09`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |
| `Content-Type` | `String` | **Có** | `application/json` |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `cartId` | `UUID` | Có | UUID hợp lệ | ID giỏ hàng |
| `rewardGiftId` | `UUID` | Có | UUID quà tặng | ID món quà muốn gỡ |

```json
{
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
  "rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01"
}
```

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Đã gỡ quà tặng khỏi đơn hàng",
  "data": {
    "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99",
    "selectedGift": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Header 'X-Tenant-ID' is required for multi-tenant data isolation.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để gỡ quà tặng.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

- **Không tìm thấy quà tặng trong đơn hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "GIFT_NOT_FOUND",
    "message": "Quà tặng không tồn tại trong giỏ hàng hiện tại.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

## Ghi Chú Kỹ Thuật & Chu Trình Trạng Thái Ưu Đãi (Technical & State Machine Notes)

1. **Vòng đời Trạng thái Voucher (`user_vouchers.status`):**
   - `AVAILABLE`: Khách hàng sở hữu voucher khả dụng trong ví.
   - `IN_USE`: Khi khách hàng chọn áp dụng mã vào giỏ hàng (`POST /api/v1/cart/voucher/apply`), voucher tạm thời bị khóa cho giỏ hàng đó để tránh áp dụng đồng thời trên nhiều thiết bị.
   - `USED`: Khi đơn hàng thanh toán thành công (`COMPLETED`), voucher chính thức được đánh dấu đã sử dụng kèm `used_at` và `order_id`.
   - Nếu giỏ hàng bị hủy, hết hạn hoặc khách ấn `[Remove from the Cart]`, voucher được hoàn trả về trạng thái `AVAILABLE`.

2. **Quy Tắc Kết Hợp Ưu Đãi Giỏ Hàng (Cart Offers Combination Rules):**
   - Trong mỗi giỏ hàng/đơn hàng, hệ thống cho phép kết hợp tối đa **01 Mã giảm giá (Voucher Fixed/Percentage)** VÀ **01 Quà tặng tri ân (Physical Loyalty Gift)** cùng lúc (theo đúng thiết kế giao diện Figma Drawer Prototype 08).
   - Không được áp dụng đồng thời 2 Voucher trên cùng một đơn hàng.

3. **Cơ Chế Khóa Phân Tán Đổi Quà (Redisson Distributed Lock):**
   - Khi thực hiện đổi quà vật phẩm (`POST /api/v1/rewards/gifts/{giftId}/redeem`), hệ thống kích hoạt Redisson multi-lock trên `lock:member:{memberId}` và `lock:gift:{giftId}` nhằm ngăn ngừa hiện tượng race condition trừ điểm vượt mức hoặc vượt quá số lượng quà tặng tồn kho (`stock_quantity`).
