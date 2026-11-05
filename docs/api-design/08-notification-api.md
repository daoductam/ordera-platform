# Phân hệ 08: Thông Báo Hệ Thống & Hỗ Trợ Khách Hàng API Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Phân hệ Thông báo Đa danh mục ("Content for you"), Hộp thư Cá nhân, Ưu đãi & Phiếu Khiếu nại Hỗ trợ**  
> **Ánh xạ Frontend:** `frontend/src/components/notifications/NotificationListView.tsx`, `NotificationDetailModal.tsx`, `frontend/src/types/notification.ts`, `frontend/src/mock/notificationMock.ts`  
> **Bảng Database:** `system_notifications`, `user_notifications`, `support_inquiries`

---

### API 8.1: Lấy Danh Sách Thông Báo Đa Danh Mục (Get Notifications with Category Filter)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications`
- **Operation ID:** `listNotifications`
- **Mô tả:** Trả về danh sách thông báo gửi tới người dùng hoặc toàn hệ thống, lọc theo 4 danh mục chuẩn của ứng dụng: `Messages`, `News`, `Voucher`, `Loyalty Gifts`. Hỗ trợ phân trang và đếm số lượng chưa đọc.
- **Frontend Gọi tại:** `NotificationListView.tsx` (Tabs: Messages, News, Voucher, Loyalty Gifts)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | **Có** | `Bearer eyJhbGciOiJIUzUxMiJ9...` |

#### 2. Query Parameters
| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|:---|:---|:---:|:---|:---|
| `category` | `String` | Không | `null` (Tất cả) | Lọc theo: `Messages`, `News`, `Voucher`, `Loyalty Gifts` |
| `isRead` | `Boolean` | Không | `null` | Lọc theo trạng thái đọc (`true`/`false`) |
| `page` | `Integer` | Không | `0` | Số trang (bắt đầu từ 0) |
| `size` | `Integer` | Không | `20` | Kích thước trang |
| `sort` | `String` | Không | `createdAt,desc` | Thứ tự sắp xếp |

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
        "category": "Messages",
        "title": "Missing Your Points?",
        "summary": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!",
        "detail": "We've upgraded our system to improve your experience. If you don't see your loyalty points, please contact us or submit a request — we'll help you retrieve them!\n\nContact us: Email: info@example.com\nHotline: (+47) 22 33 44 55\n\nOur support team will verify your phone number and historical receipts to credit all accumulated points within 24 hours.",
        "date": "Hôm nay, 10:30",
        "createdAt": "2026-11-05T10:30:00.000Z",
        "isRead": false,
        "type": "missing_points",
        "actionLabel": "Learn more",
        "actionUrl": "/support/missing-points",
        "meta": {
          "supportEmail": "info@example.com"
        }
      },
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380002",
        "category": "News",
        "title": "Chào mừng nâng cấp Ordera Loyalty 2.0",
        "summary": "Hệ thống tích điểm thành viên mới đã chính thức hoạt động với nhiều đặc quyền hấp dẫn.",
        "detail": "Khám phá giao diện mới, tiến trình thăng hạng 6 cấp độ từ New đến Ruby và nhận ngay các ưu đãi độc quyền khi dùng bữa tại các chi nhánh.",
        "date": "Hôm qua, 15:00",
        "createdAt": "2026-11-04T15:00:00.000Z",
        "isRead": true,
        "type": "system_upgrade",
        "actionLabel": "Xem chi tiết",
        "actionUrl": "/loyalty/tiers",
        "meta": null
      },
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380003",
        "category": "Voucher",
        "title": "Voucher 100 NOK đã sẵn sàng trong tài khoản",
        "summary": "Áp dụng giảm trực tiếp 100 NOK cho đơn hàng tối thiểu 250 NOK.",
        "detail": "Voucher có hạn dùng đến 03/12/2026. Bạn có thể sử dụng trực tiếp tại bước thanh toán Checkout.",
        "date": "17 Mar 2026",
        "createdAt": "2026-03-17T09:00:00.000Z",
        "isRead": true,
        "type": "promo_gift",
        "actionLabel": "Dùng ngay",
        "actionUrl": "/cart",
        "meta": {
          "requiredOrderValue": "250 NOK"
        }
      },
      {
        "id": "notif-1ebc99-9c0b-4ef8-bb6d-6bb9bd380004",
        "category": "Loyalty Gifts",
        "title": "Món quà thành viên mới: Cốc giữ nhiệt",
        "summary": "Bạn có 01 quà tặng tri ân có thể nhận tại quầy hoặc thêm vào đơn hàng.",
        "detail": "Món quà phiên bản giới hạn kỷ niệm mùa hè từ thương hiệu Linn Sushi Sentrum.",
        "date": "15 Mar 2026",
        "createdAt": "2026-03-15T08:00:00.000Z",
        "isRead": true,
        "type": "promo_gift",
        "actionLabel": "Đổi quà",
        "actionUrl": "/rewards",
        "meta": {
          "pointsCost": 100
        }
      }
    ],
    "page": {
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 4,
      "totalPages": 1,
      "isLast": true
    },
    "unreadCount": 1
  },
  "timestamp": "2026-11-05T20:55:00.000Z"
}
```

---

### API 8.2: Đánh Dấu Một Thông Báo Đã Đọc (Mark Single Notification as Read)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/notifications/{id}/read`
- **Operation ID:** `markNotificationAsRead`
- **Mô tả:** Cập nhật trạng thái thông báo sang `isRead = true`. Nếu là thông báo chung toàn hệ thống, bản ghi ánh xạ cá nhân `user_notifications` sẽ được tạo/cập nhật cho user hiện tại.

