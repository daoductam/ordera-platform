# Phân hệ 01: Tenant & Điểm bán API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Quản trị Thương hiệu & Chi nhánh Cửa hàng (Tenancy & Outlets)**  
> **Ánh xạ Frontend:** `frontend/src/pages/StoreDiscoveryPage.tsx`, `frontend/src/components/store-discovery/*`  
> **Bảng Database:** `tenants`, `stores`, `store_tables`

---

### API 1.1: Lấy Thông tin Thương hiệu Hiện tại (Get Current Tenant)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/tenants/current`
- **Operation ID:** `getCurrentTenant`
- **Mô tả:** Lấy thông tin cấu hình thương hiệu (tên đại lý, logo, mã màu chủ đạo, hotline, chính sách điểm) phục vụ việc render giao diện động (Dynamic Theming) ở Frontend PWA.
- **Quyền hạn truy cập:** `PUBLIC` (Khách vãng lai, Thành viên, Quầy Kiosk)
- **Frontend Gọi tại:** `Navbar.tsx`, `MobileContainer.tsx`, `DrawerMenu.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mặc định / Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID của thương hiệu (Linn Sushi) |
| `Accept` | `String` | Không | `application/json` | Định dạng dữ liệu |

#### 2. Parameters
*Không có Parameters.*

#### 3. Request Body
*Không có Body.*

#### 4. Phản hồi Thành công (200 OK)
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
    "logoUrl": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
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

#### 5. Phản hồi Lỗi (Error Responses)
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
- **Mô tả:** Lấy danh sách các điểm bán / chi nhánh của thương hiệu theo thành phố hoặc vị trí tọa độ GPS, phục vụ màn hình Khám phá cửa hàng (`01.01`).
- **Quyền hạn truy cập:** `PUBLIC`
- **Frontend Gọi tại:** `StoreDiscoveryPage.tsx`, `RestaurantCard.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả & Ràng buộc |
|:---|:---|:---:|:---|:---|
| `city` | `String` | Không | `null` | Lọc theo thành phố (vd: `Oslo`, `Bergen`) |
| `activeOnly` | `Boolean` | Không | `true` | Chỉ lấy các chi nhánh đang hoạt động |
| `latitude` | `BigDecimal` | Không | `null` | Vĩ độ GPS của người dùng (tính khoảng cách km) |
| `longitude` | `BigDecimal` | Không | `null` | Kinh độ GPS của người dùng |
| `pickupMethod`| `String` | Không | `null` | Lọc theo hình thức: `COUNTER` hoặc `TABLE` |

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
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Linn Sushi Sentrum",
      "address": "Prinsens gate 10C, 0153 Oslo",
      "city": "Oslo",
      "latitude": 59.9114910,
      "longitude": 10.7482320,
      "distanceKm": 0.45,
      "phoneNumber": "+4722334455",
      "openingHours": "10:00 - 22:00",
      "active": true,
      "acceptsPickup": true,
      "acceptsDinein": true,
      "createdAt": "2026-11-05T00:00:00Z"
    },
    {
      "id": "b2eebc99-9c0b-4ef8-bb6d-6bb9bd380b02",
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Linn Sushi Majorstuen",
      "address": "Valkyriegata 15, 0366 Oslo",
      "city": "Oslo",
      "latitude": 59.9298210,
      "longitude": 10.7163820,
      "distanceKm": 2.10,
      "phoneNumber": "+4722334456",
      "openingHours": "11:00 - 21:30",
      "active": true,
      "acceptsPickup": true,
      "acceptsDinein": false,
      "createdAt": "2026-11-05T00:00:00Z"
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi (Error Responses)
- **Trường hợp Tọa độ GPS không hợp lệ (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_COORDINATES",
    "message": "Latitude must be between -90 and 90, Longitude between -180 and 180.",
    "details": [
      {
        "field": "latitude",
        "rejectedValue": "95.5",
        "message": "Out of range"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 1.3: Chi tiết Chi nhánh Cửa hàng (Get Store Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}`
- **Operation ID:** `getStoreById`
- **Mô tả:** Lấy toàn bộ thông tin chi tiết của một chi nhánh bao gồm chính sách nhận tại quầy / ăn tại bàn, tọa độ bản đồ và giờ đóng/mở cửa.
- **Frontend Gọi tại:** `ConfirmStoreModal.tsx`, `OrderOptionsModal.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID định danh chi nhánh cửa hàng |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Store details retrieved successfully",
  "data": {
    "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "name": "Linn Sushi Sentrum",
    "address": "Prinsens gate 10C, 0153 Oslo",
    "city": "Oslo",
    "latitude": 59.9114910,
    "longitude": 10.7482320,
    "phoneNumber": "+4722334455",
    "openingHours": "10:00 - 22:00",
    "active": true,
    "acceptsPickup": true,
    "acceptsDinein": true,
    "createdAt": "2026-11-05T00:00:00Z"
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
- **Không tìm thấy Cửa hàng (`404 Not Found`):**
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

### API 1.4: Quét Mã QR Nhận diện Bàn ăn (Resolve Table by QR Code)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/tables/qr/{qrToken}`
- **Operation ID:** `resolveTableByQrToken`
- **Mô tả:** Khi khách hàng ngồi tại bàn và dùng camera quét mã QR dán trên bàn, API này giải mã token thành số bàn (`Table 05`) và chi nhánh cửa hàng tương ứng để tự động gán vào giỏ hàng.
- **Frontend Gọi tại:** `StoreDiscoveryPage.tsx`, `CheckoutView.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `qrToken` | `String` | **Có** | `qr_linn_sentrum_table_05` |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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

#### 3. Phản hồi Lỗi
- **Mã QR không hợp lệ hoặc bị rách/hết hạn (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_QR_TOKEN",
    "message": "The scanned QR token is invalid or does not match any table in this store.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
