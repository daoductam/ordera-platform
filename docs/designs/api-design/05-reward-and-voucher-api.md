# Phân hệ 05: Ví Voucher & Đổi Thưởng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Ví My Rewards, Danh mục Đổi Thưởng Điểm, Đổi Cốc Tumbler Hiện vật, Áp mã & Quà trong Checkout**  
> **Ánh xạ Prototype:** `docs/prototype/7-uu-dai-va-doi-thuong/` (`07.01` đến `07.08`), `docs/prototype/8-ap-ma-va-qua-trong-checkout/` (`08.01` đến `08.12`)  
> **Ánh xạ Frontend:** `frontend/src/pages/RewardsPage.tsx`, `MyRewardsView.tsx`, `GetMoreRewardsView.tsx`, `MyOffersDrawer.tsx`  
> **Bảng Database:** `vouchers`, `user_vouchers`, `loyalty_gifts`, `user_gift_redemptions`, `points_ledger`

---

### Danh sách Endpoint trong Phân hệ 05

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 5.1 | `/api/v1/rewards/my-wallet` | `GET` | Xem ví ưu đãi cá nhân (Voucher 100 NOK, -30% & Quà tặng Cốc giữ nhiệt) | `07.03`, `07.08` |
| 5.2 | `/api/v1/rewards/catalog` | `GET` | Danh mục phần thưởng đổi bằng điểm (100 pts, 1000 pts) | `07.04` |
| 5.3 | `/api/v1/rewards/{rewardId}/redeem` | `POST` | Đổi điểm lấy Voucher hoặc Quà hiện vật (Ghi sổ cái trừ điểm) | `07.04` |
| 5.4 | `/api/v1/rewards/summary` | `GET` | Lấy số lượng voucher và quà tặng đang có để render huy hiệu đỏ | `07.02` |
| 5.5 | `/api/v1/cart/available-offers` | `GET` | Lấy danh sách ưu đãi đủ điều kiện áp dụng cho đơn hàng hiện tại | `08.02`, `08.04` |
| 5.6 | `/api/v1/cart/promo/validate` | `POST` | Kiểm tra mã khuyến mãi nhập tay qua bàn phím ảo | `08.05`, `08.11`, `08.12` |
| 5.7 | `/api/v1/cart/voucher/apply` | `POST` | Áp dụng mã voucher vào giỏ hàng (`In Use`) | `08.06`, `08.07` |
| 5.8 | `/api/v1/cart/voucher/remove` | `DELETE` | Gỡ bỏ mã voucher khỏi giỏ hàng (`Remove from the Cart`) | `08.06`, `08.08` |
| 5.9 | `/api/v1/cart/gift/apply` | `POST` | Thêm quà tặng tri ân vật phẩm vào giỏ hàng (`Selected`) | `08.09`, `08.10` |
| 5.10 | `/api/v1/cart/gift/remove` | `DELETE` | Gỡ bỏ quà tặng tri ân khỏi giỏ hàng | `08.09` |

---

