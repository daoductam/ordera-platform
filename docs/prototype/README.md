# Tài liệu Phân tích Prototype & Luồng Màn hình (Ordera UI/UX Specification)
## Hệ Thống Mobile Web PWA & Trạm Cảm Ứng Kiosk POS

Tài liệu này tổng hợp toàn bộ các phân hệ giao diện người dùng (Prototype Screens) được trích xuất từ bộ thiết kế Figma của nền tảng **Ordera Multi-Tenant Loyalty & Ordering Platform**. Hệ thống bao gồm 2 nhóm nền tảng trải nghiệm số:
1. **Mobile Web PWA (8 Phân hệ):** Trải nghiệm đặt món, tích lũy điểm thưởng, quản lý hạng thẻ và đổi ưu đãi trực tiếp trên điện thoại của khách hàng.
2. **Kiosk POS Touch Terminal (7 Phân hệ):** Trạm đặt món và tích điểm nhanh tự phục vụ toàn màn hình đặt tại quầy thu ngân của nhà hàng (triển khai hoàn thiện sau khi hoàn tất bản mobile web).

Mỗi thư mục chứa các ảnh chụp màn hình độ phân giải cao kèm file `README.md` phân tích chi tiết:
- **Tên màn hình & Trạng thái UI**
- **Luồng chuyển hướng người dùng (Mermaid Flowchart)**
- **Hành động & Điều kiện kích hoạt (User Interactions & Triggers)**
- **Bảng ánh xạ Màn hình - API (Screen-to-API Mapping)**
- **Xử lý ngoại lệ, Edge cases và Empty states**

---

## 📱 Phần 1: Danh mục 8 Phân hệ Mobile Web PWA (73 Màn hình)

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

## 🖥️ Phần 2: Danh mục 7 Phân hệ Kiosk POS Touch Terminal (32 Màn hình)
> *Phân hệ Kiosk được định hướng thực hiện sau cùng sau khi đã hoàn thiện đầy đủ bản Mobile Web PWA.*  
> *Chi tiết toàn bộ kiến trúc trạm cảm ứng Kiosk xem tại: [`kiosk/README.md`](./kiosk/README.md)*

| Phân hệ | Thư mục | Số lượng màn hình | Mô tả nghiệp vụ cốt lõi |
|---|---|:---:|---|
| **K01** | [`kiosk/1-bat-dau-va-xac-thuc-tai-khoan/`](./kiosk/1-bat-dau-va-xac-thuc-tai-khoan/README.md) | 9 | Chọn hình thức phục vụ (Dine In / Take Away), đăng nhập SĐT/Email, bàn phím số cảm ứng, xác thực OTP 6 số, đăng ký thành viên mới và bổ sung thông tin hồ sơ. |
| **K02** | [`kiosk/2-tai-khoan-lich-su-va-dang-xuat/`](./kiosk/2-tai-khoan-lich-su-va-dang-xuat/README.md) | 5 | Hồ sơ David Nguyen, thẻ Silver (120 pts), thanh tiến trình 6 bậc, chỉnh sửa hồ sơ qua bàn phím ảo QWERTY, lịch sử đơn hàng (Active/Past) và popup xác nhận đăng xuất an toàn. |
| **K03** | [`kiosk/3-nhanh-ho-tro-thieu-diem-loyalty/`](./kiosk/3-nhanh-ho-tro-thieu-diem-loyalty/README.md) | 3 | Banner thiếu điểm ("Missing Your Points?"), hộp thư Messages & News, giải pháp hiển thị **Mã QR quét trên điện thoại cá nhân** để điền form khiếu nại đối soát hóa đơn cũ. |
| **K04** | [`kiosk/4-kho-doi-diem/`](./kiosk/4-kho-doi-diem/README.md) | 3 | Ví `My Rewards` (Voucher 100 NOK, -30%, Quà cốc tumbler), kho đổi điểm `Get More Rewards` (đổi thưởng 100 pts), và trạng thái áp dụng thành công (`In Use` + Toast). |
| **K05** | [`kiosk/5-dat-mon-gio-hang-my-offers/`](./kiosk/5-dat-mon-gio-hang-my-offers/README.md) | 5 | Thực đơn chia đôi (Menu bên trái, Giỏ hàng bên phải), tùy chọn khung giờ (Lunch/Dinner/Holiday), modal `My Offers` áp dụng song song cả Voucher (-30%) và Quà hiện vật (Cốc giữ nhiệt). |
| **K06** | [`kiosk/6-khach-da-dang-nhap-chekout-va-ket-thuc-phien/`](./kiosk/6-khach-da-dang-nhap-chekout-va-ket-thuc-phien/README.md) | 3 | Tiến trình 3 bước (Menu -> Tips -> Payment), thanh trượt tiền tip hình trái tim (`❤️`), số thứ tự lớn (`#768 - NHAT`), nhận hóa đơn điện tử E-Receipt, và modal bảo vệ tài khoản khi đặt đơn mới. |
| **K07** | [`kiosk/7-khach-hang-chua-dang-nhap-xac-thuc-de-tich-diem/`](./kiosk/7-khach-hang-chua-dang-nhap-xac-thuc-de-tich-diem/README.md) | 4 | Luồng thanh toán 4 bước cho khách vãng lai (Menu -> Tips -> Info -> Payment), nhập SĐT trên bàn phím ảo để nhận `+150 points`, xác thực OTP tại chỗ hoặc bỏ qua (`Skip`). |

---

## 🔗 Mối liên kết giữa Prototype và Thiết kế API (API Design Mapping)

Tất cả các API được phát hiện từ quá trình rà soát prototype đã được quy chuẩn hóa và đồng bộ 100% vào thư mục [`docs/designs/api-design/`](../designs/api-design/):
- **Cửa hàng & Chi nhánh:** [`01-tenant-and-store-api.md`](../designs/api-design/01-tenant-and-store-api.md)
- **Thực đơn & Danh mục:** [`02-catalog-and-menu-api.md`](../designs/api-design/02-catalog-and-menu-api.md)
- **Xác thực & Danh tính:** [`03-auth-and-identity-api.md`](../designs/api-design/03-auth-and-identity-api.md)
- **Hội viên & 6 Hạng thẻ:** [`04-loyalty-and-tier-api.md`](../designs/api-design/04-loyalty-and-tier-api.md)
- **Voucher, Quà tặng & Đổi thưởng:** [`05-reward-and-voucher-api.md`](../designs/api-design/05-reward-and-voucher-api.md)
- **Giỏ hàng, Đơn hàng & E-Receipt:** [`06-order-and-checkout-api.md`](../designs/api-design/06-order-and-checkout-api.md)
- **Thiết bị quầy POS/Kiosk:** [`07-kiosk-pos-api.md`](../designs/api-design/07-kiosk-pos-api.md)
- **Thông báo & Khiếu nại điểm:** [`08-notification-api.md`](../designs/api-design/08-notification-api.md)
