# Phân hệ 01: Tenant & Điểm bán API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Quản trị Thương hiệu, Chi nhánh Cửa hàng & Tùy chọn Nhận hàng (Tenancy, Outlets & Fulfillment)**  
> **Ánh xạ Prototype:** `docs/prototype/1-kham-pha-va-chon-cua-hang/` (`01.01` đến `01.09`)  
> **Ánh xạ Frontend:** `frontend/src/pages/StoreDiscoveryPage.tsx`, `frontend/src/components/store-discovery/*`  
> **Bảng Database:** `tenants`, `stores`, `store_tables`, `customer_favorite_stores`

---

### Danh sách Endpoint trong Phân hệ 01

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 1.1 | `/api/v1/tenants/current` | `GET` | Lấy thông tin cấu hình thương hiệu (logo, slogan, màu sắc, hotline) | `01.01`, `01.02` |
| 1.2 | `/api/v1/stores` | `GET` | Danh sách chi nhánh kèm khoảng cách, giờ mở cửa, bộ lọc hình thức nhận | `01.02`, `01.04`, `01.08` |
| 1.3 | `/api/v1/stores/{storeId}` | `GET` | Lấy chi tiết chi nhánh phục vụ popup xác nhận ("Just making sure...") | `01.05` |
| 1.4 | `/api/v1/stores/tables/qr/{qrToken}` | `GET` | Quét mã QR giải mã số bàn ăn tại chỗ (Dine-in) | `01.01`, `06.07` |
| 1.5 | `/api/v1/stores/fulfillment-options` | `GET` | Lấy các hình thức nhận hàng hỗ trợ (Dine In, Takeaway, Delivery) & khung giờ | `01.01`, `01.03` |
| 1.6 | `/api/v1/stores/{storeId}/favorite` | `POST` / `DELETE` | Bật/tắt trạng thái yêu thích cửa hàng (Icon trái tim đỏ) | `01.02`, `01.04` |

---

### API 1.1: Lấy Thông tin Thương hiệu Hiện tại (Get Current Tenant)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/tenants/current`
- **Operation ID:** `getCurrentTenant`
- **Mô tả:** Lấy thông tin cấu hình thương hiệu (tên đại lý, logo, slogan, mã màu chủ đạo, hotline, chính sách điểm) phục vụ việc render giao diện động (Dynamic Theming) ở Frontend PWA.
- **Quyền hạn truy cập:** `PUBLIC` (Khách vãng lai, Thành viên, Quầy Kiosk)
- **Frontend Gọi tại:** `Navbar.tsx`, `MobileContainer.tsx`, `DrawerMenu.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mặc định / Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID của thương hiệu (Linn Sushi) |
| `Accept` | `String` | Không | `application/json` | Định dạng dữ liệu |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Tenant details retrieved successfully",
  "data": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "code": "linn_sushi",
    "name": "Linn Sushi",
    "slogan": "Where fresh sushi meet your taste",
    "logoUrl": "https://storage.ordera.no/tenants/linn-sushi-logo.png",
    "primaryColor": "#F5B800",
    "secondaryColor": "#1E293B",
    "status": "ACTIVE",
    "metadata": {
      "hotline": "(+47) 22 33 44 55",
      "supportEmail": "info@linnsushi.no",
      "defaultCurrency": "NOK",
      "vatIncludedRate": 15.00
    },
    "createdAt": "2026-11-05T00:00:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.2: Danh sách Cửa hàng & Tìm kiếm (List Stores)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores`
