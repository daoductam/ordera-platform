# Phân hệ 01: Tenant & Điểm bán API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Quản trị Thương hiệu, Chi nhánh Cửa hàng & Tùy chọn Nhận hàng (Tenancy, Outlets & Fulfillment)**  
> **Ánh xạ Prototype:** `docs/prototype/1-kham-pha-va-chon-cua-hang/` (`01.01` đến `01.09`)  
> **Ánh xạ Frontend:** `frontend/src/pages/StoreDiscoveryPage.tsx`, `frontend/src/components/store-discovery/*`  
> **Bảng Database:** `tenants`, `stores`, `store_tables`, `customer_favorite_stores` (Migration `V1` & `V10`)

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

### API 1.1: Cấu hình Thương hiệu Hiện tại (Get Current Tenant)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/tenants/current`
- **Operation ID:** `getCurrentTenant`
- **Mô tả:** Lấy thông tin cấu hình thương hiệu (tên đại lý, logo, slogan, mã màu chủ đạo, hotline, chính sách điểm) phục vụ việc render giao diện động (Dynamic Theming) ở Frontend PWA.
- **Quyền hạn truy cập:** `PUBLIC` (Khách vãng lai, Thành viên, Quầy Kiosk)
- **Frontend Gọi tại:** `Navbar.tsx`, `MobileContainer.tsx`, `DrawerMenu.tsx`
- **Màn hình Prototype:** `01.01`, `01.02`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID của thương hiệu (Linn Sushi) |
| `Accept` | `String` | Không | `application/json` | Định dạng dữ liệu |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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
    "currency": "NOK",
    "defaultVatRate": 0.1500,
    "supportPhone": "+4722334455",
    "supportEmail": "contact@linnsushi.no"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi (RFC 7807)
