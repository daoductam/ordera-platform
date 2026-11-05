# Software Requirements Specification (SRS)
## Ordera Multi-Tenant Loyalty & Ordering Platform (Figma 2.0 Edition)

> **Tài liệu Đặc tả Yêu cầu Phần mềm (SRS - Software Requirements Specification)**  
> **Phiên bản:** 2.0 (Khớp 100% Figma Design `Ordera Loyalty Program 2.0 (Copy) (Copy)`)  
> **Chuẩn áp dụng:** IEEE Std 830-1998 / ISO/IEC/IEEE 29148  
> **Đối tượng:** Toàn bộ Đội ngũ Phát triển (Frontend, Backend, QA/QC, AI Agents)  
> **Nguyên tắc bất biến:** Tài liệu này là **"Kim chỉ nam tối cao (Single Source of Truth)"**. Mọi dòng code Frontend, Backend API, Use Case và Database Queries đều phải đối chiếu với SRS này để đảm bảo **tuyệt đối không bị chệch hướng**.

---

## 1. Tổng quan Dự án & Phạm vi (System Overview & Scope)

### 1.1 Mục tiêu Hệ thống (Business Goals)
Ordera là nền tảng quản trị quan hệ khách hàng thân thiết và đặt món đa thương hiệu (Multi-Tenant Loyalty & Ordering SaaS) phục vụ chuỗi nhà hàng / đại lý F&B (điển hình: *Linn Sushi Sentrum, Aby Tea*). Hệ thống cung cấp trải nghiệm số khép kín:
1. **Khách hàng (Customer Mobile Web PWA):** Tự đặt món (nhận tại quầy hoặc tại bàn), tích lũy điểm thưởng theo 6 hạng thẻ lũy tiến, đổi voucher giảm giá, nhận quà hiện vật (cốc tumbler) và thanh toán minh bạch (VAT, Tips, E-Receipt).
2. **Kiosk tại quầy (In-Store Kiosk POS):** Màn hình cảm ứng toàn màn hình giúp khách tích điểm siêu tốc qua SĐT, tip và tự động xóa phiên bảo mật sau 60 giây.
3. **Thương hiệu / Đại lý (Merchant Multi-Tenancy):** Phân lập dữ liệu 100% theo `tenant_id`, tùy biến mã màu nhận diện thương hiệu, menu, chính sách tích điểm và kho voucher độc lập.

### 1.2 Các Tác nhân Hệ thống (System Actors)
- **ACT-01 (Khách Vãng Lai - Guest User):** Khách chưa đăng nhập; có thể duyệt menu, xem sản phẩm, cấu hình món ăn. Khi thanh toán hoặc xem ví thưởng, hệ thống yêu cầu xác thực SĐT/Email.
- **ACT-02 (Thành viên - Member / Customer):** Khách hàng đã đăng nhập xác thực OTP; sở hữu thẻ thành viên, ví My Rewards, điểm khả dụng, điểm tích lũy trọn đời và lịch sử đơn hàng.
- **ACT-03 (Khách Kiosk - Kiosk Customer):** Khách tại quầy thao tác trên màn hình POS; nhập SĐT để tích điểm đơn hàng và chọn tips.
- **ACT-04 (Nhân viên Quầy - Store Staff):** Tiếp nhận đơn hàng, chuẩn bị món, bàn giao quà tặng hiện vật khi khách xuất trình mã đổi quà `redemption_code`.
- **ACT-05 (Quản trị viên Đại lý - Tenant Admin):** Quản lý thực đơn, cấu hình 6 bậc thẻ, phát hành voucher, duyệt khiếu nại thiếu điểm hóa đơn.

---

## 2. Đặc tả Chi tiết 8 Nhóm Màn hình Figma (Screen-by-Screen Specification)

Mỗi màn hình được định nghĩa chặt chẽ gồm: **Mã màn hình Figma**, **Mục đích**, **Các thành phần UI**, **Quy tắc kiểm tra (Validation)**, **Hành vi nghiệp vụ (Behavior)**, **API Endpoints tương ứng**, và **Trạng thái Ngoại lệ (Edge Cases)**.

---

### PHÂN HỆ 01: KHÁM PHÁ & CHỌN CỬA HÀNG (STORE DISCOVERY & MENU)

