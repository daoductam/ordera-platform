# Phân hệ 02: Thực đơn & Món ăn API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Danh mục Thực đơn, Nhóm con, Banner Khuyến mãi & Tùy chọn Món ăn (Catalog, Subcategories & Modifiers)**  
> **Ánh xạ Prototype:** `docs/prototype/1-kham-pha-va-chon-cua-hang/` (`01.06`, `01.07`)  
> **Ánh xạ Frontend:** `frontend/src/components/store-discovery/MenuScreen.tsx`, `ProductDetailModal`, `ReorderCartView.tsx`  
> **Bảng Database:** `categories`, `subcategories`, `products`, `product_modifier_groups`, `product_modifiers`, `promotion_banners`

---

### Danh sách Endpoint trong Phân hệ 02

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 2.1 | `/api/v1/stores/{storeId}/categories` | `GET` | Lấy danh sách danh mục (Popular, Maki, Nigiri...) kèm nhóm con (Subcategory pills) | `01.06`, `01.07` |
| 2.2 | `/api/v1/stores/{storeId}/menu` | `GET` | Toàn bộ cây thực đơn cuộn theo nhóm (Section List) kèm giá khuyến mãi | `01.06`, `01.07` |
| 2.3 | `/api/v1/products/{productId}/modifiers` | `GET` | Chi tiết món ăn, các nhóm tùy chọn (Size, Toppings, Mức đá, Mức đường) | `01.06`, `06.06` |
| 2.4 | `/api/v1/stores/{storeId}/promotions/banners` | `GET` | Lấy banner chiến dịch tiếp thị ("Summer Sales: 20% Off") | `01.06`, `01.07` |
| 2.5 | `/api/v1/stores/{storeId}/products/search` | `GET` | Tìm kiếm món ăn theo từ khóa cho nút Search ở thanh điều hướng | `01.06` |

---