### API 5.1: Ví Ưu đãi Của Tôi (Get My Rewards Wallet)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/my-wallet`
- **Operation ID:** `getMyRewardsWallet`
- **Mô tả:** Lấy toàn bộ voucher giảm giá và quà tặng vật phẩm đang có hiệu lực trong ví cá nhân của hội viên. Hỗ trợ lọc theo loại qua query param `?type=VOUCHER|GIFT|ALL`.
- **Màn hình Prototype:** `07.03`, `07.08`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Rewards wallet retrieved successfully",
  "data": {
    "vouchers": [
      {
        "userVoucherId": "uv1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "code": "Giam100NOK",
        "title": "Voucher Name",
        "discountType": "FIXED_AMOUNT",
        "discountValue": 100.00,
        "description": "Giảm 100 NOK giá trị cho Order của bạn",
        "minOrderValue": 250.00,
        "expiredDate": "2025-12-03T23:59:59Z",
        "status": "AVAILABLE",
        "actionLabel": "Use now"
      },
      {
        "userVoucherId": "uv2ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "code": "DISCOUNT30",
        "title": "DISCOUNT30%",
        "discountType": "PERCENTAGE",
        "discountValue": 30.00,
        "description": "Giảm 30% giá trị cho đơn hàng của bạn",
        "minOrderValue": 0.00,
        "expiredDate": "2025-12-03T23:59:59Z",
        "status": "AVAILABLE",
        "actionLabel": "Use now"
      }
    ],
    "gifts": [
      {
        "redemptionId": "gr1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Cốc giữ nhiệt",
        "description": "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
        "imageUrl": "https://storage.ordera.no/gifts/tumbler-pink.png",
        "quantityLeft": 10,
        "actionLabel": "Get now"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.2: Danh mục Đổi Thưởng Bằng Điểm (Get More Rewards Catalog)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/catalog`
- **Operation ID:** `getRewardsCatalog`
- **Mô tả:** Lấy danh mục các voucher và quà tặng cho phép dùng điểm tích lũy để đổi thưởng (`07.04`).

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Rewards catalog retrieved successfully",
  "data": {
    "memberCurrentPoints": 120,
    "vouchers": [
      {
        "id": "v1eebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "title": "Voucher Name",
        "discountText": "100 NOK",
        "description": "Giảm 100 NOK giá trị cho Order của bạn",
        "minOrderValue": 250.00,
        "expiredDate": "2025-12-03T23:59:59Z",
        "pointsCost": 100,
        "canRedeem": true
      },
      {
        "id": "v2eebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "title": "Voucher Name",
        "discountText": "-30 %",
        "description": "Giảm 30% giá trị cho đơn hàng của bạn",
        "minOrderValue": 0.00,
        "expiredDate": "2025-12-03T23:59:59Z",
        "pointsCost": 1000,
        "canRedeem": false
      }
    ],
    "gifts": [
      {
        "id": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Gift Name",
        "description": "Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào",
        "imageUrl": "https://storage.ordera.no/gifts/tumbler-pink.png",
        "quantityLeft": 10,
        "pointsCost": 100,
        "canRedeem": true
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.3: Đổi Điểm Lấy Phần Thưởng (Redeem Reward)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/rewards/{rewardId}/redeem`
- **Operation ID:** `redeemReward`
- **Mô tả:** Trừ điểm trong `points_ledger` (TransactionType = `REWARD_REDEEM`), thêm voucher hoặc quà vào ví cá nhân của hội viên. Chiếm giữ Redis Lock `lock:member:{memberId}`.

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `rewardId` | `UUID` | **Có** | ID voucher hoặc gift trong catalog |

#### 2. Phản hồi Thành công (201 Created)
```json
{
  "success": true,
  "message": "Đổi quà thành công! Phần thưởng đã được thêm vào ví My Rewards.",
  "data": {
    "rewardType": "VOUCHER",
    "pointsSpent": 100,
    "remainingPoints": 20,
    "newWalletItemId": "uv3ebc99-9c0b-4ef8-bb6d-6bb9bd380003"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.4: Bộ đếm Số lượng Ưu đãi (Get Rewards Summary Counter)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/rewards/summary`
- **Operation ID:** `getRewardsSummary`
- **Mô tả:** Trả về số lượng voucher và quà tặng đang có hiệu lực để render huy hiệu đỏ trên trang hồ sơ cá nhân (`07.02`).

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Rewards summary retrieved",
  "data": {
    "activeVouchersCount": 2,
    "availableGiftsCount": 1
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.5: Ưu đãi Đủ Điều Kiện Cho Giỏ Hàng (Get Available Offers for Cart)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/cart/available-offers`
- **Operation ID:** `getAvailableOffersForCart`
- **Mô tả:** Được gọi khi khách bấm vào ô *"My Offers"* trong màn hình Checkout (`08.02`, `08.04`). API đánh giá giá trị tạm tính của giỏ hàng và trả về danh sách voucher/quà tặng kèm trạng thái khả dụng.

#### 1. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `cartId` | `UUID` | Có | ID giỏ hàng hiện tại |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Available offers evaluated successfully",
  "data": {
    "vouchers": [
      {
        "code": "Giam100NOK",
        "title": "Voucher Name",
        "discountValueText": "100 NOK",
        "minOrderValue": 250.00,
        "isApplicable": true,
        "status": "AVAILABLE"
      },
      {
        "code": "DISCOUNT30",
        "title": "DISCOUNT30%",
        "discountValueText": "-30 %",
        "minOrderValue": 0.00,
        "isApplicable": true,
        "status": "IN_USE"
      }
    ],
    "gifts": [
      {
        "giftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01",
        "title": "Cốc giữ nhiệt",
        "isApplicable": true,
        "status": "AVAILABLE"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.6: Kiểm tra Mã Ưu đãi Nhập tay (Validate Manual Promo Code)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/cart/promo/validate`
- **Operation ID:** `validateManualPromoCode`
- **Mô tả:** Kiểm tra tính hợp lệ của mã khuyến mãi khách hàng gõ từ bàn phím ảo (`08.05`, `08.11`, `08.12`).

#### 1. Request Body Schema
```json
{
  "code": "DISCOUNT30",
  "cartId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99"
}
```

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Mã khuyến mãi hợp lệ",
  "data": {
    "code": "DISCOUNT30",
    "discountType": "PERCENTAGE",
    "discountValue": 30.00,
    "discountAmount": 80.10,
    "minOrderValue": 0.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 5.7 & 5.8: Áp dụng & Gỡ bỏ Voucher trong Giỏ Hàng (Apply / Remove Voucher)

- **Áp dụng:** `POST /api/v1/cart/voucher/apply`  
  - **Body:** `{"voucherCode": "DISCOUNT30", "cartId": "..."}`  
  - **Màn hình Prototype:** `08.06`, `08.07`
- **Gỡ bỏ:** `DELETE /api/v1/cart/voucher/remove`  
  - **Body:** `{"voucherCode": "DISCOUNT30", "cartId": "..."}`  
  - **Màn hình Prototype:** `08.06` (Nút `[Remove from the Cart]`)

---

### API 5.9 & 5.10: Áp dụng & Gỡ bỏ Quà tặng Hội viên (Apply / Remove Gift)

- **Áp dụng:** `POST /api/v1/cart/gift/apply`  
  - **Body:** `{"rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01", "cartId": "..."}`  
  - **Màn hình Prototype:** `08.09`, `08.10`
- **Gỡ bỏ:** `DELETE /api/v1/cart/gift/remove`  
  - **Body:** `{"rewardGiftId": "g1eebc99-9c0b-4ef8-bb6d-6bb9bd380g01", "cartId": "..."}`  
  - **Màn hình Prototype:** `08.09` (Nút `[Remove from the Cart]`)
