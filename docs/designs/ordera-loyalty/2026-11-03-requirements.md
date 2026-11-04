Started at:  2026/11/03 23:30:00
Finished at: 2026/11/03 23:34:00
Total time: 4 minutes
---

# Requirements: Ordera Loyalty Program 2.0

## Source
- **Vision:** [2026-11-03-vision.md](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-vision.md)
- **Figma Design Reference:** `Ordera Loyalty Program 2.0 (Copy) (Copy)` (Key: `m9HCuIqhJinebNk4dlxRSb`)

---

## 1. Functional Requirements (Yêu cầu chức năng)

### 1.1 Use Cases theo từng phân hệ (8 Phân hệ Web Mobile + Kiosk)

| Mã UC | Phân hệ / Tác nhân | Hành động (Action) | Phản hồi hệ thống (System Response) |
|---|---|---|---|
| **UC-STORE-01** | Khách hàng | Khám phá danh sách cửa hàng & chọn Takeaway / Delivery | Lọc danh sách cửa hàng theo vị trí, hiển thị menu thực đơn tương ứng với cửa hàng được chọn |
| **UC-AUTH-01** | Khách hàng | Đăng nhập bằng SĐT / Email | Gửi mã OTP (6 số, hiệu lực 3 phút), hiển thị màn hình đếm ngược nhập OTP |
| **UC-AUTH-02** | Khách hàng | Xác thực mã OTP | Kiểm tra OTP. Nếu chính xác: sinh JWT Access/Refresh Token và tải thông tin phiên người dùng |
| **UC-AUTH-03** | Khách mới | Đăng ký thành viên lần đầu (Họ tên, SĐT, Email, Ngày sinh) | Khởi tạo Profile với hạng mặc định **New Member**, tặng điểm chào mừng (Welcome Points) nếu có |
| **UC-TIER-01** | Thành viên | Xem tổng quan hồ sơ & hạng thẻ hiện tại | Hiển thị thẻ thành viên với 6 cấp độ: **New, Silver, Gold, Platinum, Diamond, Ruby** kèm thanh tiến trình điểm cần để lên hạng tiếp theo |
| **UC-TIER-02** | Thành viên | Xem lịch sử biến động điểm | Liệt kê chi tiết các giao dịch: Ngày giờ, Số điểm (+/-), Lý do (Tích điểm đơn hàng #X, Đổi quà Y, Thăng hạng) |
| **UC-TIER-03** | Thành viên | Gửi yêu cầu hỗ trợ "Thiếu điểm loyalty" | Nhập mã hóa đơn / tải ảnh biên lai; hệ thống tạo phiếu khiếu nại (Ticket) chuyển về admin duyệt |
| **UC-REWARD-01** | Thành viên | Xem kho ưu đãi của tôi (My Rewards) | Hiển thị danh sách voucher đang sở hữu (mã QR/barcode, hạn sử dụng, điều kiện giảm giá) |
| **UC-REWARD-02** | Thành viên | Đổi điểm lấy quà / voucher (Get More Rewards) | Kiểm tra số dư điểm khả dụng; thực hiện giao dịch trừ điểm và cộng voucher vào ví "My Rewards" (Atomic transaction) |
| **UC-ORDER-01** | Khách hàng | Thêm món vào giỏ hàng & tùy chỉnh (Topping, ghi chú) | Cập nhật giỏ hàng theo thời gian thực (State / LocalStorage / Server Cart) |
| **UC-CHECKOUT-01**| Khách hàng | Áp dụng Voucher / Đổi quà trong Checkout | Kiểm tra điều kiện áp dụng (Giá trị đơn tối thiểu, cửa hàng, ngành hàng, hạn dùng); tính lại tổng tiền thanh toán ngay lập tức |
| **UC-CHECKOUT-02**| Khách hàng | Hoàn tất đặt hàng & Nhận biên nhận điện tử | Lưu đơn hàng, tự động tích điểm theo tỷ lệ hạng thẻ, hiển thị mã đơn và cho phép gửi e-receipt về email |
| **UC-KIOSK-01** | Khách tại quầy | Tích điểm nhanh tại Kiosk bằng SĐT | Nhập SĐT -> Gửi OTP -> Xác thực: Nếu đã có profile thì lấy thông tin, nếu chưa có thì cho phép nhập tên hoặc skip để tích điểm nhanh |
| **UC-KIOSK-02** | Khách tại quầy | Checkout tại Kiosk & Auto-logout | Chọn tip, thanh toán, in hóa đơn; kích hoạt đếm ngược 60s tự động xóa phiên và giỏ hàng về trạng thái ban đầu |

---

### 1.2 Business Rules (Quy tắc nghiệp vụ cốt lõi)

| Mã BR | Quy tắc | Lý do & Ý nghĩa nghiệp vụ |
|---|---|---|
| **BR-TIER-01** | **Cơ chế 6 Hạng thẻ:**<br>- *New*: 0 - 499 điểm (Tích 1% giá trị đơn)<br>- *Silver*: 500 - 1,499 điểm (Tích 1.2%)<br>- *Gold*: 1,500 - 3,999 điểm (Tích 1.5% + Quà sinh nhật)<br>- *Platinum*: 4,000 - 7,999 điểm (Tích 2%)<br>- *Diamond*: 8,000 - 14,999 điểm (Tích 2.5%)<br>- *Ruby*: 15,000+ điểm (Tích 3% + Đặc quyền VIP) | Tạo động lực mạnh mẽ để khách hàng chi tiêu nhiều hơn nhằm tăng cấp độ |
| **BR-POINT-01** | Điểm thưởng có 2 trạng thái: **Khả dụng (Available Points)** và **Điểm tích lũy xét hạng (Tier Lifetime Points)**. Đổi quà chỉ trừ Điểm khả dụng, không làm tụt Hạng đã đạt trong chu kỳ xét hạng | Bảo vệ quyền lợi hạng thẻ của người dùng, tránh trải nghiệm tiêu cực khi đổi quà |
| **BR-VOUCHER-01** | Mỗi đơn hàng được áp dụng tối đa 01 Voucher giảm giá đơn + 01 Voucher quà tặng (Free Item), trừ khi có cấu hình đặc biệt | Chống lạm dụng khuyến mãi làm âm biên lợi nhuận của cửa hàng |
| **BR-LEDGER-01** | Tuyệt đối không cập nhật trực tiếp `user.points = user.points - X`. Mọi biến động phải tạo bản ghi **Points Ledger** (Immutable Audit Trail) | Đảm bảo tính minh bạch, phục vụ kiểm toán tài chính và đối soát dữ liệu |
| **BR-KIOSK-01** | Khách tích điểm tại Kiosk không bắt buộc tạo mật khẩu. Xác thực duy nhất qua OTP một lần. Phiên Kiosk tự hủy sau 60 giây không tương tác | Bảo vệ quyền riêng tư và thông tin cá nhân của khách hàng trên thiết bị công cộng |

---

## 2. Non-Functional Requirements (Yêu cầu phi chức năng - NFRs)

| Danh mục | Yêu cầu kỹ thuật | Chỉ số mục tiêu |
|---|---|---|
| **Hiệu năng (Performance)** | - Thời gian First Contentful Paint (FCP) trên Mobile Web 4G<br>- Thời gian phản hồi API tra cứu điểm & áp mã voucher | - FCP < 1.2 giây<br>- API P95 < 200ms |
| **Khả năng mở rộng (Scalability)** | - Kiến trúc Stateless API sẵn sàng scale ngang<br>- Tách biệt Cache (Redis) cho bảng xếp hạng và phiên OTP | Chịu tải tối thiểu 1,000 CCU đồng thời vào các giờ cao điểm đặt món |
| **Bảo mật (Security)** | - Bảo mật đa tầng JWT Access Token (15m) & Refresh Token (7d)<br>- Rate limit gửi OTP (Tối đa 3 lần / 10 phút / 1 SĐT)<br>- Chống tấn công Race Condition khi đổi thưởng (Database Transaction Locking) | Chuẩn OWASP Top 10, ngăn chặn hoàn toàn gian lận điểm thưởng |
| **Khả năng sẵn sàng (Availability)** | Hệ thống hoạt động liên tục, tự động ghi nhận điểm offline tại Kiosk và đồng bộ lại khi có mạng | Uptime 99.9% trong giờ hoạt động của cửa hàng |
| **Khả năng quan sát (Observability)** | Ghi log có cấu trúc (JSON Format) gắn kèm `correlationId` qua từng luồng giao dịch điểm | Dễ dàng truy vết 100% khiếu nại thiếu điểm của người dùng |
| **Trải nghiệm UX/UI (Usability)** | Giao diện chuẩn Mobile-First, Touch-friendly (vùng bấm tối thiểu 44x44px), màu sắc và typography khớp 100% Figma | Thao tác đổi quà và checkout không quá 3 bước chạm |

---

## 3. Constraints (Ràng buộc hệ thống)

| Loại ràng buộc | Chi tiết ràng buộc | Tác động đến thiết kế & triển khai |
|---|---|---|
| **Frontend Stack** | Bắt buộc sử dụng **React + Vite + TypeScript + Tailwind CSS** kết hợp kiến trúc UI component theo **shadcn/ui** | Cần khởi tạo cấu trúc module sạch (`/components`, `/pages`, `/hooks`, `/services`) |
| **Thiết kế Design System** | Tuân thủ tuyệt đối cấu trúc 8 module của bản vẽ `Ordera Loyalty Program 2.0 (Copy) (Copy)` trên Figma | Đảm bảo đúng tên gọi, icon SVG, mã màu thẻ hạng và layout responsive |
| **Backend & Database** | API RESTful chuẩn mực, Cơ sở dữ liệu quan hệ (PostgreSQL / Relational) hỗ trợ ACID Transaction | Bắt buộc thiết kế lược đồ quan hệ chặt chẽ giữa User, Ledger, Tier và Orders |
| **Môi trường phát triển** | Môi trường Windows, PowerShell, Node.js 22 | Các script build, install phải tương thích tốt với PowerShell |

---

## 4. Domain Skills Referenced
- **`api-design`:** Chuẩn hóa quy tắc đặt tên endpoint `/api/v1/loyalty/*`, chuẩn lỗi RFC 7807, cơ chế xác thực Token.
- **`database-design`:** Quy định chiến lược lưu trữ Points Ledger, UUID v7 cho Order & Transaction ID, cơ chế khóa bản ghi chống Double-spending.
- **`log-processing`:** Quy chuẩn ghi log sự kiện `POINT_EARNED`, `POINT_REDEEMED`, `TIER_UPGRADED`.