#### 1. Path Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|:---|:---|:---:|:---|
| `id` | `UUID` | Có | ID của thông báo cần đánh dấu |

#### 2. Phản hồi Thành công (200 OK)
- **Status:** `200 OK`
- **Body:**
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

### API 8.3: Đánh Dấu Tất Cả Thông Báo Đã Đọc (Mark All Notifications as Read)

- **HTTP Method:** `PUT`
- **URL Path:** `/api/v1/notifications/read-all`
- **Operation ID:** `markAllNotificationsAsRead`
- **Mô tả:** Đánh dấu toàn bộ thông báo đang ở trạng thái chưa đọc của người dùng hiện tại thành đã đọc.

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
  "message": "All notifications marked as read",
  "data": {
    "markedCount": 3,
    "unreadCount": 0
  },
  "timestamp": "2026-11-05T20:56:00.000Z"
}
```

---

### API 8.4: Lấy Số Lượng Thông Báo Chưa Đọc (Get Unread Notifications Count)

- **HTTP Method:** `GET`
- **URL Path:** `/api/v1/notifications/unread-count`
- **Operation ID:** `getUnreadNotificationCount`
- **Mô tả:** Trả về số lượng thông báo chưa đọc để hiển thị badge đỏ trên biểu tượng quả chuông hoặc bottom navigation.

#### 1. Phản hồi Thành công (200 OK)
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

---

### API 8.5: Gửi Yêu Cầu Hỗ Trợ & Khiếu Nại Điểm Thưởng (Submit Support / Missing Points Inquiry)

- **HTTP Method:** `POST`
- **URL Path:** `/api/v1/support/inquiries`
- **Operation ID:** `submitSupportInquiry`
- **Mô tả:** Khách hàng gửi biểu mẫu yêu cầu hỗ trợ hoặc tra cứu điểm thiếu sau khi nâng cấp hệ thống. Hệ thống tạo phiếu hỗ trợ `support_inquiries`, gửi email thông báo cho ban quản trị và gửi email xác nhận cho khách hàng.
- **Frontend Gọi tại:** `NotificationDetailModal.tsx` (`handleSubmit`, `SupportInquiryDto`)

#### 1. Request Headers
| Header | Kiểu | Bắt buộc | Mẫu |
|:---|:---|:---:|:---|
| `X-Tenant-ID` | `UUID` | **Có** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `Authorization` | `String` | Tùy chọn | `Bearer eyJhbGciOiJIUzUxMiJ9...` (Khách có thể gửi hỗ trợ mà chưa đăng nhập) |

#### 2. Request Body Schema
| Trường | Kiểu | Bắt buộc | Ràng buộc | Mô tả |
|:---|:---|:---:|:---|:---|
| `email` | `String` | Có | `@Email`, `@NotBlank` | Email khách để nhận phản hồi (VD: `henrytran.bi@gmail.com`) |
| `phone` | `String` | Có | `@NotBlank`, Định dạng E.164 | SĐT khách để đối chiếu dữ liệu điểm (VD: `+4797362748`) |
| `previousEmailOrPhone`| `String` | Không | `@Size(max=128)` | SĐT hoặc Email cũ trước khi nâng cấp (nếu có) |
| `message` | `String` | Có | `@NotBlank`, `@Size(min=5, max=2000)` | Chi tiết nội dung cần trợ giúp |
| `agreedToTerms` | `Boolean` | Có | Phải là `true` | Chấp thuận điều khoản xử lý dữ liệu cá nhân |

```json
{
  "email": "henrytran.bi@gmail.com",
  "phone": "+4797362748",
  "previousEmailOrPhone": "+4798000000",
  "message": "Tôi thấy số điểm tích lũy cũ (khoảng 3500 điểm) chưa được đồng bộ sang hệ thống mới. Nhờ bên hỗ trợ kiểm tra giúp với hóa đơn gần nhất.",
  "agreedToTerms": true
}
```

#### 3. Phản hồi Thành công (201 Created)
- **Status:** `201 Created`
- **Body:**
```json
{
  "success": true,
  "message": "Support inquiry submitted successfully. Our team will verify and reply within 24 hours.",
  "data": {
    "ticketId": "inq-20261105-0042",
    "status": "OPEN",
    "email": "henrytran.bi@gmail.com",
    "phone": "+4797362748",
    "createdAt": "2026-11-05T20:57:00.000Z",
    "estimatedResolutionHours": 24
  },
  "timestamp": "2026-11-05T20:57:00.000Z"
}
```

#### 4. Phản hồi Lỗi (RFC 7807)
##### Lỗi 400 Bad Request — Chưa đồng ý điều khoản
```json
{
  "type": "https://api.orderaapp.com/errors/terms-not-accepted",
  "title": "Terms Agreement Required",
  "status": 400,
  "detail": "You must agree to the terms of data processing to submit an inquiry",
  "instance": "/api/v1/support/inquiries",
  "code": "TERMS_NOT_ACCEPTED",
  "timestamp": "2026-11-05T20:57:00.000Z"
}
```