- **Trường hợp thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Required request header 'X-Tenant-ID' is missing.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Trường hợp không tìm thấy Tenant (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_NOT_FOUND",
    "message": "No active tenant found with ID 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.2: Danh sách Cửa hàng & Tìm kiếm (List Stores)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores`
- **Operation ID:** `listStores`
- **Mô tả:** Lấy danh sách các điểm bán / chi nhánh của thương hiệu theo thành phố hoặc vị trí tọa độ GPS, phục vụ màn hình Khám phá cửa hàng (`01.02`, `01.04`, `01.08`). Hỗ trợ bộ lọc hình thức nhận món (`DINE_IN`, `TAKEAWAY`, `DELIVERY`). Danh sách trả về không phân trang (tối đa 50 điểm bán theo chi nhánh của thương hiệu), ưu tiên sắp xếp theo khoảng cách gần nhất nếu có tọa độ GPS.
- **Quyền hạn truy cập:** `PUBLIC` (Hỗ trợ cả Khách vãng lai và Hội viên đã đăng nhập)
- **Frontend Gọi tại:** `StoreDiscoveryPage.tsx`, `RestaurantList.tsx`
- **Màn hình Prototype:** `01.02`, `01.04`, `01.08`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Tùy chọn** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | Nếu có token, hệ thống đối chiếu trạng thái `isFavorite` của hội viên; nếu là khách vãng lai, mặc định `isFavorite = false` |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---:|:---|
| `keyword` | `String` | Không | `null` | Từ khóa tìm theo tên chi nhánh / đường phố |
| `fulfillmentType` | `String` | Không | `ALL` | Bộ lọc hình thức phục vụ: `ALL`, `DINE_IN`, `TAKEAWAY`, `DELIVERY` |
| `latitude` | `Double` | Không | `null` | Tọa độ vĩ độ người dùng (để tính khoảng cách km) |
| `longitude`| `Double` | Không | `null` | Tọa độ kinh độ người dùng |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Stores retrieved successfully",
  "data": [
    {
      "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
      "name": "Linn Sushi Sentrum",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "city": "Oslo",
      "latitude": 59.9114910,
      "longitude": 10.7482320,
      "coverImageUrl": "https://storage.ordera.no/stores/sentrum-cover.jpg",
      "logoUrl": "https://storage.ordera.no/stores/sentrum-logo.png",
      "phoneNumber": "+4722334455",
      "openingHoursText": "Open 9:00 - 23:00",
      "isOpen": true,
      "distanceKm": 0.35,
      "preparingTimeMinutes": 15,
      "supportedFulfillmentTypes": ["TAKEAWAY", "DINE_IN", "DELIVERY"],
      "isFavorite": true
    },
    {
      "id": "b2eebc99-9c0b-4ef8-bb6d-6bb9bd380b02",
      "name": "Linn Sushi Majorstuen",
      "address": "Bogstadveien 45, 0366 Oslo",
      "city": "Oslo",
      "latitude": 59.9272300,
      "longitude": 10.7162900,
      "coverImageUrl": "https://storage.ordera.no/stores/majorstuen-cover.jpg",
      "logoUrl": "https://storage.ordera.no/stores/majorstuen-logo.png",
      "phoneNumber": "+4722334456",
      "openingHoursText": "Open 11:00 - 22:00",
      "isOpen": true,
      "distanceKm": 2.10,
      "preparingTimeMinutes": 20,
      "supportedFulfillmentTypes": ["TAKEAWAY", "DELIVERY"],
      "isFavorite": false
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Required request header 'X-Tenant-ID' is missing.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Tham số lọc hình thức nhận không hợp lệ (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_QUERY_PARAM",
    "message": "Giá trị 'fulfillmentType' không hợp lệ. Chỉ chấp nhận ALL, DINE_IN, TAKEAWAY, DELIVERY.",
    "details": {
      "rejectedValue": "EXPRESS_PICKUP"
    }
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.3: Chi tiết Cửa hàng & Trạng thái Thực đơn (Get Store Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}`
- **Operation ID:** `getStoreById`
- **Mô tả:** Lấy thông tin chi tiết một điểm bán gồm giờ hoạt động, địa chỉ, số hotline, thời gian chuẩn bị món dự kiến và các hình thức nhận phục vụ popup xác nhận *"Just making sure..."* tại màn hình `01.05`.
- **Màn hình Prototype:** `01.05`
- **Frontend Gọi tại:** `StoreDiscoveryPage.tsx`, `LocationConfirmModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu (chống IDOR chéo tenant) |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID duy nhất của cửa hàng |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Required request header 'X-Tenant-ID' is missing.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Không tìm thấy Cửa hàng hoặc thuộc thương hiệu khác (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Store with ID 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b99' does not exist in this tenant.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.4: Quét Mã QR Bàn Ăn (Resolve Store Table by QR)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/tables/qr/{qrToken}`
- **Operation ID:** `resolveTableByQrToken`
- **Mô tả:** Khi khách hàng ngồi tại bàn và dùng camera quét mã QR dán trên bàn, API này giải mã token thành số bàn (`Table 05`) và chi nhánh cửa hàng tương ứng để tự động gán vào giỏ hàng (`01.01`, `06.07`).
- **Màn hình Prototype:** `01.01`, `06.07`
- **Frontend Gọi tại:** `StoreDiscoveryPage.tsx`, `CheckoutView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu (đảm bảo QR token không thể đọc trộm bàn của thương hiệu khác) |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `qrToken` | `String` | **Có** | `qr_linn_sentrum_table_05` |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Table resolved successfully",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "storeName": "Linn Sushi Sentrum",
    "tableId": "tb1ebc99-9c0b-4ef8-bb6d-6bb9bd380005",
    "tableNumber": "Table 05",
    "zone": "Tầng 1 - Khu trong nhà",
    "capacity": 4,
    "isOccupied": false
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Required request header 'X-Tenant-ID' is missing.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Mã QR không hợp lệ hoặc bị rách/hết hạn (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_QR_TOKEN",
    "message": "Mã QR bàn ăn không hợp lệ hoặc không thuộc chi nhánh này.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.5: Tùy chọn Nhận hàng & Khung giờ (Get Fulfillment Options)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/fulfillment-options`
- **Operation ID:** `getFulfillmentOptions`
- **Mô tả:** Lấy danh sách các hình thức nhận món được cấu hình cho tenant (Dine In, Takeaway, Delivery) và các tùy chọn thời gian (As soon as possible / Schedule) phục vụ hiển thị Bottom Sheet `01.01` và `01.03`. Nếu có truyền tham số `storeId`, hệ thống sẽ trả về chính xác năng lực phục vụ của chi nhánh đó; nếu không truyền `storeId`, trả về cấu hình mặc định toàn thương hiệu.
- **Màn hình Prototype:** `01.01`, `01.03`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Ví dụ | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---:|:---|
| `storeId` | `UUID` | Không | `null` | ID chi nhánh cửa hàng cụ thể (nếu đã chọn quán trước) |

#### 3. Phản hồi Thành công (200 OK)
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

#### 4. Phản hồi Lỗi (RFC 7807)
- **Thiếu Header `X-Tenant-ID` (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "TENANT_HEADER_MISSING",
    "message": "Required request header 'X-Tenant-ID' is missing.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Không tìm thấy Cửa hàng với `storeId` đã cung cấp (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Chi nhánh chỉ định không tồn tại hoặc đã ngừng hoạt động.",
    "details": null
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
- **Màn hình Prototype:** `01.02`, `01.04`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token của hội viên |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID chi nhánh muốn lưu hoặc bỏ danh sách yêu thích |

#### 3. Request Body
*Không có Body.*

#### 4. Phản hồi Thành công — Thêm Yêu Thích (POST 200 OK)
```json
{
  "success": true,
  "message": "Store added to favorites successfully",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "isFavorite": true
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 5. Phản hồi Thành công — Gỡ Yêu Thích (DELETE 200 OK)
```json
{
  "success": true,
  "message": "Store removed from favorites successfully",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "isFavorite": false
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 6. Phản hồi Lỗi (RFC 7807)
- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để lưu chi nhánh vào danh sách yêu thích.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
- **Không tìm thấy Cửa hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Chi nhánh cửa hàng không tồn tại trong hệ thống.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### Ghi Chú Kỹ Thuật & Yêu Cầu Cơ Sở Dữ Liệu (Database Alignment Notes)

Để hỗ trợ đầy đủ các trường dữ liệu hiển thị trên Prototype và hợp đồng API trên, pha triển khai Database cần bổ sung Migration (`V10__add_store_fields_and_favorites.sql`) với các trường sau:

```sql
-- 1. Bổ sung các trường phục vụ hiển thị thẻ cửa hàng và dịch vụ giao hàng
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cover_image_url TEXT;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS logo_url TEXT;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS accepts_delivery BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS delivery_radius_km NUMERIC(5,2) DEFAULT 5.0;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS preparing_time_minutes INT DEFAULT 15;

-- 2. Bổ sung trường khu vực bàn ăn
ALTER TABLE store_tables ADD COLUMN IF NOT EXISTS zone VARCHAR(100);

-- 3. Tạo bảng lưu trữ danh sách cửa hàng yêu thích của khách hàng
CREATE TABLE IF NOT EXISTS customer_favorite_stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customer_store_favorite UNIQUE (tenant_id, user_id, store_id)
);

CREATE INDEX IF NOT EXISTS idx_fav_stores_user ON customer_favorite_stores(tenant_id, user_id);
```