- **Operation ID:** `listStores`
- **Mô tả:** Lấy danh sách các điểm bán / chi nhánh của thương hiệu theo thành phố, tọa độ GPS hoặc hình thức phục vụ (Takeaway, Dine In, Delivery).
- **Màn hình Prototype:** `01.02`, `01.04`, `01.08`, `01.09`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | Không | `Bearer ...` (Dùng để nhận biết cửa hàng yêu thích của user) |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả & Ràng buộc |
|:---|:---|:---:|:---|:---|
| `fulfillmentType` | `String` | Không | `null` | Lọc theo: `DINE_IN`, `TAKEAWAY`, `DELIVERY` |
| `status` | `String` | Không | `ALL` | `OPEN`, `CLOSED`, `ALL` |
| `latitude` | `BigDecimal` | Không | `null` | Vĩ độ GPS người dùng để tính `distanceKm` |
| `longitude` | `BigDecimal` | Không | `null` | Kinh độ GPS người dùng |
| `city` | `String` | Không | `null` | Lọc theo thành phố (vd: `Oslo`, `Bergen`) |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Stores retrieved successfully",
  "data": [
    {
      "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
      "name": "Linn Sushi Sentrum",
      "coverImageUrl": "https://storage.ordera.no/stores/sentrum-cover.jpg",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "city": "Oslo",
      "latitude": 59.9114910,
      "longitude": 10.7482320,
      "distanceKm": 0.45,
      "phoneNumber": "+4722334455",
      "openingHoursText": "Open 9:00 - 23:00",
      "isOpen": true,
      "preparingTimeMinutes": 15,
      "supportedFulfillmentTypes": ["TAKEAWAY", "DINE_IN", "DELIVERY"],
      "isFavorite": true
    },
    {
      "id": "b2eebc99-9c0b-4ef8-bb6d-6bb9bd380b02",
      "name": "Linn Sushi Majorstuen",
      "coverImageUrl": "https://storage.ordera.no/stores/majorstuen-cover.jpg",
      "address": "Valkyriegata 15, 0366 Oslo",
      "city": "Oslo",
      "latitude": 59.9298210,
      "longitude": 10.7163820,
      "distanceKm": 2.10,
      "phoneNumber": "+4722334456",
      "openingHoursText": "Closed · Open 13:00 Fri",
      "isOpen": false,
      "preparingTimeMinutes": 15,
      "supportedFulfillmentTypes": ["TAKEAWAY", "DELIVERY"],
      "isFavorite": false
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.3: Chi tiết Chi nhánh Cửa hàng (Get Store Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}`
- **Operation ID:** `getStoreById`
- **Mô tả:** Lấy toàn bộ thông tin chi tiết của một chi nhánh phục vụ hiển thị Modal xác nhận chọn cửa hàng (`01.05 - Just making sure...`).

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID định danh chi nhánh cửa hàng |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Store details retrieved successfully",
  "data": {
    "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "name": "Linn Sushi Sentrum",
    "address": "Prinsens gate 10C, 0153 Oslo",
    "city": "Oslo",
    "coverImageUrl": "https://storage.ordera.no/stores/sentrum-cover.jpg",
    "logoUrl": "https://storage.ordera.no/stores/sentrum-logo.png",
    "phoneNumber": "+4722334455",
    "openingHoursText": "Open 9:00 - 23:00",
    "isOpen": true,
    "preparingTimeMinutes": 15,
    "supportedFulfillmentTypes": ["TAKEAWAY", "DINE_IN", "DELIVERY"],
    "deliveryRadiusKm": 5.0,
    "deliveryFee": 39.00
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.4: Quét Mã QR Nhận diện Bàn ăn (Resolve Table by QR Code)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/tables/qr/{qrToken}`
- **Operation ID:** `resolveTableByQrToken`
- **Mô tả:** Khi khách hàng ngồi tại bàn và dùng camera quét mã QR dán trên bàn, API này giải mã token thành số bàn (`Table 05`) và chi nhánh cửa hàng tương ứng để tự động gán vào giỏ hàng.
- **Màn hình Prototype:** `01.01`, `06.07`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `qrToken` | `String` | **Có** | `qr_linn_sentrum_table_05` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Table resolved successfully",
  "data": {
    "tableId": "t1eebc99-9c0b-4ef8-bb6d-6bb9bd380t05",
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "storeName": "Linn Sushi Sentrum",
    "tableNumber": "Table 05",
    "seatingCapacity": 4,
    "occupied": false
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.5: Tùy chọn Nhận hàng & Khung giờ (Get Fulfillment Options)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/fulfillment-options`
- **Operation ID:** `getFulfillmentOptions`
- **Mô tả:** Lấy danh sách các hình thức nhận món được cấu hình cho tenant (Dine In, Takeaway, Delivery) và các tùy chọn thời gian (As soon as possible / Schedule) phục vụ hiển thị Bottom Sheet `01.01` và `01.03`.

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Fulfillment options retrieved successfully",
  "data": {
    "supportedTypes": [
      {
        "type": "DINE_IN",
        "label": "Dine In",
        "icon": "utensils",
        "enabled": true
      },
      {
        "type": "TAKEAWAY",
        "label": "Takeaway",
        "icon": "shopping-bag",
        "enabled": true
      },
      {
        "type": "DELIVERY",
        "label": "Delivery",
        "icon": "bike",
        "enabled": true
      }
    ],
    "timeOptions": {
      "asap": {
        "label": "As soon as possible",
        "estimatedPreparingMinutes": 15
      },
      "schedule": {
        "label": "Schedule",
        "sublabel": "Select a time",
        "allowSameDayOnly": true,
        "availableSlots": [
          "11:30 - 12:00",
          "12:00 - 12:30",
          "12:30 - 13:00",
          "17:30 - 18:00",
          "18:00 - 18:30"
        ]
      }
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.6: Bật / Tắt Yêu Thích Cửa Hàng (Toggle Favorite Store)

- **HTTP Method:** `POST` (Thêm vào yêu thích) / `DELETE` (Bỏ yêu thích)
- **URL Path:** `/api/v1/stores/{storeId}/favorite`
- **Operation ID:** `toggleStoreFavorite`
- **Mô tả:** Bật/tắt biểu tượng trái tim đỏ trên thẻ cửa hàng (`01.02`, `01.04`). Yêu cầu người dùng đã đăng nhập.

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Store favorite status updated",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "isFavorite": true
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