#### Màn hình 01.01 — Danh sách Cửa hàng & Tìm kiếm (Store Discovery)
- **Figma Frame:** `01.01-store-list`
- **Mục đích:** Cho phép khách hàng tìm kiếm và chọn chi nhánh nhà hàng gần nhất để bắt đầu đặt món.
- **Thành phần UI:**
  - Ô tìm kiếm chi nhánh (Search Input theo tên cửa hàng, địa chỉ, thành phố).
  - Bộ lọc hình thức phục vụ: `Counter Pickup` (Lấy tại quầy) / `Table Dine-in` (Ăn tại bàn).
  - Danh sách thẻ cửa hàng (Store Cards): Ảnh đại diện, Tên chi nhánh (*Linn Sushi Sentrum*), Địa chỉ (*Prinsens gate 10C, 0153 Oslo*), Khoảng cách (km tính theo GPS), Giờ mở cửa (*10:00 - 22:00*), Trạng thái (*Open now / Closed*).
- **Quy tắc Nghiệp vụ & Validation:**
  - Nếu chi nhánh có `is_active = false`, hiển thị nhãn "Closed" màu xám, cấm bấm vào đặt món ngay.
  - Khi khách chọn chi nhánh, lưu `currentStoreId` vào LocalStorage / Global State.
- **API Endpoint:** `GET /api/v1/stores?city={city}&activeOnly=true`

#### Màn hình 01.02 — Thực đơn Chi nhánh theo Danh mục (Store Menu & Categories)
- **Figma Frame:** `01.02-store-menu`
- **Mục đích:** Hiển thị danh mục món ăn (Categories) và danh sách món (Products) của chi nhánh được chọn.
- **Thành phần UI:**
  - Thanh cuộn ngang danh mục (Horizontal Category Tabs): *Sushi Rolls, Boba Tea, Sides, Desserts*. Tab đang chọn có thanh gạch chân màu thương hiệu.
  - Lưới danh sách món ăn (Product Cards):
    - Ảnh món ăn (`image_url`).
    - Tên món ăn (`name`: *Aurora, Tuna Maki, Passionfruit Tea*).
    - Mô tả tóm tắt nguyên liệu/hương vị (`description`).
    - Giá bán tiêu chuẩn (`base_price`: *89.00 Kr*).
    - Nút `+ Add` hoặc nút mở tuỳ chọn.
    - Nhãn `Sold Out` nếu `is_available = false`.
- **API Endpoint:** `GET /api/v1/stores/{storeId}/categories`, `GET /api/v1/stores/{storeId}/products`

#### Màn hình 01.03 — Chi tiết Món ăn & Nhóm Tùy chọn (Product Detail & Modifiers)
- **Figma Frame:** `01.03-product-detail-modal`
- **Mục đích:** Tùy biến món ăn trước khi thêm vào giỏ hàng (chọn Size, Topping, Lượng đường/đá).
- **Thành phần UI:**
  - Ảnh lớn món ăn, tên món, đơn giá cơ sở.
  - Các khối nhóm tùy chọn (Modifier Groups):
    - *Size (Bắt buộc chọn 1):* Regular (0 Kr), Large (+15 Kr) -> Kiểu hiển thị: Radio Button.
    - *Toppings (Tùy chọn, tối đa 3):* Mango Popping (+10 Kr), Tapioca Pearl (+8 Kr), Cheese Foam (+12 Kr) -> Kiểu hiển thị: Checkbox.
    - *Sweetness / Ice (Nếu là đồ uống):* 100%, 70%, 50%, 0% đá/ngọt.
  - Ô nhập ghi chú đặc biệt (`special_instruction`: tối đa 200 ký tự, vd: "Không hành, ít gừng").
  - Bộ đếm số lượng món: `[-] 1 [+]`.
  - Nút Submit: `Add to Cart — {totalItemPrice} Kr`.
- **Validation Rules:**
  - Nhóm có `min_selection = 1`: Bắt buộc phải chọn đủ 1 option mới mở khóa nút "Add to Cart".
  - Nhóm có `max_selection = N`: Không cho phép chọn quá N option trong nhóm.
  - Công thức tính giá dòng món:
    $$\text{itemTotal} = (\text{basePrice} + \sum \text{modifierExtraPrices}) \times \text{quantity}$$
- **API Endpoint:** `GET /api/v1/products/{productId}/modifier-groups`

---

### PHÂN HỆ 02: XÁC THỰC & ĐĂNG NHẬP (AUTHENTICATION & OTP)

#### Màn hình 02.01 — Nhập Số Điện Thoại / Email Đăng nhập
- **Figma Frame:** `02.01-auth-phone-input`
- **Thành phần UI:**
  - Ô chọn mã quốc gia (mặc định Na Uy `+47`).
  - Ô nhập số điện thoại (ví dụ: `973 62 748`).
  - Nút chuyển đổi: "Use Email instead".
  - Nút chính: `Send Code (OTP)`.
