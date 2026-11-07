# Tài liệu Phân tích Prototype & Luồng Màn hình (Ordera PWA UI/UX Specification)

Tài liệu này tổng hợp toàn bộ 8 phân hệ giao diện người dùng (Prototype Screens) được trích xuất từ bộ thiết kế Figma của nền tảng **Ordera Multi-Tenant Loyalty & Ordering Platform**.

Mỗi thư mục chứa các ảnh chụp màn hình độ phân giải cao kèm file `README.md` phân tích chi tiết:
- **Tên màn hình & Trạng thái UI**
- **Luồng chuyển hướng người dùng (Mermaid Flowchart)**
- **Hành động & Điều kiện kích hoạt (User Interactions & Triggers)**
- **Bảng ánh xạ Màn hình - API (Screen-to-API Mapping)**
- **Xử lý ngoại lệ, Edge cases và Empty states**

---

## Danh mục 8 Phân hệ Prototype

| Phân hệ | Thư mục | Số lượng màn hình | Mô tả nghiệp vụ cốt lõi |
|---|---|:---:|---|
| **01** | [`1-kham-pha-va-chon-cua-hang/`](./1-kham-pha-va-chon-cua-hang/README.md) | 9 | Chọn hình thức nhận (Dine-in, Takeaway, Delivery), tìm chi nhánh, xác nhận cửa hàng và duyệt menu theo danh mục. |
| **02** | [`2-dang-nhap-va-truy-cap-tai-khoan/`](./2-dang-nhap-va-truy-cap-tai-khoan/README.md) | 7 | Đăng nhập không mật khẩu bằng Số điện thoại/Email, xác thực mã OTP 6 số (Redis cooldown 120s) và cấp phát JWT. |
| **03** | [`3-dang-ky-va-xac-thuc-thanh-vien/`](./3-dang-ky-va-xac-thuc-thanh-vien/README.md) | 10 | Đăng ký thành viên 2 bước: điền thông tin cá nhân (DOB tặng quà sinh nhật), chọn kênh OTP (SMS hoặc Email) và tạo tài khoản New. |
| **04** | [`4-ho-so-va-hang-thanh-vien/`](./4-ho-so-va-hang-thanh-vien/README.md) | 15 | Quản lý hồ sơ David Nguyen, động cơ 6 cấp bậc thẻ (`New` -> `Silver` -> `Gold` -> `Platinum` -> `Diamond` -> `Ruby`), cập nhật thông tin và đổi avatar. |
| **05** | [`5-thong-bao-va-ho-tro-diem/`](./5-thong-bao-va-ho-tro-diem/README.md) | 3 | Trung tâm thông báo (Messages & News), hỗ trợ khiếu nại thiếu điểm (*Missing Your Points?*) qua email hoặc biểu mẫu đối soát hóa đơn. |
| **06** | [`6-gio-hang-don-hang-va-bien-nhan/`](./6-gio-hang-don-hang-va-bien-nhan/README.md) | 10 | Giỏ hàng, lịch sử đơn hàng (Active & Past), tính năng Reorder 1-click, màn hình Checkout tính thuế/tips, và gửi E-Receipt qua email. |
| **07** | [`7-uu-dai-va-doi-thuong/`](./7-uu-dai-va-doi-thuong/README.md) | 7 | Ví ưu đãi cá nhân (*My Rewards*), kho đổi thưởng bằng điểm (*Get More Rewards*: voucher 100 NOK, -30%, cốc tumbler), và trạng thái rỗng. |
| **08** | [`8-ap-ma-va-qua-trong-checkout/`](./8-ap-ma-va-qua-trong-checkout/README.md) | 12 | Drawer chọn ưu đãi trực tiếp trong Checkout: áp dụng đồng thời Voucher giảm giá và Quà tặng tri ân vật phẩm, hoặc nhập mã khuyến mãi thủ công. |

---

## Mối liên kết giữa Prototype và Thiết kế API (API Design Mapping)

Tất cả các API được phát hiện từ quá trình rà soát prototype đã được quy chuẩn hóa và đồng bộ 100% vào thư mục [`docs/designs/api-design/`](../designs/api-design/):
- **Cửa hàng & Chi nhánh:** [`01-tenant-and-store-api.md`](../designs/api-design/01-tenant-and-store-api.md)
- **Thực đơn & Danh mục:** [`02-catalog-and-menu-api.md`](../designs/api-design/02-catalog-and-menu-api.md)
- **Xác thực & Danh tính:** [`03-auth-and-identity-api.md`](../designs/api-design/03-auth-and-identity-api.md)
- **Hội viên & 6 Hạng thẻ:** [`04-loyalty-and-tier-api.md`](../designs/api-design/04-loyalty-and-tier-api.md)
- **Voucher, Quà tặng & Đổi thưởng:** [`05-reward-and-voucher-api.md`](../designs/api-design/05-reward-and-voucher-api.md)
- **Giỏ hàng, Đơn hàng & E-Receipt:** [`06-order-and-checkout-api.md`](../designs/api-design/06-order-and-checkout-api.md)
- **Thiết bị quầy POS/Kiosk:** [`07-kiosk-pos-api.md`](../designs/api-design/07-kiosk-pos-api.md)
- **Thông báo & Khiếu nại điểm:** [`08-notification-api.md`](../designs/api-design/08-notification-api.md)
