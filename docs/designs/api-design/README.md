# Enterprise API Design Specification
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Trung tâm Tài liệu Thiết kế API Chi tiết (API Design Blueprint)**  
> **Phiên bản:** 2.1 Enterprise Edition (Đồng bộ 100% với Figma Prototype)  
> **Tiêu chuẩn Kỹ thuật:** RESTful API, JSON:API, OpenAPI 3.0, RFC 7807 (Problem Details for HTTP APIs)  
> **Ánh xạ Hệ thống:** Khớp 100% với 8 phân hệ Prototype (`docs/prototype/`), Giao diện Frontend React 19 (`frontend/src/`) và PostgreSQL 16 (`backend/src/main/resources/db/migration/`)

---

## 1. Kiến trúc Tổng thể & Nguyên tắc Thiết kế (Design Principles)

1. **Chuẩn RESTful Nhất quán:** Đặt tên tài nguyên theo danh từ số nhiều (e.g., `/stores`, `/products`, `/vouchers`, `/orders`), phân cấp quan hệ rõ ràng (`/stores/{storeId}/menu`).
2. **Multi-Tenant Isolation Cấp 1 (Strict Tenant Scoping):**
   - 100% Request gọi vào hệ thống đều bắt buộc phải mang Header `X-Tenant-ID: <UUID>`.
   - Backend lọc toàn bộ dữ liệu qua `TenantInterceptor` kết hợp `TenantContextHolder` và PostgreSQL Row-Level Security (RLS) để triệt tiêu hoàn toàn lỗ hổng IDOR.
3. **Phong bì Phản hồi Chuẩn hóa (Universal Response Envelope):**
   - Mọi response thành công đều bọc trong cấu trúc `ApiResponse<T>` hoặc `ApiPaginatedResponse<T>`.
   - Mọi response lỗi đều trả về mã HTTP chuẩn kèm payload lỗi chi tiết theo chuẩn **RFC 7807**.
4. **Idempotency & Race Condition Prevention:**
   - Các API nhạy cảm về tài chính và điểm số (`/orders/checkout`, `/rewards/{id}/redeem`, `/cart/voucher/apply`) bắt buộc tích hợp khóa phân tán **Redisson Distributed Lock** và kiểm tra `Idempotency-Key` để tránh Double-spending khi mạng chập chờn hoặc người dùng bấm liên tục (Double-tap).
5. **Kế toán Điểm Bất biến (Immutable Points Accounting):**
   - Mọi biến động điểm số đều được lưu vết trong sổ cái kép `points_ledger` (Partition theo `created_at`). Không bao giờ cập nhật điểm bằng câu lệnh ghi đè trực tiếp.

---

## 2. Tiêu chuẩn Request Headers Toàn cục (Global Headers)

| Tên Header | Kiểu Dữ liệu | Bắt buộc | Ví dụ Giá trị | Ý nghĩa & Quy tắc Xử lý |
|:---|:---|:---|:---|:---|
| `X-Tenant-ID` | `UUID` | **BẮT BUỘC** | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | Định danh thương hiệu/đại lý (vd: Linn Sushi Sentrum). Nếu thiếu sẽ trả về lỗi `400 TENANT_HEADER_MISSING`. |
| `Authorization` | `String` | Tùy API | `Bearer eyJhbGciOiJIUzUxMiJ9...` | JWT Access Token đã ký HMAC-SHA512. Bắt buộc đối với các API yêu cầu đăng nhập thành viên hoặc quyền quản trị. |
| `Content-Type` | `String` | Bắt buộc cho POST/PUT | `application/json; charset=UTF-8` | Định dạng dữ liệu gửi lên máy chủ. |
| `Accept` | `String` | Khuyến nghị | `application/json` | Định dạng dữ liệu mong muốn nhận về. |
| `Idempotency-Key` | `String` | Khuyến nghị cho Checkout/Redeem | `idem-9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d` | Chuỗi định danh giao dịch duy nhất để chống thực thi lặp lại cùng một lệnh thanh toán/đổi điểm. |
| `X-Client-Platform` | `String` | Tùy chọn | `WEB_MOBILE`, `KIOSK_POS`, `WEB_ADMIN` | Nhận diện kênh thiết bị gửi yêu cầu để phục vụ thống kê và tối ưu luồng nghiệp vụ. |

