# Phân hệ 08: Thông Báo Hệ Thống & Hỗ Trợ Khách Hàng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Thông báo Đa danh mục ("Content for you" - Messages & News), Hỗ trợ Thiếu điểm & Khiếu nại Hóa đơn**  
> **Ánh xạ Prototype:** `docs/prototype/5-thong-bao-va-ho-tro-diem/` (`05.01`, `05.02`, `05.03`)  
> **Ánh xạ Frontend:** `frontend/src/components/notifications/NotificationListView.tsx`, `NotificationDetailModal.tsx`  
> **Bảng Database:** `system_notifications`, `user_notifications`, `support_inquiries`, `support_claims`

---

### Danh sách Endpoint trong Phân hệ 08

| STT | Endpoint | Method | Tên thao tác / Mục đích | Màn hình Prototype |
|---|---|:---:|---|:---:|
| 8.1 | `/api/v1/notifications/unread-count` | `GET` | Lấy số lượng thông báo chưa đọc (render huy hiệu đỏ trên icon quả chuông) | `05.01` |
| 8.2 | `/api/v1/notifications` | `GET` | Danh sách thông báo lọc theo 2 tabs: `Messages` (tin hỗ trợ) hoặc `News` (tin tức) | `05.02` |
| 8.3 | `/api/v1/notifications/{id}` | `GET` | Chi tiết thông báo (ảnh banner đồ ăn, nội dung chỉ dẫn, email hỗ trợ) | `05.03` |
| 8.4 | `/api/v1/notifications/{id}/read` | `PATCH` | Đánh dấu một thông báo đã đọc | `05.03` |
| 8.5 | `/api/v1/notifications/read-all` | `PUT` | Đánh dấu tất cả thông báo đã đọc | `05.02` |
| 8.6 | `/api/v1/support/missing-points/submit` | `POST` | Gửi biểu mẫu yêu cầu hỗ trợ đối soát điểm thiếu | `05.03` (Nút Submit Form) |

---

### API 8.1: Lấy Số Lượng Thông Báo Chưa Đọc (Get Unread Notifications Count)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications/unread-count`
- **Operation ID:** `getUnreadNotificationCount`
- **Mô tả:** Trả về số lượng thông báo chưa đọc để hiển thị badge đỏ trên quả chuông ở thanh Header (`05.01`).

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Unread count retrieved",
  "data": {
    "unreadCount": 1
  },
  "timestamp": "2026-11-05T20:56:15.000Z"
}
```

---

### API 8.2: Lấy Danh Sách Thông Báo Đa Danh Mục (Get Notifications List)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications`
- **Operation ID:** `listNotifications`
- **Mô tả:** Trả về danh sách thông báo gửi tới người dùng, lọc theo 2 tab chính: `Messages` (tin nhắn riêng, khiếu nại điểm) hoặc `News` (tin tức sự kiện thương hiệu).
- **Màn hình Prototype:** `05.02`

#### 1. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---|:---|
| `type` | `String` | Không | `MESSAGES` | Lọc theo tab: `MESSAGES` hoặc `NEWS` |
| `isRead` | `Boolean` | Không | `null` | Lọc trạng thái đọc |
| `page` | `Integer` | Không | `0` | Số trang |
| `size` | `Integer` | Không | `10` | Kích thước trang |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "type": "MESSAGES",
        "title": "Missing Your Points?",
        "summary": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!",
        "isRead": false,
        "actionLabel": "Learn more",
        "createdAt": "2026-11-05T10:30:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:55:00.000Z"
}
```

---

### API 8.3: Chi Tiết Thông Báo & Hỗ Trợ (Get Notification Details)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications/{id}`
- **Operation ID:** `getNotificationDetails`
- **Mô tả:** Lấy thông tin bài viết chi tiết gồm hình ảnh banner đồ ăn chất lượng cao, nội dung hướng dẫn, địa chỉ email liên hệ hỗ trợ, và các hành động tương tác (`05.03`).

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `id` | `UUID` | Có | ID của thông báo |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Notification details retrieved",
  "data": {
    "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "type": "MESSAGES",
    "title": "Missing Your Points?",
    "bannerImageUrl": "https://storage.ordera.no/notifications/missing-points-banner.jpg",
    "contentBody": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!\n\nContact us:\nEmail: info@example.com",
    "supportEmail": "info@example.com",
    "hasEmailAction": true,
    "hasFormAction": true,
    "formActionTitle": "Submit Form",
    "isRead": true,
    "createdAt": "2026-11-05T10:30:00Z"
  },
  "timestamp": "2026-11-05T20:55:30.000Z"
}
```

---

### API 8.4: Đánh Dấu Thông Báo Đã Đọc (Mark as Read)

- **HTTP Method:** `PATCH`
- **URL Path:** `/api/v1/notifications/{id}/read`
- **Operation ID:** `markNotificationRead`
- **Mô tả:** Đánh dấu thông báo đã đọc và tự động giảm số lượng unread count.

---

### API 8.5: Đánh Dấu Tất Cả Đã Đọc (Mark All Read)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/notifications/read-all`
- **Operation ID:** `markAllNotificationsRead`
- **Mô tả:** Đánh dấu tất cả thông báo của hội viên là đã đọc.

---

### API 8.6: Gửi Biểu Mẫu Hỗ Trợ Thiếu Điểm (Submit Missing Points Inquiry)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/support/missing-points/submit`
- **Operation ID:** `submitMissingPointsInquiry`
- **Mô tả:** Khách hàng nhấn nút `[Submit Form]` trên màn hình `05.03` để gửi yêu cầu tra cứu và phục hồi điểm thưởng cũ.

#### 1. Request Body Schema
```json
{
  "email": "info@ordera.no",
  "phoneNumber": "+4797362748",
  "receiptCode": "RCP-907",
  "orderDate": "2025-03-17",
  "billAmount": 290.00,
  "note": "Tôi mua đơn 290 NOK tại quầy Shop 1 nhưng chưa được cộng điểm.",
  "receiptPhotoUrl": "https://storage.ordera.no/claims/rcp-907.jpg"
}
```

#### 2. Phản hồi Thành công (201 Created)
```json
{
  "success": true,
  "message": "Phiếu yêu cầu tra cứu điểm đã được gửi thành công. Đội ngũ hỗ trợ sẽ đối soát và phản hồi trong 24 giờ.",
  "data": {
    "ticketId": "inq-20250317-009",
    "status": "PENDING",
    "createdAt": "2026-11-05T20:57:00Z"
  },
  "timestamp": "2026-11-05T20:57:00.000Z"
}
```
