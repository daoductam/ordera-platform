# Tài Liệu Đặc Tả Yêu Cầu Nghiệp Vụ Phần Mềm (SRS)
## Ordera Multi-Tenant Loyalty & Ordering Platform

> **Hệ Thống Quản Trị Khách Hàng Thân Thiết & Đặt Món Đa Thương Hiệu (F&B Omni-channel)**  
> **Phiên bản:** 2.0 Enterprise Business Flow Edition  
> **Phương pháp luận:** Phân tích luồng nghiệp vụ chi tiết (Business Flow Methodology - chuẩn hóa theo CarLLM Business Flow Spec)  
> **Phạm vi:** Mobile Web PWA Khách hàng, Trạm Kiosk POS màn hình chạm tại quầy, Cổng quản trị chi nhánh / thương hiệu (Store & Agency Operations)  
> **Thương hiệu tham chiếu:** Linn Sushi Sentrum (Prinsens gate 10C, 0153 Oslo, Norway)

---

## 1. Tổng Quan Hệ Thống & Bối Cảnh Nghiệp Vụ

Ordera là nền tảng số hóa trải nghiệm khách hàng cho ngành dịch vụ ẩm thực (F&B / Nhà hàng), kết hợp khép kín giữa **Đặt món đa kênh (Mobile Web PWA + Touchscreen Kiosk tại quầy)** và **Động cơ Kế toán Khách hàng thân thiết (Loyalty & Points Ledger)**.

Hệ thống giải quyết 4 bài toán nghiệp vụ cốt lõi:
1. **Trải nghiệm khách hàng không gián đoạn:** Khách có thể tự đặt món nhận tại quầy (Counter Pickup) hoặc tại bàn (Table Dine-in), quét mã QR gắn bàn, tùy chọn linh hoạt kích cỡ/topping/đá/đường và thanh toán nhanh.
2. **Kế toán điểm bất biến chống gian lận:** Điểm tích lũy được quản lý như tài khoản tiền tệ kép (Double-entry Ledger), tự động cập nhật theo tỷ lệ hoàn điểm của 6 hạng thẻ (`New` $\rightarrow$ `Silver` $\rightarrow$ `Gold` $\rightarrow$ `Platinum` $\rightarrow$ `Diamond` $\rightarrow$ `Ruby`).
3. **Kích cầu qua Ví ưu đãi đa dạng:** Quản lý vòng đời voucher giảm giá (theo số tiền cố định hoặc phần trăm) và kho quà tặng hiện vật (Cốc giữ nhiệt Tumbler), đảm bảo chống chi tiêu kép (Double-spending) bằng cơ chế khóa phân tán Redis.
4. **Vận hành tại quầy tinh gọn (Kiosk POS):** Màn hình cảm ứng hỗ trợ khách tích điểm nhanh bằng số điện thoại không cần mật khẩu, tự động reset phiên sau 60 giây không thao tác.

---

## 2. Danh Sách Tác Nhân & Vai Trò Hệ Thống (Actors & Stakeholders)