---

## 3. Cấu trúc Phong bì Dữ liệu Chuẩn (Data Envelopes)

### 3.1 Thành công Đơn lẻ (`ApiResponse<T>`)
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

### 3.2 Thành công Danh sách Phân trang (`ApiPaginatedResponse<T>`)
```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": [ ... ],
  "meta": {
    "page": 1,
    "limit": 10,
    "totalItems": 45,
    "totalPages": 5,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

### 3.3 Phản hồi Lỗi Chuẩn Doanh nghiệp (RFC 7807 Error Envelope)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Input validation error occurred. Please check field details.",
    "details": [
      {
        "field": "phoneNumber",
        "rejectedValue": "123",
        "message": "Phone number must follow international E.164 format (+47xxxxxxxx)"
      }
    ]
  },
  "timestamp": "2026-11-05T20:45:00.000Z"
}
```

---

## 4. Bảng Tổng hợp Ma trận Endpoint & Ánh xạ Prototype

Hệ thống cung cấp đầy đủ 55 API endpoints chuyên biệt, tương thích hoàn toàn với 73 màn hình Prototype trong thư mục [`docs/prototype/`](../prototype/):

| Phân hệ | Tài liệu API Chi tiết | Số lượng API | Các chức năng nghiệp vụ trọng tâm | Màn hình Prototype Ánh xạ |
|---|---|:---:|---|:---:|
| **01** | [`01-tenant-and-store-api.md`](./01-tenant-and-store-api.md) | 6 | Thông tin thương hiệu, Tùy chọn nhận hàng, Danh sách cửa hàng kèm giờ mở/đóng, Popup xác nhận chi nhánh, Quét mã QR số bàn, Yêu thích cửa hàng | `01.01` đến `01.09` |
| **02** | [`02-catalog-and-menu-api.md`](./02-catalog-and-menu-api.md) | 5 | Danh mục chính & nhóm con (Subcategory pills), Menu cuộn phân cấp kèm giá gạch bỏ, Tùy chọn Modifiers (Size, Toppings, Đá, Đường), Banner marketing Summer Sales, Tìm kiếm món | `01.06`, `01.07` |
| **03** | [`03-auth-and-identity-api.md`](./03-auth-and-identity-api.md) | 6 | Đăng nhập OTP SĐT, Cờ phân nhánh `isProfileComplete`, Đăng ký thành viên 2 bước, Tùy chọn kênh OTP (SMS hoặc Email), Cấp & làm mới JWT, Đăng xuất | `02.01` đến `02.07`, `03.01` đến `03.10` |
| **04** | [`04-loyalty-and-tier-api.md`](./04-loyalty-and-tier-api.md) | 7 | Hồ sơ tổng quan, Bộ đếm huy hiệu đỏ trên các ô tác vụ, Cập nhật thông tin cá nhân, Hoàn thiện hồ sơ mới, Đổi avatar camera, Bảng cấu hình 6 hạng thẻ (`New` $\rightarrow$ `Ruby`), Sổ cái điểm, Khiếu nại thiếu điểm hóa đơn | `04.01` đến `04.15`, `05.03` |
| **05** | [`05-reward-and-voucher-api.md`](./05-reward-and-voucher-api.md) | 11 | Ví cá nhân My Rewards (Voucher & Quà tặng), Kho đổi thưởng bằng điểm (Get More Rewards: 100 NOK, Cốc tumbler), Đổi thưởng trừ điểm, Đổi quà hiện vật, Bộ đếm huy hiệu đỏ ví, Ưu đãi đủ điều kiện trong Checkout, Nhập mã tay qua bàn phím ảo, Áp dụng/Gỡ bỏ voucher & quà | `07.01` đến `07.08`, `08.01` đến `08.12` |
| **06** | [`06-order-and-checkout-api.md`](./06-order-and-checkout-api.md) | 9 | Chi tiết giỏ hàng, Cập nhật số lượng/tùy chọn, Ghi chú đơn (0/200 chars), Dự toán Checkout (VAT 15%, Tips, +170 pts), Đặt hàng Vipps/Card, Lịch sử phân tab (Active & Past), Đặt lại đơn cũ 1-click (Reorder), Gửi biên nhận E-Receipt PDF qua Email | `06.01` đến `06.10` |
| **07** | [`07-kiosk-pos-api.md`](./07-kiosk-pos-api.md) | 5 | Chế độ Kiosk màn hình chạm toàn màn hình: Nhận diện SĐT hội viên, Tích điểm nhanh tại quầy không cần đặt món, Kiosk Self-checkout, Tự động reset màn hình chờ sau 60s, Cấu hình thiết bị | Kiosk POS Mode |
| **08** | [`08-notification-api.md`](./08-notification-api.md) | 6 | Số lượng thông báo chưa đọc (Huy hiệu đỏ trên quả chuông), Danh sách thông báo phân tab (`Messages` & `News`), Chi tiết thông báo banner đồ ăn, Đánh dấu đã đọc, Gửi biểu mẫu tra cứu/khiếu nại thiếu điểm (*Missing Your Points?*) | `05.01`, `05.02`, `05.03` |