- **Validation Rules:**
  - SĐT phải chuẩn hóa theo chuẩn quốc tế E.164 (độ dài 8 - 15 ký tự số).
  - Rate Limiting: Tối đa 3 lần yêu cầu OTP trong 10 phút trên cùng 1 SĐT/IP (HTTP 429 nếu vi phạm).
- **API Endpoint:** `POST /api/v1/auth/otp/send`
  - Request: `{"channel": "SMS", "recipient": "+4797362748", "purpose": "AUTH"}`

#### Màn hình 02.02 — Nhập Mã OTP Xác thực
- **Figma Frame:** `02.02-auth-otp-verify`
- **Thành phần UI:**
  - 6 ô nhập mã số OTP (tự động nhảy focus sang ô tiếp theo khi gõ).
  - Đồng hồ đếm ngược hiệu lực: `Resend code in 01:59` (120 giây).
  - Nút "Resend Code" (chỉ kích hoạt khi đếm ngược về 00:00).
- **Validation & Business Logic:**
  - Mã OTP gồm 6 chữ số ngẫu nhiên lưu trong Redis với TTL 120 giây, mã hóa băm SHA-256 trong audit log.
  - Khách nhập sai quá 5 lần: Khóa phiên OTP ngay lập tức và hủy mã trong Redis.
  - Nếu xác thực thành công:
    - Nếu tài khoản đã tồn tại: Sinh cặp JWT `accessToken` (15 phút) & `refreshToken` (7 ngày) -> Điều hướng về màn hình trước đó.
    - Nếu tài khoản chưa từng đăng ký: Điều hướng tới **Màn hình 03.01 (Đăng ký thành viên)**.
- **API Endpoint:** `POST /api/v1/auth/otp/verify`
  - Request: `{"recipient": "+4797362748", "code": "849201"}`
  - Response: `{"accessToken": "ey...", "refreshToken": "ey...", "isNewUser": false, "user": {...}}`

---

### PHÂN HỆ 03: ĐĂNG KÝ THÀNH VIÊN (MEMBER REGISTRATION)

#### Màn hình 03.01 — Form Đăng ký Thông tin Thành viên Mới
- **Figma Frame:** `03.01-register-profile`
- **Thành phần UI:**
  - Họ và tên (`full_name`: Bắt buộc, tối thiểu 2 từ, tối đa 128 ký tự, vd: *David Nguyen*).
  - Địa chỉ Email (`email`: Bắt buộc, chuẩn regex email RFC 5322).
  - Ngày tháng năm sinh (`date_of_birth`: Picker ngày/tháng/năm, dùng để tặng quà sinh nhật).
  - Giới tính (`gender`: *Male / Female / Other* - Tùy chọn).
  - Checkbox chấp thuận Điều khoản dịch vụ & Chính sách bảo mật (Bắt buộc tích chọn).
  - Nút: `Complete Registration`.
- **Business Logic:**
  - Tạo bản ghi mới trong bảng `users` với `role = 'CUSTOMER'`, `is_phone_verified = true`.
  - Tự động tạo bản ghi trong `member_profiles` với:
    - `current_tier_id` trỏ đến Hạng **New Member** (Thứ tự 1).
    - `current_points = 0`, `lifetime_points = 0`.
  - Ghi nhận giao dịch chào mừng nếu đại lý có cấu hình Welcome Bonus.
- **API Endpoint:** `POST /api/v1/auth/register`

---

### PHÂN HỆ 04: HỒ SƠ THÀNH VIÊN & SỔ CÁI ĐIỂM (PROFILE & POINTS LEDGER)