| STT | Tác nhân (Actor) | Loại tác nhân | Vai trò & Trách nhiệm chính | Điểm chạm giao diện (Touchpoints) |
|:---:|:---|:---|:---|:---|
| **A-01** | **Khách hàng Thành viên (Loyalty Member)** | Khách hàng cuối | Đăng nhập/đăng ký bằng OTP SĐT hoặc Email, xem thẻ thành viên David Nguyen, tích điểm khi ăn uống, thăng hạng, đổi voucher/quà tặng, theo dõi đơn và khiếu nại điểm sót. | Mobile Web PWA (`01.xx` – `08.xx`) |
| **A-02** | **Khách vãng lai (Guest Customer)** | Khách hàng cuối | Khám phá thực đơn, quét mã QR bàn, đặt món không cần đăng ký tài khoản, có thể nhập SĐT để tích lũy điểm tạm thời. | Mobile Web PWA, Kiosk POS |
| **A-03** | **Khách tại quầy (Kiosk Shopper)** | Khách hàng cuối | Thao tác trên màn hình chạm cảm ứng lớn tại quầy thu ngân, nhập SĐT tích điểm nhanh cho đơn giấy, hoặc tự đặt món và quẹt thẻ thanh toán trực tiếp. | Màn hình chạm Kiosk POS 21-inch |
| **A-04** | **Nhân viên Quầy / Thu ngân (Cashier / Barista)** | Vận hành cửa hàng | Tiếp nhận đơn gọi tại quầy, kiểm tra và bàn giao quà hiện vật (Cốc Tumbler) khi khách xuất trình mã đổi quà, hỗ trợ in lại hóa đơn. | POS Counter Terminal, Web Portal |
| **A-05** | **Nhân viên Bếp / Bar (Kitchen Staff)** | Vận hành cửa hàng | Tiếp nhận danh sách món cần làm (KDS - Kitchen Display System), chuyển trạng thái món: `PREPARING` $\rightarrow$ `READY`. | Màn hình KDS Bếp |
| **A-06** | **Quản lý Chi nhánh (Store Manager)** | Quản lý vận hành | Quản lý danh mục bàn ăn, cập nhật trạng thái hết món (Out of stock), đối soát hóa đơn giấy và phê duyệt khiếu nại điểm sót (Missing points claim). | Web Portal Quản lý Chi nhánh |
| **A-07** | **Quản trị viên Thương hiệu (Tenant Admin)** | Quản trị thương hiệu | Cấu hình tỷ lệ tích điểm 6 hạng thẻ, tạo chiến dịch Voucher, quản lý kho quà Tumbler, xem báo cáo doanh thu và giao dịch điểm toàn chuỗi. | Web Portal Quản trị Thương hiệu |
| **A-08** | **Hệ thống Tự động (System Daemon / Scheduler)** | Thành phần tự động | Hủy phiên Kiosk sau 60s idle, thu hồi voucher hết hạn, quét hết hạn OTP sau 120s, gửi email thông báo kết quả duyệt điểm. | Spring Boot Background Cron Jobs |

---

## 3. Ma Trận Phân Quyền Vai Trò (Permission Matrix)

Ký hiệu: **C** (Create), **R** (Read), **U** (Update), **D** (Delete), **X** (Execute / Trigger), **-** (Không có quyền).

| Phân hệ Nghiệp vụ | Khách vãng lai | Khách Thành viên | Nhân viên Quầy / Bếp | Quản lý Chi nhánh | Quản trị Thương hiệu |
|:---|:---:|:---:|:---:|:---:|:---:|
| **Xem Thực đơn & Chi nhánh** | R | R | R | R | R |
| **Đặt món & Thanh toán** | C, R | C, R | C, R, U (Thay khách) | R, U (Hủy đơn) | R (Báo cáo) |
| **Xem Hồ sơ & Điểm thưởng** | - | R | - | R (Tra cứu SĐT) | R (Toàn chuỗi) |
| **Đổi Voucher & Quà tặng** | - | C, R (Dùng điểm đổi) | R, U (Xác nhận trao quà) | R, U (Điều chỉnh kho quà) | C, R, U, D (Tạo voucher) |
| **Gửi Khiếu nại Thiếu điểm** | - | C, R | - | R, U (Duyệt/Từ chối) | R (Xem lịch sử) |
| **Cấu hình Bàn & Mã QR** | - | - | R | C, R, U | C, R, U, D |
| **Điều chỉnh Bậc thẻ & Điểm tay** | - | - | - | - | C, R, U (Ghi sổ cái Admin) |
| **Kích hoạt / Reset Kiosk POS** | X (Thao tác chạm) | X (Đăng nhập SĐT) | X (Reset thiết bị) | U (Cấu hình trạm) | C, R, U, D |

---

## 4. Danh Sách Luồng Nghiệp Vụ Cốt Lõi (Master Business Flows)

Mỗi luồng nghiệp vụ được phân tích chi tiết trong tài liệu chuyên biệt:

| Mã Luồng | Tên Luồng Nghiệp Vụ | Mục Tiêu Nghiệp Vụ | Trigger (Sự kiện kích hoạt) | Tác Nhân Chính | File Tài Liệu Chi Tiết |
|:---:|:---|:---|:---|:---|:---:|
| **BF-01** | **Onboarding, Khám Phá Cửa Hàng & Xác Thực OTP** | Khách truy cập web, tìm kiếm cửa hàng gần nhất hoặc quét QR bàn, xác thực OTP và định danh tài khoản. | Khách mở PWA hoặc quét QR code tại bàn | Member, Guest | [`bf-01-onboarding-va-dinh-danh.md`](./bf-01-onboarding-va-dinh-danh.md) |
| **BF-02** | **Khám Phá Thực Đơn, Tùy Biến Topping & Giỏ Hàng** | Khách xem món ăn theo danh mục, chọn kích cỡ, đá/đường, thêm topping và quản lý giỏ hàng tạm. | Khách bấm chọn món ăn hoặc xem giỏ hàng | Member, Guest | [`bf-02-thuc-don-va-tuy-chon-mon.md`](./bf-02-thuc-don-va-tuy-chon-mon.md) |
| **BF-03** | **Quy Trình Đặt Món, Áp Mã Voucher & Thanh Toán Checkout** | Tính thuế VAT 15%, chọn hình thức nhận (Quầy/Bàn), chọn tiền tip, khóa voucher và thanh toán. | Khách bấm nút "Pay" tại màn hình Checkout | Member, Cashier | [`bf-03-dat-mon-va-thanh-toan.md`](./bf-03-dat-mon-va-thanh-toan.md) |
| **BF-04** | **Tích Lũy Điểm Bất Biến & Tự Động Thăng Tiến 6 Cấp Bậc** | Tự động ghi sổ cái `points_ledger`, cộng điểm theo tỷ lệ hạng thẻ, kiểm tra mốc và nâng bậc thẻ thành viên. | Đơn hàng chuyển trạng thái `PAID` / `COMPLETED` | System Daemon, Member | [`bf-04-tich-luy-diem-va-thang-hang.md`](./bf-04-tich-luy-diem-va-thang-hang.md) |
| **BF-05** | **Đổi Thưởng Điểm (Voucher / Quà Tumbler) & Quản Lý Ví** | Khách dùng điểm tích lũy để đổi mã giảm giá hoặc đổi cốc Tumbler vật lý, nhận quà tại quầy. | Khách bấm "Get now" trong mục Rewards | Member, Cashier | [`bf-05-doi-thuong-va-quan-ly-vi.md`](./bf-05-doi-thuong-va-quan-ly-vi.md) |
| **BF-06** | **Kiosk POS Tự Phục Vụ & Tích Điểm Nhanh Tại Quầy** | Khách nhập SĐT trên màn hình cảm ứng Kiosk để nhận điểm đơn giấy hoặc tự gọi món quẹt thẻ. | Khách chạm vào màn hình Kiosk | Kiosk Shopper, Cashier | [`bf-06-kiosk-pos-tu-phuc-vu.md`](./bf-06-kiosk-pos-tu-phuc-vu.md) |
| **BF-07** | **Khiếu Nại Thiếu Điểm & Chăm Sóc Khách Hàng** | Khách gửi ảnh bill giấy khi không thấy điểm, Quản lý chi nhánh đối soát và duyệt cộng bù điểm vào sổ cái. | Khách gửi form Missing Points Claim | Member, Store Manager | [`bf-07-khieu-nai-thieu-diem.md`](./bf-07-khieu-nai-thieu-diem.md) |
| **BF-08** | **Vận Hành Bếp, Quản Trị Cửa Hàng & Báo Cáo** | Quản lý chuyển trạng thái món ăn (hết hàng), nhân viên bếp nhận đơn chế biến và xác nhận trả quà. | Đơn mới xuất hiện trên KDS bếp | Kitchen Staff, Manager | [`bf-08-van-hanh-bep-va-quan-tri.md`](./bf-08-van-hanh-bep-va-quan-tri.md) |

---

## 5. Danh Sách Tính Năng Hệ Thống (Feature Breakdown)