---

## 5. Từ điển Mã Lỗi Hệ thống (Global Error Code Dictionary)

| Mã Lỗi (`code`) | HTTP Status | Diễn giải & Tình huống Xảy ra |
|:---|:---:|:---|
| `TENANT_HEADER_MISSING` | `400` | Header `X-Tenant-ID` bị thiếu hoặc không đúng định dạng UUID. |
| `TENANT_NOT_FOUND` | `404` | Không tìm thấy thương hiệu/đại lý tương ứng với ID đã cung cấp. |
| `INVALID_INPUT` | `400` | Dữ liệu gửi lên sai định dạng (JSON malformed, kiểu dữ liệu không khớp). |
| `VALIDATION_FAILED` | `422` | Vi phạm các quy tắc validation dữ liệu đầu vào (`@NotBlank`, `@Size`, v.v.). |
| `UNAUTHORIZED` | `401` | Token JWT bị thiếu, sai chữ ký, hoặc đã hết hạn. |
| `TOKEN_EXPIRED` | `401` | Access token đã quá 24 giờ, cần gọi API refresh-token. |
| `FORBIDDEN` | `403` | Không đủ quyền hạn hoặc cố tình truy cập tài nguyên của Tenant khác (RLS Block). |
| `RESOURCE_NOT_FOUND` | `404` | Không tìm thấy Cửa hàng, Món ăn, Voucher hoặc Đơn hàng yêu cầu. |
| `OTP_INVALID` | `400` | Mã OTP 6 chữ số không khớp với mã đã sinh. |
| `OTP_EXPIRED` | `400` | Mã OTP đã quá thời hạn 120 giây. |
| `OTP_MAX_ATTEMPTS_EXCEEDED` | `429` | Nhập sai OTP quá 5 lần, phiên xác thực bị khóa 15 phút. |
| `OTP_COOLDOWN_ACTIVE` | `429` | Yêu cầu gửi lại OTP khi chưa hết thời gian chờ 120 giây. |
| `POINTS_INSUFFICIENT` | `422` | Số dư điểm khả dụng (`currentPoints`) không đủ để đổi voucher hoặc quà. |
| `GIFT_OUT_OF_STOCK` | `409` | Quà hiện vật (cốc tumbler) đã hết số lượng tồn kho (`stockQuantity <= 0`). |
| `VOUCHER_ALREADY_USED` | `409` | Voucher đã được áp dụng trong đơn hàng khác hoặc trạng thái không phải `AVAILABLE`. |
| `MIN_ORDER_VALUE_NOT_MET` | `422` | Tiền hàng chưa đạt mốc tối thiểu `minOrderValue` của voucher (VD: 250 NOK). |
| `STORE_CLOSED` | `400` | Cửa hàng đang đóng cửa hoặc không tiếp nhận hình thức đặt hàng đã chọn. |
| `CONCURRENT_TRANSACTION_LOCK` | `409` | Đang có một giao dịch khác cùng can thiệp vào điểm/voucher của khách (Redisson Lock). |
| `INTERNAL_SERVER_ERROR` | `500` | Lỗi máy chủ không xác định, đã được ghi vết qua `correlationId`. |