#### Màn hình 04.01 — Chi tiết Hồ sơ & Thẻ Thành viên
- **Figma Frame:** `04.01-profile-overview`
- **Thành phần UI:**
  - Thẻ nhận diện thành viên ảo (Virtual Membership Card):
    - Tên thành viên: *David Nguyen*.
    - Số điện thoại: *+47 973 62 748*.
    - Tên hạng thẻ hiện tại: *Gold Member* (kèm dải màu nền #F5B800).
    - Mã Barcode / QR Code thành viên (chứa chuỗi token định danh để nhân viên quầy quét tích điểm).
  - Ô tóm tắt điểm:
    - **Available Points (Điểm khả dụng):** *120 Points* (Có thể dùng đổi quà).
    - **Lifetime Points (Điểm trọn đời xét hạng):** *300 Points*.
  - Thanh tiến trình thăng hạng (Tier Upgrade Stepper):
    - Hiển thị mốc: Đã đạt 300 / 1,000 điểm để lên hạng **Platinum**.
    - Cần tích thêm: *700 điểm nữa*.
  - Lối tắt: `Points History`, `Edit Profile`, `Missing Points Claim`, `Logout`.
- **API Endpoint:** `GET /api/v1/loyalty/members/me`

#### Màn hình 04.02 — Lịch sử Biến động Điểm (Points History)
- **Figma Frame:** `04.02-points-history`
- **Thành phần UI:**
  - Danh sách dòng biến động điểm sắp xếp theo thời gian mới nhất:
    - Icon loại giao dịch (Cộng điểm: Mũi tên xanh lá `+`; Trừ điểm: Mũi tên đỏ `-`).
    - Diễn giải: *Points earned from Linn Sushi Sentrum*, *Redeem Voucher 100 NOK*, *Tier Upgrade Bonus*.
    - Ngày giờ giao dịch: *05 Nov 2026, 14:32*.
    - Số điểm biến động: `+170 Points` (màu xanh lá) hoặc `-100 Points` (màu xám đậm).
    - Số dư sau giao dịch: `Balance: 290 Points`.
- **Quy tắc Kiểm toán (Ledger Invariant):**
  - Mọi dòng lịch sử phải khớp 100% với dữ liệu từ bảng phân vùng `points_ledger`. Không được phép sai lệch giữa tổng các `points_delta` và `current_points` hiện tại.
- **API Endpoint:** `GET /api/v1/loyalty/members/me/points-history?page=0&size=20`

---

### PHÂN HỆ 05: 6 HẠNG THẺ & KHIẾU NẠI THIẾU ĐIỂM (TIERS & CLAIMS)

#### Màn hình 05.01 — Bảng 6 Cấp bậc Thẻ & Đặc quyền (6-Tier Benefits)
- **Figma Frame:** `05.01-tier-benefits`
- **Thành phần UI:**
  - Bảng cuộn danh sách 6 hạng thẻ theo đúng thiết kế Figma:
    1. **New:** 0 điểm | Tích điểm x1.0 | Mã màu `#1EB759`
    2. **Silver:** 200 điểm | Tích điểm x1.2 | Mã màu `#00A3FF`
    3. **Gold:** 400 điểm | Tích điểm x1.5 + Quà sinh nhật | Mã màu `#F5B800`
    4. **Platinum:** 1,000 điểm | Tích điểm x1.8 + Voucher 100 NOK | Mã màu `#FF5B4D`
    5. **Diamond:** 2,000 điểm | Tích điểm x2.0 + Free Topping | Mã màu `#7C3AED`
    6. **Ruby:** 4,000 điểm | Tích điểm x2.5 + VIP Tumbler Cup | Mã màu `#D9148E`
  - Đánh dấu thẻ khách hàng đang đạt bằng nhãn `Current Tier`.
- **API Endpoint:** `GET /api/v1/loyalty/tiers`

#### Màn hình 05.02 — Gửi Khiếu nại Thiếu Điểm Hóa đơn (Support Claim)
- **Figma Frame:** `05.02-missing-points-claim`
- **Mục đích:** Khách hàng mua tại quầy quên tích điểm có thể gửi yêu cầu để Admin kiểm tra và cộng bù điểm.
- **Thành phần UI:**
  - Ô nhập Mã hóa đơn (`receipt_code`: Bắt buộc, in trên phiếu tính tiền, vd: *RCP-8921-X*).
  - Ô chọn Ngày mua hàng (`order_date`: Bắt buộc, không được vượt quá 30 ngày so với hiện tại).
  - Ô nhập Tổng tiền trên hóa đơn (`bill_amount`: Bắt buộc, định dạng số thực 2 chữ số thập phân).
  - Nút tải lên ảnh chụp hóa đơn (`bill_photo_url`: Định dạng PNG, JPEG, tối đa 5MB).
  - Nút gửi: `Submit Claim`.
  - Danh sách các phiếu khiếu nại đã gửi kèm trạng thái: `PENDING` (Chờ duyệt), `APPROVED` (Đã cộng +X điểm), `REJECTED` (Từ chối).
- **API Endpoints:**
  - `POST /api/v1/loyalty/claims` (Tạo yêu cầu khiếu nại)
  - `GET /api/v1/loyalty/claims/me` (Xem danh sách khiếu nại của tôi)

---

### PHÂN HỆ 06: GIỎ HÀNG, CHECKOUT & E-RECEIPT (CART & ORDERING)

#### Màn hình 06.01 — Giỏ hàng Đặt lại (ReorderCartView)
- **Figma Frame:** `06.01-cart-view`
- **Thành phần UI:**
  - Danh sách các món ăn đã chọn:
    - Tên món (*Aurora, Tuna Maki*).
    - Topping kèm theo (*Large, Mango Popping, Less Ice*).
    - Đơn giá dòng món, bộ đếm số lượng `[-] 2 [+]`.
    - Nút xóa món khỏi giỏ hàng.
  - Khung tóm tắt tiền tạm tính (Subtotal).
  - Nút chuyển sang thanh toán: `Proceed to Checkout`.

#### Màn hình 06.02 — Xác nhận Đơn hàng & Tính tiền (Checkout View)
- **Figma Frame:** `06.02-checkout-details`
- **Thành phần UI:**
  - **Hình thức nhận món:**
    - `Counter Pickup` (Nhận tại quầy)
    - `Table Dine-in` (Nhận tại bàn -> Hiện thêm ô nhập `Table Number`, ví dụ: *Table 05*).
  - **Thời gian lấy đồ:**
    - `ASAP` (Càng sớm càng tốt, ước tính 15 - 20 phút).
    - `Scheduled` (Hẹn giờ cụ thể trong ngày).
  - **Thông tin người nhận:** Tên (*David Nguyen*), SĐT (*+4797362748*).
  - **Khu vực Voucher Giảm giá:**
    - Nếu chưa áp dụng: Nút bấm `Apply Voucher / Reward` (Mở Drawer Nhóm 08).
    - Nếu đã áp dụng: Hiển thị tag màu xanh lá `SAVE100 (-100.00 Kr)` kèm nút bấm `Remove`.
  - **Khu vực Tip cho nhân viên:**
    - Các nút bấm chọn nhanh: `0 Kr`, `5 Kr`, `10 Kr`, `15 Kr`, hoặc ô nhập số tùy ý `Custom`.
  - **Bảng Tính Tiền Chi Tiết (Financial Calculation Table):**
    - Subtotal (Tiền món gốc): `267.00 Kr`
    - Voucher Discount: `-100.00 Kr` (nếu có)
    - Tips: `+10.00 Kr`
    - Thuế VAT đã bao gồm (Included VAT 15%): `34.83 Kr`
    - **Total to Pay (Tổng thanh toán):**
      $$\text{Total} = \max(0, \text{Subtotal} - \text{Discount}) + \text{Tips} = (267 - 100) + 10 = 177.00 \text{ Kr}$$
    - Dự kiến điểm tích lũy: `+170 Points earned`
  - **Phương thức thanh toán:** *Bank Card / Vipps / Apple Pay*.
  - Nút bấm thanh toán: `Pay 177.00 Kr`.
- **API Endpoint:** `POST /api/v1/orders/checkout`

#### Màn hình 06.03 — Hóa đơn Điện tử E-Receipt (Order Completed & Receipt)
- **Figma Frame:** `06.03-e-receipt`
- **Thành phần UI:**
  - Icon thành công (Dấu tích xanh lá to bản).
  - Mã đơn hàng hiển thị: `ORD-20261105-0891`.
  - Mã vạch Barcode và QR Code của đơn hàng để nhân viên quét khi nhận món.
  - Thông tin điểm nhận món: *Linn Sushi Sentrum, Table 05*.
  - Thời gian dự kiến sẵn sàng: *Estimated ready at 14:50*.
  - Chi tiết hóa đơn đầy đủ (Món, Giá, Giảm giá, Tip, Thuế VAT, Tổng tiền).
  - Hộp thông báo thưởng: `Congratulations! +170 Points have been added to your account`.
  - Nút thao tác: `Back to Home`, `Download E-Receipt (PDF)`.
- **API Endpoint:** `GET /api/v1/orders/{orderCode}/receipt`

---

### PHÂN HỆ 07: VÍ MY REWARDS & ĐỔI QUÀ (REWARDS & WALLET)

#### Màn hình 07.01 — Ví Voucher Của Tôi (My Rewards - Active Vouchers)
- **Figma Frame:** `07.01-my-rewards-active`
- **Thành phần UI:**
  - Thanh Tab điều hướng: `My Rewards (Active)` / `Expired & Used`.
  - Danh sách các Voucher khách hàng đang sở hữu:
    - Tiêu đề: *Voucher 100 NOK*, *-30% Total Bill*.
    - Mô tả: *Valid on orders above 250 NOK*.
    - Thời hạn sử dụng: *Expires 03 Dec 2026*.
    - Nút bấm: `Use Now` (Chuyển đến giỏ hàng để tự động gán).
  - Nút nổi bật: `+ Get More Rewards` (Dẫn tới màn hình đổi quà 07.02).

#### Màn hình 07.02 — Đổi Thưởng Lấy Quà / Voucher (Get More Rewards)
- **Figma Frame:** `07.02-get-more-rewards`
- **Thành phần UI:**
  - Hiển thị số điểm khả dụng của khách: `Your balance: 120 Points`.
  - **Mục 1: Voucher Ưu Đãi (Voucher Rewards):**
    - Voucher 100 NOK (Chi phí: 100 Points). Nút: `Redeem`.
    - Voucher 30% OFF (Chi phí: 1,000 Points). Nút: `Need 880 more pts` (Vô hiệu hóa nếu không đủ điểm).
  - **Mục 2: Quà Hiện Vật Độc Quyền (Physical Loyalty Gifts):**
    - Ảnh ly giữ nhiệt tumbler màu hồng.
    - Tiêu đề: *Gift Name - Cốc Tumbler Ordera Pink*.
    - Mô tả: *Nhận trực tiếp tại quầy thu ngân sau khi đổi*.
    - Chi phí: `100 Points`.
    - Tồn kho còn lại: `Quantity left: 10 Pieces`.
    - Nút bấm: `Redeem Gift`.
- **Business Logic & Race Condition Protection:**
  - Bắt buộc kiểm tra số dư: `member.current_points >= item.points_cost`.
  - Bắt buộc kiểm tra tồn kho quà hiện vật: `stock_quantity > 0`.
  - Bắt buộc áp dụng **Khóa Phân Tán (Redisson Distributed Lock)** theo khóa `lock:member:{memberId}` và `lock:gift:{giftId}` để chống việc bấm đúp 2 lần liên tiếp gây trừ điểm âm hoặc cướp quà quá tồn kho.
  - Giao dịch thực hiện nguyên tử (Atomic ACID Transaction):
    1. Trừ `points_cost` trong `member_profiles.current_points`.
    2. Ghi một bản ghi trừ điểm vào `points_ledger` (TransactionType = `VOUCHER_REDEEM` hoặc `GIFT_REDEEM`).
    3. Tạo bản ghi trong `user_vouchers` hoặc `user_gift_redemptions`.
- **API Endpoints:**
  - `POST /api/v1/rewards/vouchers/{voucherId}/redeem`
  - `POST /api/v1/rewards/gifts/{giftId}/redeem`

#### Màn hình 07.05 & 07.06 — Trạng thái Ví Rỗng (Empty States)
- **Figma Frame:** `07.05-empty-active-rewards`, `07.06-empty-history-rewards`
- **Thành phần UI:**
  - Minh họa hình vẽ đồ họa vector trống (Empty Illustration).
  - Tiêu đề: *No active rewards yet* / *No reward history*.
  - Đoạn văn khuyến khích: *You haven't claimed any rewards. Order more food to earn points and unlock great discounts!*
  - Nút CTA: `Explore Rewards Catalog`.

#### Màn hình 07.08 — Voucher Đang Được Sử Dụng (Voucher In-Use State)
- **Figma Frame:** `07.08-voucher-in-use`
- **Mục đích:** Hiển thị rõ ràng voucher nào đã được gán vào một giỏ hàng đang chờ thanh toán để tránh việc dùng 1 voucher cho 2 thiết bị khác nhau.
- **Thành phần UI:**
  - Thẻ voucher có viền màu vàng/cam kèm huy hiệu nhãn: `In Use on Order #ORD-8921`.
  - Không cho phép bấm "Use" ở màn hình khác cho đến khi đơn hàng đó bị hủy hoặc voucher được gỡ ra.

---

### PHÂN HỆ 08: DRAWER CHỌN ƯU ĐÃI TRONG CHECKOUT (VOUCHER DRAWER)

#### Màn hình 08.01 — Bottom Sheet Drawer Chọn Voucher
- **Figma Frame:** `08.01-checkout-voucher-drawer`
- **Mục đích:** Khách hàng bấm "Apply Voucher" tại màn hình Checkout sẽ trượt một Drawer từ dưới lên để chọn mã.
- **Thành phần UI:**
  - Tiêu đề: `Select Voucher`.
  - Ô nhập mã thủ công: `Promo code (SAVE100)` kèm nút `Apply`.
  - Danh sách các voucher đủ điều kiện (Eligible):
    - Có nút Radio chọn 1 voucher.
    - Hiển thị số tiền sẽ được trừ ngay: `-100.00 Kr`.
  - Danh sách các voucher không đủ điều kiện (Ineligible - làm mờ màu xám):
    - Lý do không áp dụng: *Order minimum is 250.00 Kr (Current: 180.00 Kr)*.
  - Nút xác nhận: `Confirm Selection`.

---

### PHÂN HỆ 09: KIOSK MÀN HÌNH CHẠM TẠI QUẦY (KIOSK TOUCHSCREEN POS)

#### Màn hình Kiosk 01 — Tích Điểm Nhanh & Đếm ngược Tự động (Kiosk Touchscreen)
- **Figma Frame:** `kiosk-pos-touchscreen`
- **Thành phần UI:**
  - Giao diện chế độ toàn màn hình (Fullscreen Kiosk Mode), nút bấm cực lớn tối ưu cho thao tác chạm tay.
  - Bàn phím số ảo (Virtual Numpad) nhập nhanh SĐT quốc gia Na Uy.
  - Sau khi xác nhận SĐT:
    - Nếu đã là thành viên: Hiển thị lời chào *Hello, David Nguyen!*, hiển thị điểm tích lũy hiện tại và số điểm sẽ được tích cho hóa đơn này.
    - Nếu là khách mới: Tự động khởi tạo thẻ thành viên nhanh.
  - Chọn Tips nhanh tại quầy: `No Tip`, `10 Kr`, `20 Kr`, `50 Kr`.
  - **Bảo mật Kiosk:** Bộ đếm ngược an toàn `60s Auto-reset`. Nếu khách hàng không chạm vào màn hình trong 60 giây, ứng dụng Kiosk sẽ tự động đăng xuất, xóa toàn bộ bộ nhớ đệm và quay trở về màn hình chào ban đầu để bảo vệ dữ liệu khách hàng.

---

## 3. Hệ thống Quy tắc Nghiệp vụ Cốt lõi (Master Business Rules - BR)

| Mã Quy tắc | Tên Quy tắc | Nội dung Chi tiết |
|:---|:---|:---|
| **BR-01** | **Multi-Tenant RLS Bắt buộc** | Mọi truy vấn database liên quan đến khách hàng, đơn hàng, menu, voucher đều bắt buộc lọc theo `tenant_id`. Không chấp nhận bất kỳ truy vấn nào bỏ sót `tenant_id`. |
| **BR-02** | **Tính Bất biến của Sổ cái Điểm** | Cấm 100% lệnh `UPDATE member_profiles SET current_points = X`. Mọi biến động điểm (cộng, trừ, hủy) bắt buộc phải chèn một bản ghi mới vào bảng phân vùng `points_ledger` kèm `reference_type` và `reference_id`. |
| **BR-03** | **Tách biệt Điểm Khả dụng & Điểm Trọn đời** | Khi đổi voucher hoặc quà tặng, chỉ trừ `current_points`. Tuyệt đối không giảm `lifetime_points` để đảm bảo khách không bị tụt hạng thẻ khi đổi quà. |
| **BR-04** | **Quy chuẩn 6 Hạng thẻ Lũy tiến** | Mốc điểm xét hạng trọn đời: New (0), Silver (200), Gold (400), Platinum (1,000), Diamond (2,000), Ruby (4,000). Khi `lifetime_points` chạm mốc mới, hệ thống tự động thăng hạng ngay lập tức. |
| **BR-05** | **Khóa Phân tán Chống Đổi Quà Kép** | Khi thực hiện đổi voucher hoặc quà hiện vật, bắt buộc chiếm giữ Redis Lock (`lock:member:{id}`) trong tối đa 3 giây để tránh gian lận do bấm liên tục (Double-tap attack). |
| **BR-06** | **Ràng buộc Áp dụng Voucher** | Mỗi đơn hàng chỉ được áp dụng tối đa 01 voucher giảm giá. Giá trị đơn hàng tối thiểu (`subtotal`) phải $\ge$ `min_order_value` của voucher đó. |
| **BR-07** | **Khóa Trạng thái Voucher In-Use** | Khi voucher được chọn trong giỏ hàng và gửi tạo đơn hàng, trạng thái của nó chuyển sang `IN_USE` để ngăn chặn việc mở thiết bị khác áp dụng cùng mã đó. |
| **BR-08** | **Quy chuẩn Tiền tệ & Làm tròn** | Mọi phép toán tiền tệ (`subtotal`, `discount`, `tip`, `vat`, `total`) phải dùng kiểu số thực `BigDecimal` với scale = 2 (2 chữ số thập phân), làm tròn theo `RoundingMode.HALF_UP`. Đơn vị tiền tệ chuẩn là `NOK` (Kr). |
| **BR-09** | **Thuế Giá trị Gia tăng (VAT)** | Thuế VAT đồ ăn/đồ uống tiêu chuẩn là 15.00% (đã bao gồm trong giá niêm yết của món ăn). Công thức tính tiền VAT trong đơn hàng: $\text{VAT} = \text{Total} \times \frac{15}{115}$. |
| **BR-10** | **Giới hạn Cooldown OTP** | Mỗi mã OTP chỉ có hiệu lực trong 120 giây. Khoảng cách giữa 2 lần gửi mã tối thiểu là 60 giây. Sau 5 lần nhập sai, phiên OTP bị hủy. |

---

## 4. Ma trận Truy vết Toàn diện (Requirements Traceability Matrix - RTM)

Bảng đối chiếu chuẩn giúp kỹ sư kiểm tra chéo: từ màn hình Figma $\rightarrow$ Yêu cầu chức năng $\rightarrow$ Use Case $\rightarrow$ API Endpoint $\rightarrow$ Bảng Database.

| Màn hình Figma | Yêu cầu Chức năng | Use Case Backend | REST API Endpoint | Bảng Database liên quan |
|:---|:---|:---|:---|:---|
| **01.01** | Tìm kiếm chi nhánh | `FindStoresUseCase` | `GET /api/v1/stores` | `stores`, `tenants` |
| **01.02** | Xem menu theo danh mục | `GetStoreCatalogUseCase` | `GET /api/v1/stores/{id}/menu` | `categories`, `products` |
| **01.03** | Tùy biến món & modifier | `GetProductModifiersUseCase` | `GET /api/v1/products/{id}/modifiers` | `product_modifier_groups`, `product_modifiers` |
| **02.01, 02.02** | Đăng nhập OTP SMS/Email | `SendOtpUseCase`, `VerifyOtpUseCase` | `POST /api/v1/auth/otp/send`<br>`POST /api/v1/auth/otp/verify` | `users`, `otp_audit_logs`, Redis |
| **03.01** | Đăng ký thành viên mới | `RegisterMemberUseCase` | `POST /api/v1/auth/register` | `users`, `member_profiles`, `tier_definitions` |
| **04.01** | Xem hồ sơ & hạng thẻ | `GetMemberProfileUseCase` | `GET /api/v1/loyalty/members/me` | `member_profiles`, `tier_definitions` |
| **04.02** | Xem lịch sử điểm sổ cái | `GetPointsHistoryUseCase` | `GET /api/v1/loyalty/members/me/points` | `points_ledger` (Partitioned) |
| **05.01** | Xem đặc quyền 6 hạng thẻ | `CalculateTierProgressUseCase` | `GET /api/v1/loyalty/members/{id}/progress`<br>`GET /api/v1/loyalty/tiers` | `tier_definitions`, `member_profiles` |
| **05.02** | Gửi khiếu nại thiếu điểm | `SubmitSupportClaimUseCase` | `POST /api/v1/loyalty/claims` | `support_claims` |
| **06.01, 06.02** | Tạo đơn & Checkout | `CheckoutOrderUseCase` | `POST /api/v1/orders/checkout` | `orders`, `order_items`, `order_item_modifiers`, `user_vouchers` |
| **06.03** | Xem hóa đơn E-Receipt | `GetOrderReceiptUseCase` | `GET /api/v1/orders/{code}/receipt` | `orders`, `order_items`, `stores` |
| **07.01** | Xem ví My Rewards | `GetUserVouchersUseCase` | `GET /api/v1/rewards/my-vouchers` | `user_vouchers`, `vouchers` |
| **07.02, 07.03** | Đổi điểm lấy voucher/quà | `RedeemVoucherUseCase`<br>`RedeemGiftUseCase` | `POST /api/v1/rewards/vouchers/{id}/redeem`<br>`POST /api/v1/rewards/gifts/{id}/redeem` | `points_ledger`, `user_vouchers`, `loyalty_gifts`, `user_gift_redemptions` |
| **08.01** | Chọn voucher trong drawer | `GetEligibleVouchersUseCase` | `GET /api/v1/rewards/checkout-eligible` | `user_vouchers`, `vouchers` |
| **Kiosk POS** | Tích điểm quầy & Auto-reset | `KioskEarnPointsUseCase` | `POST /api/v1/kiosk/quick-earn` | `users`, `orders`, `points_ledger` |

---

## 5. Ký duyệt & Hiệu lực Áp dụng (Sign-off & Governance)
- Mọi Pull Request (PR) hoặc tác vụ sinh mã nguồn mới (Feature/Bug fix) **bắt buộc phải trích dẫn mã màn hình hoặc mã BR tương ứng trong tài liệu này**.
- Khi phát hiện mâu thuẫn giữa code và tài liệu, mã nguồn phải được sửa để tuân thủ theo SRS này (trừ khi có tài liệu Architecture Amendment được phê duyệt bằng văn bản).