| Mã Tính Năng | Phân Hệ | Tên Tính Năng | Mô Tả Nghiệp Vụ Ngắn Gọn | Mức Ưu Tiên |
|:---|:---|:---|:---|:---:|
| `STR-01` | Store | Định vị & Danh sách chi nhánh | Hiển thị khoảng cách GPS, địa chỉ, giờ mở cửa và hình thức nhận món | P1 (Bắt buộc) |
| `STR-02` | Store | Quét mã QR Bàn ăn | Giải mã QR token, tự động gắn số bàn (`Table 05`) vào giỏ hàng | P1 (Bắt buộc) |
| `AUTH-01`| Auth | Gửi mã OTP xác thực (SMS/Email) | Sinh OTP ngẫu nhiên 6 số, giới hạn cooldown 60s, TTL 120s trên Redis | P1 (Bắt buộc) |
| `AUTH-02`| Auth | Kiểm tra OTP & Chống brute-force | Kiểm tra khớp mã, khóa phiên 15 phút nếu nhập sai quá 5 lần | P1 (Bắt buộc) |
| `AUTH-03`| Auth | Đăng ký & Hoàn thiện hồ sơ | Nhập họ tên, SĐT, Email, ngày sinh (sinh nhật tặng quà), giới tính | P1 (Bắt buộc) |
| `AUTH-04`| Auth | Cấp phát & Làm mới JWT | Access Token 15 phút, Refresh Token 7 ngày, Blacklist Token khi logout | P1 (Bắt buộc) |
| `CAT-01` | Catalog | Thực đơn phân cấp | Danh mục món trượt ngang, danh sách món kèm ảnh, giá, mô tả | P1 (Bắt buộc) |
| `CAT-02` | Catalog | Nhóm tùy biến Modifier | Radio button chọn size (M/L), checkbox chọn topping (Trân châu, Popping) | P1 (Bắt buộc) |
| `ORD-01` | Order | Quản lý Giỏ hàng Động | Thêm món, tăng giảm số lượng, lưu tạm localStorage và đồng bộ máy chủ | P1 (Bắt buộc) |
| `ORD-02` | Order | Tính toán thuế VAT & Tips | Tự động tính VAT 15% Na Uy (in-price), cộng tiền tip (5/10/20 NOK) | P1 (Bắt buộc) |
| `ORD-03` | Order | Khóa Voucher chống chi tiêu kép | Tạm khóa voucher sang `IN_USE` qua Redis lock khi mở checkout | P1 (Bắt buộc) |
| `ORD-04` | Order | Tạo đơn hàng & Thanh toán | Ghi nhận đơn hàng `orders`, xuất số thứ tự `#A-08`, gửi lệnh chế biến | P1 (Bắt buộc) |
| `ORD-05` | Order | Hóa đơn điện tử E-Receipt | Hiển thị chi tiết thanh toán, VAT, điểm tích lũy và mã vạch biên nhận | P1 (Bắt buộc) |
| `LOY-01` | Loyalty | Sổ cái Điểm Bất Biến | Ghi nhận mọi giao dịch điểm vào `points_ledger`, nghiêm cấm lệnh UPDATE | P1 (Bắt buộc) |
| `LOY-02` | Loyalty | Động cơ 6 Cấp bậc Thành viên | Tự động tính toán tiến trình thăng hạng (`New` $\rightarrow$ `Ruby`) theo điểm trọn đời | P1 (Bắt buộc) |
| `REW-01` | Reward | Ví ưu đãi My Rewards | Hiển thị voucher khả dụng, voucher đang dùng và voucher đã hết hạn | P1 (Bắt buộc) |
| `REW-02` | Reward | Đổi Voucher bằng Điểm | Trừ điểm khả dụng, cấp voucher mới vào ví thành viên | P1 (Bắt buộc) |
| `REW-03` | Reward | Đổi Quà hiện vật Cốc Tumbler | Trừ điểm, trừ tồn kho `stock_quantity`, sinh mã nhận quà 8 ký tự | P1 (Bắt buộc) |
| `REW-04` | Reward | Xác nhận trao quà tại quầy | Thu ngân quét mã, kiểm tra tính hợp lệ và xác nhận trao cốc | P2 (Quan trọng) |
| `KSK-01` | Kiosk | Bàn phím chạm nhập SĐT | Giao diện số lớn, nhận diện thành viên tức thì bằng số điện thoại | P1 (Bắt buộc) |
| `KSK-02` | Kiosk | Tích điểm nhanh tại quầy | Nhập tiền bill giấy, tích điểm ngay theo tỷ lệ hạng thẻ không cần pass | P1 (Bắt buộc) |
| `KSK-03` | Kiosk | Tự động hủy phiên 60s idle | Đếm ngược 60 giây không chạm, tự động xóa phiên đưa về màn hình chào | P1 (Bắt buộc) |
| `CLM-01` | Claim | Gửi yêu cầu khiếu nại điểm | Nhập mã hóa đơn giấy, ngày mua, số tiền và đính kèm ảnh chụp bill | P2 (Quan trọng) |
| `CLM-02` | Claim | Phê duyệt & Cộng bù điểm | Quản lý đối soát bill, duyệt cộng điểm bù vào sổ cái kèm lý do | P2 (Quan trọng) |
| `NTF-01` | Notif | Hộp thư 4 Danh mục | Phân loại thông báo: Messages, News, Voucher, Loyalty Gifts | P2 (Quan trọng) |

---

## 6. Ma Trận Truy Vết Nghiệp Vụ (Requirements Traceability Matrix - RTM)

