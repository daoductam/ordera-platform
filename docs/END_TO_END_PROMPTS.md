# Ordera Platform — End-to-End Development Prompt Playbook
## Sổ Tay Câu Lệnh Prompt Phát Triển Toàn Diện Từ Đầu Đến Cuối

> **Mục đích:** Tập hợp sẵn các câu lệnh Prompt chuẩn hóa theo từng giai đoạn (Phase) và từng tính năng (Feature). Bạn chỉ cần **Copy & Paste** từng câu lệnh vào ô chat với AI Agent để hệ thống tự động thực thi tuần tự từ phân tích, lập kế hoạch, code, kiểm thử đến commit.
>
> **Nguyên tắc kỹ thuật áp dụng xuyên suốt:**
> - Clean Architecture 4 lớp: `presentation`, `application`, `domain`, `infrastructure`.
> - Multi-tenant isolation: Bắt buộc lọc theo `X-Tenant-ID` qua `TenantContextHolder` (ngăn chặn IDOR).
> - Sổ cái điểm bất biến: Tuyệt đối không UPDATE trực tiếp cột điểm, mọi biến động đều ghi vào `points_ledger`.
> - Quy chuẩn commit: Tuân thủ skill `action-commit` (`develop: ...` kèm trailer `Co-authored-by`).

---

## 📑 Mục Lục Các Giai Đoạn (Execution Roadmap)

