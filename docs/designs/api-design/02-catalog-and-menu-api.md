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

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `storeId` | `UUID` | **Có** | ID chi nhánh cửa hàng |

#### 2. Phản hồi Thành công (200 OK)
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

---

### API 2.2: Toàn bộ Thực đơn Phân cấp theo Danh mục (Get Full Menu)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/menu`
- **Operation ID:** `getFullStoreMenu`
- **Mô tả:** Trả về cây thực đơn phân cấp hoàn chỉnh gồm các danh mục, nhóm con và toàn bộ món ăn bên trong. Hỗ trợ hiển thị giá khuyến mãi gạch bỏ (`originalPrice: 130.00`, `basePrice: 111.20 NOK`), nhãn `isPopular`, nhãn ăn kiêng dị ứng và nút thêm nhanh `[+]`.
- **Màn hình Prototype:** `01.06`, `01.07`

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Full store menu retrieved successfully",
  "data": {
    "storeId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01",
    "storeName": "Linn Sushi Sentrum",
    "currency": "NOK",
    "categories": [
      {
        "id": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
        "name": "Maki",
        "subcategories": [
          {
            "id": "sub2ebc9-9c0b-4ef8-bb6d-6bb9bd380s02",
            "name": "Fried maki",
            "description": "Fried maki description",
            "products": [
              {
                "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
                "name": "Laks Maki",
                "description": "8 biter maki med laks og kremost. Velg mellom mango eller avokado...",
                "basePrice": 150.00,
                "originalPrice": null,
                "imageUrl": "https://storage.ordera.no/products/laks-maki.jpg",
                "isPopular": true,
                "available": true,
                "dietaryTags": ["SEAFOOD", "CONTAINS_DAIRY"],
                "hasModifiers": true
              },
              {
                "id": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
                "name": "Tuna Maki",
                "description": "8 biter maki med tuna og kremost. Velg mellom mango eller avokado...",
                "basePrice": 111.20,
                "originalPrice": 130.00,
                "imageUrl": "https://storage.ordera.no/products/tuna-maki.jpg",
                "isPopular": true,
                "available": true,
                "dietaryTags": ["SEAFOOD", "SPICY"],
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

---

### API 2.3: Chi tiết Món ăn & Tùy chọn Modifiers (Get Product Modifiers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/products/{productId}/modifiers`
- **Operation ID:** `getProductModifiers`
- **Mô tả:** Lấy toàn bộ các nhóm tùy chọn (Size, Topping, Mức ngọt, Mức đá) của món ăn phục vụ hiển thị Modal chi tiết hoặc chỉnh sửa món trong giỏ hàng.

#### 1. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Product modifier groups retrieved successfully",
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

---

### API 2.4: Banner Khuyến mãi Tiếp thị (Get Promotion Banners)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/promotions/banners`
- **Operation ID:** `getPromotionBanners`
- **Mô tả:** Lấy danh sách banner chiến dịch tiếp thị kích hoạt tại chi nhánh (như *"Summer Sales: 20% Off"* trong prototype `01.06`, `01.07`) hiển thị phía trên danh mục món ăn.

#### 1. Phản hồi Thành công (200 OK)
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

---

### API 2.5: Tìm kiếm Món ăn (Search Products)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/products/search`
- **Operation ID:** `searchStoreProducts`
- **Mô tả:** Tìm kiếm món ăn theo từ khóa nhập vào khi người dùng chạm vào icon kính lúp trên thanh điều hướng (`01.06`).

#### 1. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `query` | `String` | Có | Từ khóa tìm kiếm (vd: `maki`, `salmon`) |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Search completed",
  "data": [
    {
      "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
      "name": "Laks Maki",
      "basePrice": 150.00,
      "categoryName": "Maki",
      "imageUrl": "https://storage.ordera.no/products/laks-maki.jpg",
      "isPopular": true
    }
  ],
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```
