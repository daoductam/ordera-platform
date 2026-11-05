# Phân hệ 02: Thực đơn & Món ăn API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Danh mục Thực đơn, Món ăn & Topping (Catalog & Modifiers)**  
> **Ánh xạ Frontend:** `frontend/src/components/store-discovery/MenuScreen.tsx`, `ProductDetailModal`, `ReorderCartView.tsx`  
> **Bảng Database:** `categories`, `products`, `product_modifier_groups`, `product_modifiers`

---

### API 2.1: Danh mục Món ăn của Cửa hàng (Get Categories)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/stores/{storeId}/categories`
- **Operation ID:** `getCategoriesByStore`
- **Mô tả:** Lấy danh sách các nhóm danh mục món ăn (Sushi Rolls, Boba Tea, Sides, Desserts) sắp xếp theo thứ tự hiển thị `display_order` trên thanh cuộn ngang của menu.
- **Quyền hạn:** `PUBLIC`
- **Frontend Gọi tại:** `MenuScreen.tsx` (Horizontal Category Bar)

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
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01",
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Sushi Rolls",
      "displayOrder": 1,
      "active": true
    },
    {
      "id": "c2eebc99-9c0b-4ef8-bb6d-6bb9bd380c02",
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Boba Tea",
      "displayOrder": 2,
      "active": true
    },
    {
      "id": "c3eebc99-9c0b-4ef8-bb6d-6bb9bd380c03",
      "tenantId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Sides & Appetizers",
      "displayOrder": 3,
      "active": true
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
- **Mô tả:** Trả về cây thực đơn hoàn chỉnh gồm các danh mục và toàn bộ món ăn bên trong để frontend hiển thị danh sách cuộn liên tục (Infinite Scroll / Section List).
- **Frontend Gọi tại:** `MenuScreen.tsx`, `HomePage.tsx`

#### 1. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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
        "name": "Sushi Rolls",
        "displayOrder": 1,
        "products": [
          {
            "id": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
            "name": "Aurora",
            "description": "Cá hồi tươi Na Uy phủ bơ béo ngậy, sốt teriyaki đặc biệt, 8 miếng",
            "basePrice": 89.00,
            "imageUrl": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
            "available": true,
            "vatRate": 15.00,
            "hasModifiers": true
          },
          {
            "id": "d2eebc99-9c0b-4ef8-bb6d-6bb9bd380d02",
            "name": "Tuna Maki",
            "description": "Cá ngừ đại dương tươi rói cuộn rong biển giòn tan, 8 miếng",
            "basePrice": 89.00,
            "imageUrl": "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56",
            "available": true,
            "vatRate": 15.00,
            "hasModifiers": true
          },
          {
            "id": "d3eebc99-9c0b-4ef8-bb6d-6bb9bd380d03",
            "name": "Dragon Roll Special",
            "description": "Lươn Nhật nướng sốt unagi ngọt đậm đà phủ bơ sáp",
            "basePrice": 145.00,
            "imageUrl": "https://images.unsplash.com/photo-1553621042-f6e147245754",
            "available": false,
            "vatRate": 15.00,
            "hasModifiers": false
          }
        ]
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

### API 2.3: Chi tiết Món ăn & Danh sách Tùy chọn Modifiers (Get Product Modifiers)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/products/{productId}/modifiers`
- **Operation ID:** `getProductModifiers`
- **Mô tả:** Lấy toàn bộ các nhóm tùy chọn (Size, Topping, Đá, Ngọt) của món ăn phục vụ hiển thị Modal tùy chỉnh (Figma screen `01.03`).
- **Frontend Gọi tại:** `ProductDetailModal.tsx`

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Ví dụ |
|:---|:---|:---:|:---|
| `productId` | `UUID` | **Có** | `d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01` |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Product modifier groups retrieved successfully",
  "data": {
    "productId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01",
    "productName": "Aurora",
    "basePrice": 89.00,
    "imageUrl": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
    "modifierGroups": [
      {
        "id": "e1eebc99-9c0b-4ef8-bb6d-6bb9bd380e01",
        "name": "Kích cỡ Phần ăn (Size)",
        "minSelection": 1,
        "maxSelection": 1,
        "displayOrder": 1,
        "modifiers": [
          {
            "id": "f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01",
            "name": "Tiêu chuẩn (8 miếng)",
            "extraPrice": 0.00,
            "isDefault": true
          },
          {
            "id": "f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02",
            "name": "Phần lớn (12 miếng)",
            "extraPrice": 39.00,
            "isDefault": false
          }
        ]
      },
      {
        "id": "e2eebc99-9c0b-4ef8-bb6d-6bb9bd380e02",
        "name": "Topping & Sốt Kèm thêm",
        "minSelection": 0,
        "maxSelection": 3,
        "displayOrder": 2,
        "modifiers": [
          {
            "id": "f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03",
            "name": "Thêm Trứng cá hồi (Salmon Roe)",
            "extraPrice": 25.00,
            "isDefault": false
          },
          {
            "id": "f4eebc99-9c0b-4ef8-bb6d-6bb9bd380f04",
            "name": "Thêm Gừng hồng & Wasabi tươi",
            "extraPrice": 10.00,
            "isDefault": false
          },
          {
            "id": "f5eebc99-9c0b-4ef8-bb6d-6bb9bd380f05",
            "name": "Sốt Cay Spicy Mayo Extra",
            "extraPrice": 8.00,
            "isDefault": false
          }
        ]
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

#### 3. Phản hồi Lỗi
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