### API 2.1: Danh mục & Nhóm con Món ăn (Get Categories with Subcategories)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/categories`
- **Operation ID:** `getCategoriesByStore`
- **Mô tả:** Lấy danh sách các nhóm danh mục chính (*Popular, Maki, Nigiri, Drinks, Hot Food*) cùng danh sách nhóm con (*Fresh maki, Fried maki...*) phục vụ hiển thị thanh cuộn ngang và các nút pill lọc nhanh.
- **Màn hình Prototype:** `01.06`, `01.07`
- **Frontend Gọi tại:** `MenuScreen.tsx` (Horizontal Category Bar)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID chi nhánh cửa hàng |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c00",
      "name": "Popular",
      "displayOrder": 0,
      "icon": "flame",
      "subcategories": []
    },
    {
      "id": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
      "name": "Maki",
      "displayOrder": 1,
      "icon": "sushi",
      "subcategories": [
        {
          "id": "sub1ebc9-9c0b-4ef8-bb6d-6bb9bd380s01",
          "name": "Fresh maki",
          "displayOrder": 1
        },
        {
          "id": "sub2ebc9-9c0b-4ef8-bb6d-6bb9bd380s02",
          "name": "Fried maki",
          "displayOrder": 2
        }
      ]
    },
    {
      "id": "c2eebc99-9c0b-4ef8-bb6d-6bb9bd380c02",
      "name": "Nigiri",
      "displayOrder": 2,
      "subcategories": []
    },
    {
      "id": "c3eebc99-9c0b-4ef8-bb6d-6bb9bd380c03",
      "name": "Drinks",
      "displayOrder": 3,
      "subcategories": []
    },
    {
      "id": "c4eebc99-9c0b-4ef8-bb6d-6bb9bd380c04",
      "name": "Hot Food",
      "displayOrder": 4,
      "subcategories": []
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Không tìm thấy Cửa hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Store with ID 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b99' does not exist.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 2.2: Cây Thực Đơn Đầy Đủ (Get Store Menu)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/menu`
- **Operation ID:** `getStoreMenu`
- **Mô tả:** Lấy toàn bộ cây danh mục và danh sách món ăn phân theo từng nhóm (Section List) hiển thị trên màn hình Menu (`01.06`, `01.07`).
- **Màn hình Prototype:** `01.06`, `01.07`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID chi nhánh đang xem thực đơn |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Store menu retrieved successfully",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "categories": [
      {
        "categoryId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
        "categoryName": "Maki",
        "subcategories": [
          {
            "subcategoryId": "sub1ebc9-9c0b-4ef8-bb6d-6bb9bd380s01",
            "subcategoryName": "Fresh maki",
            "products": [
              {
                "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
                "name": "Laks Maki",
                "description": "8 miếng cuộn cá hồi Na Uy tươi kèm bơ sáp và dưa chuột giòn",
                "basePrice": 150.00,
                "originalPrice": 180.00,
                "piecesText": "8 biter",
                "dietaryTags": ["Popular", "Gluten Free"],
                "imageUrl": "https://storage.ordera.no/products/laks-maki.jpg",
                "isPopular": true,
                "isAvailable": true,
                "hasModifiers": true
              },
              {
                "id": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
                "name": "Tuna Maki",
                "description": "8 miếng cuộn cá ngừ đại dương với sốt cay đặc biệt",
                "basePrice": 72.00,
                "originalPrice": null,
                "piecesText": "8 biter",
                "dietaryTags": ["Spicy"],
                "imageUrl": "https://storage.ordera.no/products/tuna-maki.jpg",
                "isPopular": false,
                "isAvailable": true,
                "hasModifiers": true
              }
            ]
          }
        ]
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

- **Không tìm thấy Cửa hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Store with ID 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b99' does not exist.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 2.3: Chi Tiết Món Ăn & Nhóm Tùy Chọn (Get Product Modifiers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/products/{productId}/modifiers`
- **Operation ID:** `getProductModifiers`
- **Mô tả:** Lấy danh sách các nhóm tùy chọn (Size, Topping, Mức ngọt, Mức đá) khi khách hàng chạm vào một món ăn trong danh sách để mở Modal tùy biến (`01.06`, `06.06`).
- **Màn hình Prototype:** `01.06`, `06.06`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `productId` | `UUID` | **Có** | ID của món ăn |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Product modifiers retrieved successfully",
  "data": {
    "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
    "productName": "Aurora",
    "basePrice": 89.00,
    "imageUrl": "https://storage.ordera.no/products/aurora.jpg",
    "modifierGroups": [
      {
        "id": "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380e01",
        "name": "Size",
        "minSelection": 1,
        "maxSelection": 1,
        "modifiers": [
          { "id": "m1", "name": "M", "extraPrice": 0.00, "isDefault": false },
          { "id": "m2", "name": "L", "extraPrice": 15.00, "isDefault": true }
        ]
      },
      {
        "id": "e2eebc99-9c0b-4ef8-bb6d-6bb9bd380e02",
        "name": "Topping",
        "minSelection": 0,
        "maxSelection": 3,
        "modifiers": [
          { "id": "m3", "name": "Mango Popping", "extraPrice": 10.00 },
          { "id": "m4", "name": "Strawberry Popping", "extraPrice": 10.00 },
          { "id": "m5", "name": "Tapioca", "extraPrice": 8.00 }
        ]
      },
      {
        "id": "e3eebc99-9c0b-4ef8-bb6d-6bb9bd380e03",
        "name": "Sweetness Level",
        "minSelection": 1,
        "maxSelection": 1,
        "modifiers": [
          { "id": "m6", "name": "Standard", "extraPrice": 0.00, "isDefault": true },
          { "id": "m7", "name": "Less Sweet (50%)", "extraPrice": 0.00 }
        ]
      },
      {
        "id": "e4eebc99-9c0b-4ef8-bb6d-6bb9bd380e04",
        "name": "Ice Level",
        "minSelection": 1,
        "maxSelection": 1,
        "modifiers": [
          { "id": "m8", "name": "Standard Ice", "extraPrice": 0.00 },
          { "id": "m9", "name": "Less Ice", "extraPrice": 0.00, "isDefault": true }
        ]
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

- **Không tìm thấy Món ăn (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product with ID 'd1eebc99-9c0b-4ef8-bb6d-6bb9bd380d99' does not exist.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 2.4: Banner Khuyến mãi Tiếp thị (Get Promotion Banners)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/promotions/banners`
- **Operation ID:** `getPromotionBanners`
- **Mô tả:** Lấy danh sách banner chiến dịch tiếp thị kích hoạt tại chi nhánh (như *"Summer Sales: 20% Off"* trong prototype `01.06`, `01.07`) hiển thị phía trên danh mục món ăn.
- **Màn hình Prototype:** `01.06`, `01.07`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | `b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01` |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Promotion banners retrieved successfully",
  "data": [
    {
      "id": "b-summer-2026",
      "title": "Summer Sales: 20% Off",
      "subtitle": "Áp dụng giảm 20% cho toàn bộ thực đơn Sushi cuộn tươi trong tuần này.",
      "imageUrl": "https://storage.ordera.no/promotions/summer-banner.jpg",
      "ctaLabel": "Learn more",
      "ctaActionType": "NAVIGATE_CATEGORY",
      "targetCategoryId": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
      "active": true
    }
  ],
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

- **Không tìm thấy Cửa hàng (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Store with ID 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b99' does not exist.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 2.5: Tìm kiếm Món ăn (Search Products)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/products/search`
- **Operation ID:** `searchStoreProducts`
- **Mô tả:** Tìm kiếm món ăn theo từ khóa nhập vào khi người dùng chạm vào icon kính lúp trên thanh điều hướng (`01.06`).
- **Màn hình Prototype:** `01.06`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | `b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01` |

#### 3. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `query` | `String` | Có | Từ khóa tìm kiếm (vd: `maki`, `salmon`, tối thiểu 2 ký tự) |

#### 4. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Search completed",
  "data": [
    {
      "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "name": "Laks Maki",
      "basePrice": 150.00,
      "originalPrice": 180.00,
      "piecesText": "8 biter",
      "dietaryTags": ["Popular", "Gluten Free"],
      "categoryName": "Maki",
      "imageUrl": "https://storage.ordera.no/products/laks-maki.jpg",
      "isPopular": true
    }
  ],
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

- **Từ khóa tìm kiếm không hợp lệ (`400 Bad Request`):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_QUERY_PARAM",
    "message": "Search query must contain at least 2 characters.",
    "details": {
      "field": "query",
      "rejectedValue": "a"
    }
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
    "message": "Store with ID 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b99' does not exist.",
    "details": null
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

## Ghi Chú Kỹ Thuật & Đồng Bộ Cơ Sở Dữ Liệu (Technical & DB Sync Notes)

1. **Đồng bộ Đa khách thuê (Multi-Tenancy):**
   - Mọi truy vấn thực đơn (`categories`, `subcategories`, `products`, `modifiers`, `promotion_banners`) bắt buộc lọc kép theo `tenant_id` lấy từ `TenantContextHolder` và `store_id` (hoặc store áp dụng chung toàn tenant).
2. **Hỗ trợ Hiển thị Giá khuyến mãi & Tag khẩu phần (Prototype 01.06):**
   - Các trường `originalPrice` (giá gạch ngang khi có ưu đãi), `piecesText` (ví dụ `"8 biter"`), và `dietaryTags` (`["Popular", "Gluten Free"]`) được bổ sung trực tiếp vào Product DTO để phục vụ giao diện mà không cần hardcode phía Client.
3. **Mô hình DDL Flyway bổ sung (Upcoming Migration):**
   - Bổ sung bảng `subcategories` hỗ trợ phân nhóm con (Pill buttons: *Fresh maki*, *Fried maki*).
   - Bổ sung bảng `promotion_banners` hỗ trợ banner chiến dịch tiếp thị trên đầu thực đơn cửa hàng.
   - Bổ sung các cột `original_price`, `pieces_text`, `dietary_tags` vào bảng `products`.
   - Bổ sung cột `icon` vào bảng `categories`.