Bảng đối chiếu đảm bảo mọi Luồng Nghiệp Vụ (BF) đều được hiện thực hóa qua các Tính năng cụ thể và ngược lại không có tính năng thừa:

| Mã Tính Năng | BF-01 (Onboarding) | BF-02 (Catalog) | BF-03 (Checkout) | BF-04 (Loyalty) | BF-05 (Reward) | BF-06 (Kiosk) | BF-07 (Claim) | BF-08 (Bếp/Ops) |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `STR-01` | ● | | | | | | | |
| `STR-02` | ● | | ● | | | | | |
| `AUTH-01`| ● | | | | | | | |
| `AUTH-02`| ● | | | | | | | |
| `AUTH-03`| ● | | | | | | | |
| `AUTH-04`| ● | | ● | | ● | | | |
| `CAT-01` | | ● | | | | | | ● |
| `CAT-02` | | ● | ● | | | | | |
| `ORD-01` | | ● | ● | | | | | |
| `ORD-02` | | | ● | | | ● | | |
| `ORD-03` | | | ● | | ● | | | |
| `ORD-04` | | | ● | ● | | ● | | ● |
| `ORD-05` | | | ● | | | ● | | |
| `LOY-01` | | | | ● | ● | ● | ● | |
| `LOY-02` | ● | | | ● | | ● | | |
| `REW-01` | | | ● | | ● | | | |
| `REW-02` | | | | | ● | | | |
| `REW-03` | | | | | ● | | | |
| `REW-04` | | | | | ● | | | ● |
| `KSK-01` | | | | | | ● | | |
| `KSK-02` | | | | ● | | ● | | |
| `KSK-03` | | | | | | ● | | |
| `CLM-01` | | | | | | | ● | |
| `CLM-02` | | | | ● | | | ● | ● |
| `NTF-01` | ● | | | ● | ● | | ● | |

---

## 7. Quy Tắc Nghiệp Vụ Toàn Cục (Global Business Rules)

- **BR-01 (Multi-Tenant Scoping):** Mọi thao tác đọc ghi dữ liệu đều phải ràng buộc chặt chẽ theo `tenant_id`. Nghiêm cấm tuyệt đối việc để lộ dữ liệu chéo thương hiệu (RLS Block).
- **BR-02 (Quy tắc Tính Thuế VAT Na Uy):** Thuế giá trị gia tăng áp dụng cho đồ ăn thức uống mang về/nhà hàng tại Na Uy là **15.00%**. Giá niêm yết trên menu đã bao gồm VAT. Thuế được tính ngược theo công thức:
  $$\text{VAT} = \text{Total} \times \frac{15}{115}$$
- **BR-03 (Bất Biến Điểm Thưởng):** Không bao giờ dùng lệnh UPDATE cột `current_points`. Mọi biến động đều phải ghi nhận bằng bản ghi mới trong `points_ledger` kèm `transaction_type` và `reference_id`.
- **BR-04 (Chống Chi Tiêu Kép Voucher):** Một voucher khi được chọn áp dụng vào đơn hàng sẽ chuyển sang trạng thái `IN_USE` và bị khóa bằng Redis Lock trong tối đa 15 phút. Nếu khách hủy đơn hoặc quá thời hạn thanh toán, voucher sẽ tự động mở lại trạng thái `AVAILABLE`.
- **BR-05 (Thang Bậc Thành Viên & Tỷ Lệ Hoàn Điểm):**
  1. `New Member`: 0 - 199 pts $\rightarrow$ Hoàn điểm 1.0%
  2. `Silver Member`: 200 - 399 pts $\rightarrow$ Hoàn điểm 1.2%
  3. `Gold Member`: 400 - 999 pts $\rightarrow$ Hoàn điểm 1.5%
  4. `Platinum Member`: 1,000 - 1,999 pts $\rightarrow$ Hoàn điểm 1.8%
  5. `Diamond Member`: 2,000 - 3,999 pts $\rightarrow$ Hoàn điểm 2.0%
  6. `Ruby Member`: $\ge$ 4,000 pts $\rightarrow$ Hoàn điểm 2.5%
- **BR-06 (An Toàn Phiên Kiosk):** Khi trạm Kiosk không nhận được tương tác chạm của khách hàng trong 60 giây, phiên làm việc phải tự động bị hủy và xóa trắng toàn bộ dữ liệu tạm (Start Over) để bảo mật thông tin khách hàng trước.
