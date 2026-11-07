# Phân hệ 08: Thông Báo Hệ Thống & Hỗ Trợ Khách Hàng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Thông báo Đa danh mục ("Content for you" - Messages & News), Hộp thư Cá nhân, Hỗ trợ Thiếu điểm & Khiếu nại Hóa đơn**  
> **Ánh xạ Prototype:** `docs/prototype/5-thong-bao-va-ho-tro-diem/` (`05.01`, `05.02`, `05.03`)  
> **Ánh xạ Frontend:** `frontend/src/components/notifications/NotificationListView.tsx`, `NotificationDetailModal.tsx`, `MissingPointsBanner.tsx`  
> **Bảng Database:** `system_notifications`, `user_notifications`, `support_inquiries`

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
- **Mô tả:** Trả về số lượng thông báo chưa đọc của hội viên hiện tại để hiển thị badge đỏ trên biểu tượng quả chuông ở thanh Header (`05.01`).
- **Màn hình Prototype:** `05.01`
- **Frontend Gọi tại:** `Navbar.tsx`, `NotificationBell.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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

#### 3. Phản hồi Lỗi
- **Chưa đăng nhập (`401 Unauthorized`):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập để kiểm tra số lượng thông báo.",
    "details": null
  },
  "timestamp": "2026-11-05T20:56:15.000Z"
}
```

---

### API 8.2: Danh Sách Thông Báo Đa Danh Mục (Get Notifications List)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications`
- **Operation ID:** `listNotifications`
- **Mô tả:** Trả về danh sách thông báo gửi tới người dùng hoặc toàn hệ thống, lọc theo 2 tab chính: `Messages` (tin nhắn riêng, đối soát điểm) hoặc `News` (tin tức sự kiện, nâng cấp hệ thống). Hỗ trợ phân trang và tìm kiếm theo trạng thái đọc.
- **Màn hình Prototype:** `05.02`
- **Frontend Gọi tại:** `NotificationListView.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---|:---|
| `type` | `String` | Không | `MESSAGES` | Lọc theo tab: `MESSAGES` hoặc `NEWS` |
| `isRead` | `Boolean` | Không | `null` | Lọc theo trạng thái đọc (`true`/`false`) |
| `page` | `Integer` | Không | `0` | Số trang (bắt đầu từ 0) |
| `size` | `Integer` | Không | `20` | Số lượng mục trên một trang |

#### 3. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
        "type": "MESSAGES",
        "category": "Messages",
        "title": "Missing Your Points?",
        "summary": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!",
        "date": "Hôm nay, 10:30",
        "createdAt": "2026-11-05T10:30:00.000Z",
        "isRead": false,
        "bannerImageUrl": "https://storage.ordera.no/notifications/missing-points-banner.jpg",
        "actionLabel": "Learn more",
        "actionUrl": "/notifications/notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001"
      },
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "type": "NEWS",
        "category": "News",
        "title": "Chào mừng nâng cấp Ordera Loyalty 2.0",
        "summary": "Hệ thống tích điểm thành viên mới đã chính thức hoạt động với nhiều đặc quyền hấp dẫn.",
        "date": "Hôm qua, 15:00",
        "createdAt": "2026-11-04T15:00:00.000Z",
        "isRead": true,
        "bannerImageUrl": "https://storage.ordera.no/notifications/welcome-banner.jpg",
        "actionLabel": "Xem chi tiết",
        "actionUrl": "/loyalty/tiers"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
  },
  "timestamp": "2026-11-05T20:56:15.000Z"
}
```

---

### API 8.3: Chi Tiết Thông Báo (Get Notification Detail)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications/{id}`
- **Operation ID:** `getNotificationDetail`
- **Mô tả:** Lấy thông tin chi tiết một thông báo bao gồm ảnh minh họa lớn, toàn bộ đoạn văn nội dung và các trường dữ liệu hỗ trợ (như hướng dẫn gửi đối soát hóa đơn cũ) tại màn hình `05.03`. Tự động đánh dấu là đã đọc.
- **Màn hình Prototype:** `05.03`
- **Frontend Gọi tại:** `NotificationDetailModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `id` | `UUID` | **Có** | ID của thông báo |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Notification detail retrieved successfully",
  "data": {
    "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "category": "Messages",
    "title": "Missing Your Points?",
    "bannerImageUrl": "https://storage.ordera.no/notifications/missing-points-banner.jpg",
    "content": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!\n\nContact us directly:\n- Email: info@ordera.no\n- Phone: (+47) 97362748\n\nOur team will cross-check your previous transactions and restore your points within 24 hours.",
    "isRead": true,
    "createdAt": "2026-11-05T10:30:00.000Z",
    "hasSupportForm": true,
    "supportEmail": "info@ordera.no",
    "supportPhone": "+4797362748"
  },
  "timestamp": "2026-11-05T20:56:15.000Z"
}
```