- [Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend](#giai-đoạn-1-triển-khai-toàn-diện-8-module-backend)
  - [Prompt 1.1: Phân hệ Cửa Hàng & Bàn Ăn QR (Tenant & Store Module)](#prompt-11-phân-hệ-cửa-hàng--bàn-ăn-qr-tenant--store-module)
  - [Prompt 1.2: Phân hệ Xác Thực OTP & JWT (Auth & Identity Module)](#prompt-12-phân-hệ-xác-thực-otp--jwt-auth--identity-module)
  - [Prompt 1.3: Phân hệ Thực Đơn & Nhóm Tùy Chọn Modifier (Catalog Module)](#prompt-13-phân-hệ-thực-đơn--nhóm-tùy-chọn-modifier-catalog-module)
  - [Prompt 1.4: Phân hệ Động Cơ Thẻ Thành Viên & Sổ Cái Điểm (Loyalty Engine)](#prompt-14-phân-hệ-động-cơ-thẻ-thành-viên--sổ-cái-điểm-loyalty-engine)
  - [Prompt 1.5: Phân hệ Ví Voucher & Kho Quà Hiện Vật (Reward Module)](#prompt-15-phân-hệ-ví-voucher--kho-quà-hiện-vật-reward-module)
  - [Prompt 1.6: Phân hệ Giỏ Hàng, VAT 15%, Tips & Checkout (Order Module)](#prompt-16-phân-hệ-giỏ-hàng-vat-15-tips--checkout-order-module)
  - [Prompt 1.7: Phân hệ Trạm Cảm Ứng Kiosk POS & Tích Điểm Nhanh (Kiosk Module)](#prompt-17-phân-hệ-trạm-cảm-ứng-kiosk-pos--tích-điểm-nhanh-kiosk-module)
  - [Prompt 1.8: Phân hệ Hộp Thư Thông Báo & Khiếu Nại Sót Điểm (Claim & Notif)](#prompt-18-phân-hệ-hộp-thư-thông-báo--khiếu-nại-sót-điểm-claim--notif)
- [Giai Đoạn 2: Tích Hợp Frontend PWA & Kiosk Với Backend API](#giai-đoạn-2-tích-hợp-frontend-pwa--kiosk-với-backend-api)
  - [Prompt 2.1: Tích hợp API Client & Context State Frontend](#prompt-21-tích-hợp-api-client--context-state-frontend)
  - [Prompt 2.2: Kiểm thử Luồng Hoạt động Kiosk POS với Backend Thực](#prompt-22-kiểm-thử-luồng-hoạt-động-kiosk-pos-với-backend-thực)
- [Giai Đoạn 3: Kiểm Thử Toàn Diện, Bảo Mật & CI/CD](#giai-đoạn-3-kiểm-thử-toàn-diện-bảo-mật--cicd)
  - [Prompt 3.1: Viết Bộ Test Tích Hợp Testcontainers (PostgreSQL + Redis)](#prompt-31-viết-bộ-test-tích-hợp-testcontainers-postgresql--redis)
  - [Prompt 3.2: Quét Bảo Mật & Đóng Gói Docker Compose](#prompt-32-quét-bảo-mật--đóng-gói-docker-compose)

---

## Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend

### Prompt 1.1: Phân hệ Cửa Hàng & Bàn Ăn QR (Tenant & Store Module)
> **Mục tiêu:** Cung cấp thông tin thương hiệu, chi nhánh Linn Sushi Sentrum, danh sách bàn ăn và giải mã mã QR bàn.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Cửa Hàng, Chi Nhánh & Bàn Ăn (Tenant & Store Module) cho Backend theo đúng Clean Architecture 4 lớp.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/README.md (Tính năng STR-01, STR-02).
2. API Contract: docs/designs/api-design/01-tenant-and-store-api.md (Các API: GET /api/v1/tenant/info, GET /api/v1/stores, GET /api/v1/stores/{id}, GET /api/v1/stores/{id}/tables, GET /api/v1/stores/tables/qr-resolve).
3. DB Design: docs/designs/db-design/01-tenants-and-stores-db.md (Bảng tenants, stores, store_tables).

Yêu cầu thực hiện:
- Tầng Domain: Pure Java model Store, StoreTable, Tenant; custom domain exceptions.
- Tầng Application: UseCases lấy thông tin thương hiệu, danh sách chi nhánh kèm khoảng cách GPS, lấy danh sách bàn và giải mã QR token.
- Tầng Infrastructure: Tận dụng các JPA Entity và Repository có sẵn trong infrastructure/persistence.
- Tầng Presentation: Viết StoreController, TenantController, DTOs theo đúng chuẩn Envelope ApiResponse<T> và mã lỗi RFC 7807.
- Kiểm thử: Viết Unit tests theo mẫu AAA cho UseCases và Controller.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.2: Phân hệ Xác Thực OTP & JWT (Auth & Identity Module)
> **Mục tiêu:** Xây dựng luồng xác thực đăng nhập/đăng ký qua SMS/Email OTP (cooldown 60s, giới hạn 5 lần trên Redis), nạp hồ sơ David Nguyen và cấp phát JWT Token.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Xác Thực OTP & Cấp Phát JWT (Auth & Identity Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-01-onboarding-va-dinh-danh.md (Luồng BF-01, tính năng AUTH-01 đến AUTH-04).
2. API Contract: docs/designs/api-design/03-auth-and-identity-api.md (API 3.1 đến 3.5: Gửi OTP, Xác thực OTP, Đăng ký hồ sơ, Làm mới token, Đăng xuất).
3. DB Design: docs/designs/db-design/02-users-and-identity-db.md (Bảng users, otp_audit_logs).

Yêu cầu thực hiện:
- Tầng Domain: Domain models User, Role, OtpSession, TokenPayload.
- Tầng Application:
  + SendOtpUseCase: Kiểm tra cooldown 60s trên Redis, sinh ngẫu nhiên 6 chữ số, băm SHA-256 lưu Redis (TTL 120s), gọi SMS/Mail gateway (mock log).
  + VerifyOtpUseCase: Kiểm tra số lần nhập sai (max 5 lần thì khóa 15p), kiểm tra khớp mã OTP.
  + RegisterUserUseCase: Đăng ký thành viên mới, tạo tài khoản users và tự động khởi tạo member_profiles (Hạng New, 0 điểm).
  + JwtTokenService: Sinh HMAC-SHA512 Access Token (15 phút) và Refresh Token (7 ngày), xử lý blacklist token trên Redis khi Logout.
- Tầng Presentation: AuthController hoàn chỉnh với mã lỗi RFC 7807 (OTP_INVALID, OTP_EXPIRED, OTP_COOLDOWN_ACTIVE, OTP_MAX_ATTEMPTS_EXCEEDED).
- Kiểm thử: Unit tests kiểm tra chặt chẽ các trường hợp đúng, sai OTP, hết hạn và vượt quá 5 lần.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.3: Phân hệ Thực Đơn & Nhóm Tùy Chọn Modifier (Catalog Module)
> **Mục tiêu:** Cung cấp danh mục thực đơn trượt ngang, chi tiết món ăn (Aurora 89 Kr, Tuna Maki), và nhóm tùy chọn (Size L, đá, đường, topping kèm phụ thu).

```markdown
/feature Hãy triển khai tính năng: Phân hệ Quản Lý Thực Đơn & Tùy Chọn Món (Catalog & Modifiers Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-02-thuc-don-va-tuy-chon-mon.md (Luồng BF-02, tính năng CAT-01, CAT-02).
2. API Contract: docs/designs/api-design/02-catalog-and-menu-api.md (API 2.1 đến 2.4: Danh mục món, Cây thực đơn phân cấp, Chi tiết món, Nhóm Modifiers).
3. DB Design: docs/designs/db-design/04-catalog-and-menu-db.md (Bảng categories, products, product_modifier_groups, product_modifiers).

Yêu cầu thực hiện:
- Tầng Domain: Category, Product, ProductModifierGroup, ProductModifier.
- Tầng Application:
  + GetStoreMenuUseCase: Trả về cây thực đơn phân cấp hoàn chỉnh (Categories kèm danh sách Products khả dụng).
  + GetProductDetailUseCase: Lấy chi tiết món kèm các nhóm modifier bắt buộc (min_selection = 1) và tùy chọn topping.
- Tầng Presentation: CategoryController, ProductController với đầy đủ DTOs theo chuẩn `ApiResponse<T>`.
- Đảm bảo kiểm tra cờ is_available và lọc theo Tenant RLS.
- Kiểm thử: Unit tests cho UseCases.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.4: Phân hệ Động Cơ Thẻ Thành Viên & Sổ Cái Điểm (Loyalty Engine)
> **Mục tiêu:** Quản lý hồ sơ David Nguyen (Hạng Silver, 120 điểm), kế toán sổ cái kép bất biến `points_ledger` và tự động thăng tiến 6 cấp bậc thành viên.

```markdown
/feature Hãy triển khai tính năng: Động Cơ Thẻ Thành Viên & Sổ Cái Điểm Bất Biến (Loyalty & Points Ledger Engine) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-04-tich-luy-diem-va-thang-hang.md (Luồng BF-04, tính năng LOY-01, LOY-02).
2. API Contract: docs/designs/api-design/04-loyalty-and-tier-api.md (API 4.1 đến 4.4: Hồ sơ thành viên, Tiến trình thăng hạng Stepper, Định nghĩa 6 bậc, Lịch sử sổ cái điểm phân trang).
3. DB Design: docs/designs/db-design/03-loyalty-and-points-ledger-db.md (Bảng tier_definitions, member_profiles, points_ledger).

Yêu cầu thực hiện:
- Tầng Domain: MemberProfile, MemberTier (New, Silver, Gold, Platinum, Diamond, Ruby), TransactionType (ORDER_EARN, VOUCHER_REDEEM, GIFT_REDEEM, TIER_BONUS, CLAIM_APPROVED, KIOSK_EARN).
- Tầng Application:
  + GetMemberProfileUseCase: Trả về tên, hạng thẻ, điểm khả dụng (current_points), điểm trọn đời (lifetime_points), mã màu hex và tỷ lệ hoàn điểm.
  + CalculateTierProgressUseCase: Tính toán chính xác phần trăm tiến trình và số điểm còn thiếu để lên hạng tiếp theo (Figma 04.05).
  + AccruePointsUseCase: Ghi sổ cái bất biến points_ledger, cập nhật điểm, kiểm tra mốc thăng hạng và nâng bậc thẻ nếu đủ điều kiện.
  + GetPointsHistoryUseCase: Truy vấn lịch sử giao dịch điểm phân trang có sắp xếp thời gian giảm dần.
- BẢO MẬT & BẤT BIẾN: Nghiêm cấm mọi lệnh UPDATE trực tiếp trên points_ledger.
- Kiểm thử: Unit tests kiểm tra kỹ công thức tính điểm và logic thăng hạng qua 6 bậc.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.5: Phân hệ Ví Voucher & Kho Quà Hiện Vật (Reward Module)
> **Mục tiêu:** Quản lý ví ưu đãi `My Rewards`, kho đổi thưởng `Get More Rewards` (voucher 100 NOK, -30%), đổi Cốc Tumbler và khóa phân tán Redisson.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Ví Voucher & Đổi Quà Hiện Vật (Reward & Voucher Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-05-doi-thuong-va-quan-ly-vi.md (Luồng BF-05, tính năng REW-01 đến REW-04).
2. API Contract: docs/designs/api-design/05-reward-and-voucher-api.md (API 5.1 đến 5.5: Ví My Rewards, Kho đổi thưởng, Đổi voucher, Đổi cốc Tumbler, Voucher hợp lệ cho Checkout).
3. DB Design: docs/designs/db-design/05-rewards-and-vouchers-db.md (Bảng vouchers, user_vouchers, loyalty_gifts, user_gift_redemptions).

Yêu cầu thực hiện:
- Tầng Domain: Voucher, UserVoucher, VoucherStatus (AVAILABLE, IN_USE, USED, EXPIRED), LoyaltyGift, UserGiftRedemption.
- Tầng Application:
  + RedeemVoucherUseCase: Kiểm tra điểm khả dụng >= points_cost, sử dụng Redisson Lock lock:user:redeem:{userId}, trừ điểm khả dụng (không trừ điểm trọn đời), ghi sổ cái points_ledger (VOUCHER_REDEEM), tạo user_vouchers (AVAILABLE).
  + RedeemPhysicalGiftUseCase: Sử dụng Redisson Lock kiểm tra tồn kho stock_quantity > 0, trừ điểm, trừ 1 tồn kho, sinh mã nhận quà 8 ký tự duy nhất (redemption_code), tạo user_gift_redemptions (PENDING).
  + GetEligibleVouchersUseCase: Lọc các voucher trong ví thỏa mãn điều kiện đơn hàng tối thiểu min_order_value.
  + ClaimPhysicalGiftUseCase: Dành cho thu ngân tại quầy xác nhận trao cốc Tumbler, chuyển trạng thái sang CLAIMED.
- Tầng Presentation: RewardController với xử lý lỗi RFC 7807 (POINTS_INSUFFICIENT, GIFT_OUT_OF_STOCK, CONCURRENT_TRANSACTION_LOCK).
- Kiểm thử: Unit tests cho các kịch bản đủ điểm, thiếu điểm, hết tồn kho quà.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.6: Phân hệ Giỏ Hàng, VAT 15%, Tips & Checkout (Order Module)
> **Mục tiêu:** Xem trước tính tiền, tính thuế VAT 15% Na Uy, tiền tip, thanh toán chốt đơn, khóa voucher và sinh hóa đơn điện tử E-Receipt.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Đặt Hàng, Tính Thuế VAT, Tips & Checkout (Order & Checkout Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-03-dat-mon-va-thanh-toan.md (Luồng BF-03, tính năng ORD-01 đến ORD-05).
2. API Contract: docs/designs/api-design/06-order-and-checkout-api.md (API 6.1 đến 6.5: Preview tính tiền, Checkout đặt đơn, E-Receipt, Đơn tích cực, Lịch sử đơn).
3. DB Design: docs/designs/db-design/06-orders-and-checkout-db.md (Bảng orders, order_items, order_item_modifiers).

Yêu cầu thực hiện:
- Tầng Domain: Order, OrderItem, OrderItemModifier, OrderStatus, PaymentStatus, PickupMethod (COUNTER, TABLE).
- Tầng Application:
  + PreviewOrderCalculationUseCase: Nhận giỏ hàng, ID voucher, tiền tip để tính Subtotal, Discount, Tip, VAT 15% (công thức Total * 15 / 115), Tổng tiền và Điểm dự kiến.
  + CheckoutOrderUseCase:
    * Kiểm tra Idempotency-Key.
    * Chiếm giữ Redisson Lock khóa voucher.
    * Tạo bản ghi orders, order_items, order_item_modifiers.
    * Cập nhật user_vouchers sang USED.
    * Tự động gọi AccruePointsUseCase ghi sổ cái tích điểm (ORDER_EARN).
    * Sinh mã đơn và số thứ tự nhận món #A-08.
  + GetOrderDetailUseCase / EReceipt: Trả về chi tiết hóa đơn điện tử cho khách.
- Tầng Presentation: OrderController bọc đầy đủ ApiResponse<T> và mã lỗi RFC 7807.
- Kiểm thử: Unit test tính toán số học chính xác đến từng cent (scale = 2, BigDecimal), kiểm tra công thức VAT và chiết khấu voucher.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.7: Phân hệ Trạm Cảm Ứng Kiosk POS & Tích Điểm Nhanh (Kiosk Module)
> **Mục tiêu:** Cung cấp API cho màn hình cảm ứng toàn màn hình tại quầy: nhận diện SĐT, tích điểm nhanh cho đơn giấy không cần pass, và tự động reset phiên 60s idle.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Kiosk POS & Tích Điểm Nhanh Tại Quầy (Kiosk POS Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-06-kiosk-pos-tu-phuc-vu.md (Luồng BF-06, tính năng KSK-01 đến KSK-03).
2. API Contract: docs/designs/api-design/07-kiosk-pos-api.md (API 7.1 đến 7.5: Nhận diện SĐT, Tích điểm nhanh, Đặt đơn Kiosk, Reset phiên, Cấu hình trạm).
3. Frontend tham chiếu: frontend/src/pages/KioskPage.tsx.

Yêu cầu thực hiện:
- Tầng Domain: KioskSession, KioskDeviceConfig.
- Tầng Application:
  + KioskIdentifyCustomerUseCase: Nhận SĐT từ bàn phím số, tra cứu danh tính thành viên, trả về tên, hạng thẻ, số điểm và tỷ lệ tích lũy. Nếu khách mới trả về isRegistered = false.
  + KioskQuickEarnUseCase: Tích điểm nhanh cho đơn giấy tại quầy, tính điểm theo hạng thẻ, ghi sổ cái points_ledger (TransactionType = KIOSK_EARN), sinh số thứ tự #A-08 và lệnh in nhiệt.
  + ResetKioskSessionUseCase: Xóa session tạm thời trên Redis khi bộ đếm 60 giây hết hạn hoặc khách bấm Start Over.
  + GetKioskConfigUseCase: Cung cấp thông tin cấu hình trạm Kiosk theo deviceId.
- Tầng Presentation: KioskController.
- Kiểm thử: Unit tests cho luồng tra cứu khách hàng và tích điểm nhanh.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.8: Phân hệ Hộp Thư Thông Báo & Khiếu Nại Sót Điểm (Claim & Notif)
> **Mục tiêu:** Hộp thư 4 tab ("Messages", "News", "Voucher", "Loyalty Gifts") và biểu mẫu gửi khiếu nại điểm sót (Missing points claim) kèm ảnh bill giấy.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Thông Báo & Khiếu Nại Điểm Sót (Notifications & Missing Points Claim Module) cho Backend.

Tham chiếu kỹ thuật bắt buộc:
1. SRS: docs/designs/srs/bf-07-khieu-nai-thieu-diem.md (Luồng BF-07, tính năng CLM-01, CLM-02, NTF-01).
2. API Contract: docs/designs/api-design/08-notification-api.md (API 8.1 đến 8.5: Danh sách thông báo theo category, Đánh dấu đã đọc, Đếm unread, Gửi khiếu nại Missing Points).
3. DB Design: docs/designs/db-design/03-loyalty-and-points-ledger-db.md (Bảng support_claims).

Yêu cầu thực hiện:
- Tầng Domain: SupportClaim, ClaimStatus (PENDING, APPROVED, REJECTED), SystemNotification.
- Tầng Application:
  + SubmitSupportClaimUseCase: Nhận mã bill giấy, ngày mua (trong vòng 30 ngày), số tiền, link ảnh bill, tạo bản ghi support_claims (PENDING).
  + ResolveSupportClaimUseCase: Dành cho Quản lý chi nhánh đối soát, nếu APPROVED sẽ gọi AccruePointsUseCase cộng bù điểm vào sổ cái (CLAIM_APPROVED), nếu REJECTED ghi rõ lý do.
  + NotificationUseCases: Lấy thông báo theo category (Messages, News, Voucher, Loyalty Gifts), đánh dấu đã đọc và đếm số lượng chưa đọc.
- Tầng Presentation: NotificationController, SupportClaimController.
- Kiểm thử: Unit tests cho quy trình khiếu nại và duyệt điểm bù.
- Sau khi hoàn thành và test pass, hãy commit vào nhánh develop theo skill action-commit.
```

---

## Giai Đoạn 2: Tích Hợp Frontend PWA & Kiosk Với Backend API

### Prompt 2.1: Tích hợp API Client & Context State Frontend
> **Mục tiêu:** Thay thế dữ liệu mock trong Frontend React bằng các lệnh gọi REST API thực tế tới Backend, duy trì trạng thái giỏ hàng và token đăng nhập.

```markdown
/feature Hãy thực hiện: Kết nối Frontend React 19 với Backend REST API thực tế (Full API Integration).

Tài liệu tham chiếu:
- docs/designs/api-design/README.md (Chuẩn Envelope ApiResponse<T> và Headers).
- frontend/src/types/index.ts và các file trong frontend/src/api/.

Yêu cầu thực hiện:
1. Tạo HTTP Client tập trung (Axios hoặc Fetch wrapper) có tự động chèn Header `X-Tenant-ID` và `Authorization: Bearer <token>`, tự động gọi refresh-token khi nhận mã lỗi 401 TOKEN_EXPIRED.
2. Nối API cho CartContext: Thay thế hàm tính tiền mock bằng việc gọi API POST /api/v1/orders/preview.
3. Nối API cho AuthContext: Đăng nhập OTP thực tế qua POST /api/v1/auth/otp/send và verify.
4. Nối API cho trang chủ HomePage, trang chi tiết món ăn ProductDetailModal, và trang đổi thưởng RewardsPage.
5. Kiểm tra build frontend bằng lệnh `npm.cmd run build` đảm bảo không có lỗi kiểu TypeScript.
6. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 2.2: Kiểm thử Luồng Hoạt động Kiosk POS với Backend Thực
> **Mục tiêu:** Kết nối màn hình cảm ứng Kiosk POS với Backend API để tra cứu SĐT thực và in biên nhận số thứ tự.

```markdown
/feature Hãy thực hiện: Kết nối trang Kiosk POS (KioskPage.tsx) với Backend Kiosk API thực tế.

Tài liệu tham chiếu:
- docs/designs/api-design/07-kiosk-pos-api.md
- docs/designs/srs/bf-06-kiosk-pos-tu-phuc-vu.md
- frontend/src/pages/KioskPage.tsx

Yêu cầu thực hiện:
1. Thay thế mock handleLookup bằng lời gọi POST /api/v1/kiosk/identify với số điện thoại vừa gõ trên Touch Numpad.
2. Hiển thị đúng thông tin thành viên (Đào Tâm Đức, Gold Member, 2450 pts) trả về từ máy chủ.
3. Thay thế handleCompletePayment bằng POST /api/v1/kiosk/quick-earn để ghi sổ cái tích điểm và nhận số thứ tự thực tế (#A-08).
4. Duy trì bộ đếm thời gian 60 giây idle timeout: khi hết hạn gọi POST /api/v1/kiosk/session/reset và đưa màn hình về trạng thái chờ ban đầu.
5. Kiểm tra build frontend bằng `npm.cmd run build`.
6. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

## Giai Đoạn 3: Kiểm Thử Toàn Diện, Bảo Mật & CI/CD

### Prompt 3.1: Viết Bộ Test Tích Hợp Testcontainers (PostgreSQL + Redis)
> **Mục tiêu:** Đảm bảo toàn bộ hệ thống hoạt động hoàn hảo trên môi trường CSDL thật với PostgreSQL 16 và Redis 7.

```markdown
/feature Hãy viết bộ kiểm thử tích hợp (Integration Tests) cho Backend sử dụng Testcontainers.

Yêu cầu thực hiện:
1. Cấu hình Testcontainers khởi động container PostgreSQL 16 và Redis 7 thật trong môi trường test.
2. Viết Integration Test cho luồng Checkout đặt đơn: Tạo giỏ hàng -> Áp mã voucher -> Trừ điểm -> Khóa phân tán Redisson -> Kiểm tra dữ liệu được ghi đúng vào bảng orders và points_ledger.
3. Viết Integration Test cho bảo mật đa thuê (Multi-tenant RLS): Đảm bảo Tenant A không thể đọc được dữ liệu của Tenant B ngay cả khi biết ID (Ngăn chặn 100% lỗ hổng IDOR).
4. Chạy toàn bộ test suite bằng `cd backend; mvn clean test` đảm bảo BUILD SUCCESS.
5. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 3.2: Quét Bảo Mật & Đóng Gói Docker Compose
> **Mục tiêu:** Đảm bảo mã nguồn sạch sẽ, không lộ secret, cấu hình Nginx reverse proxy và docker-compose sẵn sàng chạy production.

```markdown
/feature Hãy rà soát bảo mật toàn diện và hoàn thiện cấu hình Docker Compose cho toàn bộ hệ thống Ordera.

Yêu cầu thực hiện:
1. Rà soát bảo mật mã nguồn theo nguyên tắc Security Guidelines: Đảm bảo không có API keys, mật khẩu, token hardcode trong code.
2. Kiểm tra cấu hình Nginx trong thư mục infra/ đảm bảo định tuyến đúng /api/* vào Backend port 8080 và /* vào Frontend port 5173/80.
3. Kiểm tra file docker-compose.yml khởi động đầy đủ 5 services: postgres, redis, backend, frontend, nginx.
4. Kiểm tra pipeline GitHub Actions trong .github/workflows/ (backend-ci.yml, frontend-ci.yml).
5. Commit thay đổi hoàn thiện vào nhánh develop theo skill action-commit.
```