#### 4. Phản hồi Lỗi
- **Không tìm thấy thông báo (`404 Not Found`):**
```json
{
  "success": false,
  "error": {
    "code": "NOTIFICATION_NOT_FOUND",
    "message": "Không tìm thấy thông báo với ID được chỉ định.",
    "details": null
  },
  "timestamp": "2026-11-05T20:56:15.000Z"
}
```

---

### API 8.4: Đánh Dấu Một Thông Báo Đã Đọc (Mark Notification as Read)

- **HTTP Method:** `PATCH`
- **URL Path:** `/api/v1/notifications/{id}/read`
- **Operation ID:** `markNotificationAsRead`
- **Mô tả:** Đánh dấu một thông báo cụ thể từ trạng thái chưa đọc sang đã đọc khi người dùng bấm vào xem.
- **Màn hình Prototype:** `05.03`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `id` | `UUID` | **Có** | ID của thông báo |

#### 3. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380001",
    "isRead": true,
    "readAt": "2026-11-05T20:55:30.000Z"
  },
  "timestamp": "2026-11-05T20:55:30.000Z"
}
```

---

### API 8.5: Đánh Dấu Tất Cả Thông Báo Đã Đọc (Mark All Notifications Read)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/notifications/read-all`
- **Operation ID:** `markAllNotificationsAsRead`
- **Mô tả:** Đánh dấu tất cả thông báo đang có trong hộp thư của hội viên thành đã đọc và đặt số lượng chưa đọc về 0.
- **Màn hình Prototype:** `05.02`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Phản hồi Thành công (200 OK)
```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": {
    "markedCount": 3,
    "unreadCount": 0
  },
  "timestamp": "2026-11-05T20:56:00.000Z"
}
```

---

### API 8.6: Gửi Biểu Mẫu Hỗ Trợ Thiếu Điểm (Submit Missing Points Inquiry)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/support/missing-points/submit`
- **Operation ID:** `submitMissingPointsInquiry`
- **Mô tả:** Khách hàng nhấn nút `[Submit Form]` trên màn hình `05.03` để gửi yêu cầu tra cứu và phục hồi điểm thưởng cũ bị thiếu sau đợt di chuyển hệ thống.
- **Màn hình Prototype:** `05.03`
- **Frontend Gọi tại:** `NotificationDetailModal.tsx`

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu | Mô tả |
|:---|:---|:---:|:---|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | ID Tenant thương hiệu |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` | Cho phép cả khách chưa đăng nhập gửi hỗ trợ |
| `Content-Type` | `String` | **Có** | `application/json` | Định dạng dữ liệu |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `email` | `String` | Có | `@NotBlank`, `@Email` | Email nhận kết quả phản hồi của hỗ trợ |
| `phoneNumber` | `String` | Có | Định dạng E.164 | SĐT dùng đăng ký thẻ trước đây |
| `receiptCode` | `String` | Không | `@Size(max=64)` | Mã hóa đơn đối chiếu gần nhất (nếu có) |
| `orderDate` | `LocalDate` | Không | `YYYY-MM-DD` | Ngày mua đơn hàng gần nhất |
| `billAmount` | `BigDecimal` | Không | `@DecimalMin("0.00")` | Số tiền đơn hàng đã mua |
| `note` | `String` | Có | `@NotBlank`, `@Size(min=5, max=1000)` | Nội dung chi tiết cần trợ giúp |
| `receiptPhotoUrl`| `String`| Không | URL hợp lệ | Ảnh chụp hóa đơn hoặc bằng chứng |

```json
{
  "email": "info@ordera.no",
  "phoneNumber": "+4797362748",
  "receiptCode": "RCP-907",
  "orderDate": "2025-03-17",
  "billAmount": 290.00,
  "note": "Tôi mua đơn 290 NOK tại quầy Shop 1 nhưng chưa được cộng điểm vào hệ thống mới.",
  "receiptPhotoUrl": "https://storage.ordera.no/claims/rcp-907.jpg"
}
```

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Phiếu yêu cầu tra cứu điểm đã được gửi thành công. Đội ngũ hỗ trợ sẽ đối soát và phản hồi trong 24 giờ.",
  "data": {
    "ticketId": "inq-20250317-009",
    "status": "PENDING",
    "email": "info@ordera.no",
    "phoneNumber": "+4797362748",
    "createdAt": "2026-11-05T20:57:00Z"
  },
  "timestamp": "2026-11-05T20:57:00.000Z"
}
```

#### 4. Phản hồi Lỗi (RFC 7807)
- **Dữ liệu không hợp lệ (`422 Unprocessable Entity`):**
```json
{
  "type": "https://api.orderaapp.com/errors/validation-failed",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Email hoặc số điện thoại không đúng định dạng.",
  "instance": "/api/v1/support/missing-points/submit",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-11-05T20:57:00.000Z"
}
```
