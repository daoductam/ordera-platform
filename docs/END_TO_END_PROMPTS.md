# Ordera Platform — End-to-End Development Prompt Playbook
## Sổ Tay Câu Lệnh Prompt Phát Triển Toàn Diện Từ Đầu Đến Cuối

> **Mục đích:** Tập hợp sẵn các câu lệnh Prompt chuẩn hóa theo từng giai đoạn (Phase) và từng phân hệ tính năng (Feature). Bạn chỉ cần **Copy & Paste** từng câu lệnh vào ô chat với AI Agent để hệ thống tự động thực thi tuần tự từ phân tích, lập kế hoạch, viết code, kiểm thử tự động đến commit git.
>
> **Nguyên tắc kỹ thuật áp dụng xuyên suốt:**
> - **Clean Architecture 4 lớp:** `presentation`, `application`, `domain`, `infrastructure`.
> - **Multi-Tenant Isolation (Bắt buộc):** Mọi truy vấn CSDL đều phải lọc theo `X-Tenant-ID` qua `TenantContextHolder` (ngăn chặn 100% lỗ hổng IDOR).
> - **Sổ cái điểm kép bất biến:** Tuyệt đối không UPDATE trực tiếp cột điểm, mọi biến động đều ghi bản ghi mới vào `points_ledger` kèm `reference_id`.
> - **Chuẩn phong bì API & Lỗi:** Mọi phản hồi bọc trong `ApiResponse<T>`, mã lỗi tuân thủ chuẩn RFC 7807 (`code`, `message`, `details`).
> - **Quy chuẩn commit:** Tuân thủ skill `action-commit` (`develop: ...` kèm trailer `Co-authored-by`).

---

## 📑 Mục Lục Các Giai Đoạn (Execution Roadmap)

- [Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend (Clean Architecture)](#giai-đoạn-1-triển-khai-toàn-diện-8-module-backend)
  - [Prompt 1.1: Phân hệ Cửa Hàng, Chi Nhánh, Bàn Ăn & Thiết Bị Kiosk (Tenant & Store Module)](#prompt-11-phân-hệ-cửa-hàng-chi-nhánh-bàn-ăn--thiết-bị-kiosk-tenant--store-module)
  - [Prompt 1.2: Phân hệ Xác Thực OTP, Định Danh & Cấp Phát JWT (Auth & Identity Module)](#prompt-12-phân-hệ-xác-thực-otp-định-danh--cấp-phát-jwt-auth--identity-module)
  - [Prompt 1.3: Phân hệ Thực Đơn Đa Cấp, Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module)](#prompt-13-phân-hệ-thực-đơn-đa-cấp-tùy-chọn-modifier--banner-khuyến-mãi-catalog-module)
  - [Prompt 1.4: Phân hệ Hồ Sơ Khách Hàng, Động Cơ 6 Hạng Thẻ & Sổ Cái Điểm Bất Biến (Loyalty Module)](#prompt-14-phân-hệ-hồ-sơ-khách-hàng-động-cơ-6-hạng-thẻ--sổ-cái-điểm-bất-biến-loyalty-module)
  - [Prompt 1.5: Phân hệ Ví Voucher, Kho Đổi Thưởng & Drawer Ưu Đãi Giỏ Hàng (Reward Module)](#prompt-15-phân-hệ-ví-voucher-kho-đổi-thưởng--drawer-ưu-đãi-giỏ-hàng-reward-module)
  - [Prompt 1.6: Phân hệ Giỏ Hàng, Thuế VAT 15%, Tips, Checkout & Hóa Đơn E-Receipt (Order Module)](#prompt-16-phân-hệ-giỏ-hàng-thuế-vat-15-tips-checkout--hóa-đơn-e-receipt-order-module)
  - [Prompt 1.7: Phân hệ Trạm Cảm Ứng Kiosk POS, Tích Điểm Nhanh & Watchdog 60s (Kiosk Module)](#prompt-17-phân-hệ-trạm-cảm-ứng-kiosk-pos-tích-điểm-nhanh--watchdog-60s-kiosk-module)
  - [Prompt 1.8: Phân hệ Hộp Thư Đa Danh Mục & Hỗ Trợ Đối Soát Điểm Cũ (Notification & Support Module)](#prompt-18-phân-hệ-hộp-thư-đa-danh-mục--hỗ-trợ-đối-soát-điểm-cũ-notification--support-module)
- [Giai Đoạn 2: Tích Hợp Frontend PWA & Kiosk POS Với Backend API Thực Tế](#giai-đoạn-2-tích-hợp-frontend-pwa--kiosk-pos-với-backend-api-thực-tế)
  - [Prompt 2.1: Tích Hợp HTTP Client Tập Trung, Interceptors & State Contexts (PWA Mobile)](#prompt-21-tích-hợp-http-client-tập-trung-interceptors--state-contexts-pwa-mobile)
  - [Prompt 2.2: Tích Hợp Màn Hình Cảm Ứng Kiosk POS Toàn Màn Hình (Kiosk Touch Terminal)](#prompt-22-tích-hợp-màn-hình-cảm-ứng-kiosk-pos-toàn-màn-hình-kiosk-touch-terminal)
  - [Prompt 2.3: Tích Hợp Toàn Diện Các Modal Nghiệp Vụ (E-Receipt, Missing Points, Offer Drawer)](#prompt-23-tích-hợp-toàn-diện-các-modal-nghiệp-vụ-e-receipt-missing-points-offer-drawer)
- [Giai Đoạn 3: Kiểm Thử Toàn Diện, Bảo Mật, Container Hóa & CI/CD](#giai-đoạn-3-kiểm-thử-toàn-diện-bảo-mật-container-hóa--cicd)
  - [Prompt 3.1: Viết Bộ Test Tích Hợp Testcontainers (PostgreSQL 16 RLS + Redis 7)](#prompt-31-viết-bộ-test-tích-hợp-testcontainers-postgresql-16-rls--redis-7)
  - [Prompt 3.2: Rà Soát Bảo Mật Chống IDOR, Quét Secret & Hoàn Thiện Docker Compose](#prompt-32-rà-soát-bảo-mật-chống-idor-quét-secret--hoàn-thiện-docker-compose)
  - [Prompt 3.3: Kiểm Thử Tự Động Hóa E2E Toàn Trình (End-to-End User Journeys)](#prompt-33-kiểm-thử-tự-động-hóa-e2e-toàn-trình-end-to-end-user-journeys)

---

## Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend

### Prompt 1.1: Phân hệ Cửa Hàng, Chi Nhánh, Bàn Ăn & Thiết Bị Kiosk (Tenant & Store Module)
> **Mục tiêu:** Cung cấp thông tin đại lý thương hiệu, danh sách chi nhánh (Linn Sushi Sentrum), vị trí GPS, danh sách bàn ăn, giải mã QR Token, yêu thích cửa hàng và danh mục thiết bị Kiosk.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Cửa Hàng, Chi Nhánh, Bàn Ăn & Thiết Bị Kiosk (Tenant & Store Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/01-tenant-and-store-api.md (Đầy đủ 7 API: 1.1 Tenant info, 1.2 Danh sách chi nhánh, 1.3 Chi tiết chi nhánh, 1.4 Danh sách bàn, 1.5 Giải mã QR bàn, 1.6 Yêu thích cửa hàng, 1.7 Danh sách cửa hàng yêu thích).
2. DB Design: docs/designs/db-design/01-tenants-and-stores-db.md (Bảng tenants, stores, store_tables, customer_favorite_stores, kiosk_devices).
3. Migration: backend/src/main/resources/db/migration/V1__create_tenants_and_stores.sql & V11__create_kiosk_devices.sql.

Yêu cầu thực hiện:
- Tầng Domain: Pure Java models Tenant, Store, StoreTable, CustomerFavoriteStore, KioskDevice; Domain Exceptions (StoreNotFoundException, TableNotFoundException).
- Tầng Application:
  + GetTenantInfoUseCase: Trả về thương hiệu kèm màu sắc chủ đạo hex (#F5B800) và metadata.
  + GetStoresListUseCase: Tính khoảng cách Haversine theo tọa độ vĩ độ/kinh độ GPS của khách.
  + GetStoreDetailUseCase & GetStoreTablesUseCase: Lấy thông tin điểm bán và các bàn phục vụ ăn tại quán.
  + ResolveTableQrUseCase: Giải mã mã QR token bàn (vd: QR-LINN-SENTRUM-T05).
  + ToggleFavoriteStoreUseCase & GetFavoriteStoresUseCase: Lưu và truy vấn quán yêu thích của hội viên.
- Tầng Infrastructure: JPA Entities (TenantJpaEntity, StoreJpaEntity, StoreTableJpaEntity, CustomerFavoriteStoreJpaEntity, KioskDeviceJpaEntity) và Spring Data Repositories.
- Tầng Presentation: TenantController, StoreController với đầy đủ DTOs, bọc ApiResponse<T> và phản hồi lỗi chuẩn RFC 7807 (400 TENANT_HEADER_MISSING, 404 STORE_NOT_FOUND).
- Kiểm thử: Viết Unit tests AAA (Arrange - Act - Assert) cho UseCases và Web Layer.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.2: Phân hệ Xác Thực OTP, Định Danh & Cấp Phát JWT (Auth & Identity Module)
> **Mục tiêu:** Xây dựng luồng xác thực đăng nhập/đăng ký hội viên qua SMS/Email OTP (cooldown 120s, tối đa 5 lần thử trên Redis), quản lý ngày sinh `dateOfBirth`, cấp phát cặp Access Token (15m) + Refresh Token (7d) và thu hồi token vào Redis Blacklist khi đăng xuất.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Xác Thực OTP & Quản Lý Danh Tính (Auth & Identity Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/03-auth-and-identity-api.md (Đầy đủ 5 API: 3.1 Gửi OTP, 3.2 Xác thực OTP, 3.3 Đăng ký hội viên, 3.4 Làm mới JWT, 3.5 Đăng xuất).
2. DB Design: docs/designs/db-design/02-users-and-identity-db.md (Bảng users, otp_audit_logs).
3. Migration: backend/src/main/resources/db/migration/V2__create_users_and_identity.sql.

Yêu cầu thực hiện:
- Tầng Domain: User, UserRole (CUSTOMER, STAFF, ADMIN), OtpSession, TokenPair.
- Tầng Application:
  + SendOtpUseCase: Kiểm tra cooldown 120s (`otp:cooldown:{phone}`), sinh mã 6 chữ số ngẫu nhiên, lưu băm SHA-256 vào Redis với TTL 120s (`otp:code:{phone}`), ghi nhận vào otp_audit_logs.
  + VerifyOtpUseCase: Theo dõi số lần nhập sai (`otp:attempts:{phone}`, tối đa 5 lần thì khóa 15 phút), kiểm tra khớp mã OTP, sinh temporary session token nếu là khách mới.
  + RegisterMemberUseCase: Tiếp nhận họ tên (David Nguyen), SĐT, Email và ngày sinh dateOfBirth, tạo tài khoản users và tự động tạo hồ sơ member_profiles (hạng New Member, 0 điểm).
  + TokenManagementService: Cấp phát HMAC-SHA512 JWT Access Token (15 phút) và Refresh Token (7 ngày). Khi gọi POST /api/v1/auth/logout, đưa cả 2 token vào Redis Blacklist (`blacklist:token:{jti}`).
- Tầng Presentation: AuthController với phản hồi lỗi RFC 7807 (OTP_COOLDOWN_ACTIVE, OTP_INVALID, OTP_EXPIRED, OTP_MAX_ATTEMPTS_EXCEEDED, PHONE_ALREADY_EXISTS).
- Kiểm thử: Unit tests bao phủ đầy đủ luồng thành công, sai OTP, hết hạn OTP, thử quá 5 lần, và refresh token hết hạn.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.3: Phân hệ Thực Đơn Đa Cấp, Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module)
> **Mục tiêu:** Cung cấp danh mục thực đơn chính, danh mục con (Subcategories), cây thực đơn phân cấp, chi tiết món ăn (Aurora 89 Kr, Tuna Maki) kèm nhãn ăn kiêng `dietaryTags`, số lượng miếng `piecesText`, giá gốc `originalPrice`, các nhóm modifier (Size L, đá, đường, topping phụ thu) và banner khuyến mãi trang chủ.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Thực Đơn, Món Ăn, Nhóm Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/02-catalog-and-menu-api.md (Đầy đủ 6 API: 2.1 Danh mục chính, 2.2 Danh mục con, 2.3 Cây thực đơn phân cấp, 2.4 Chi tiết món ăn, 2.5 Nhóm Modifiers, 2.6 Banner khuyến mãi).
2. DB Design: docs/designs/db-design/04-catalog-and-menu-db.md (Bảng categories, subcategories, products, product_modifier_groups, product_modifiers, promotion_banners).
3. Migration: backend/src/main/resources/db/migration/V5__create_catalog_and_modifiers.sql.

Yêu cầu thực hiện:
- Tầng Domain: Category, Subcategory, Product, ModifierGroup, Modifier, PromotionBanner.
- Tầng Application:
  + GetCategoriesUseCase & GetSubcategoriesUseCase: Lấy danh mục món phân chia theo vị trí hiển thị và chi nhánh.
  + GetStoreMenuUseCase: Trả về cây thực đơn phân cấp hoàn chỉnh (Categories -> Subcategories -> Products khả dụng).
  + GetProductDetailUseCase: Lấy chi tiết món kèm originalPrice, piecesText, dietaryTags (GLUTEN_FREE, VEGAN, HALAL) và các nhóm modifier.
  + GetPromotionBannersUseCase: Lấy danh sách banner quảng cáo trang chủ (Hot Deals, Seasonal).
- Tầng Infrastructure: Đảm bảo lọc dữ liệu theo Tenant RLS và cờ is_available = true.
- Tầng Presentation: CategoryController, ProductController, PromotionController chuẩn Envelope ApiResponse<T> và mã lỗi RFC 7807 (CATEGORY_NOT_FOUND, PRODUCT_NOT_FOUND).
- Kiểm thử: Unit tests kiểm tra tính đúng đắn của dữ liệu trả về và nhóm tùy chọn modifier.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.4: Phân hệ Hồ Sơ Khách Hàng, Động Cơ 6 Hạng Thẻ & Sổ Cái Điểm Bất Biến (Loyalty Module)
> **Mục tiêu:** Quản lý hồ sơ David Nguyen (hạng Silver, 120 điểm, cờ `hasMissingPointsNotice`), mã vạch tích điểm nhanh `barcodeToken`, cập nhật thông tin cá nhân/ảnh đại diện, định nghĩa 6 hạng thẻ (`New` -> `Silver` -> `Gold` -> `Platinum` -> `Diamond` -> `Ruby`), lịch sử sổ cái phân trang từ bảng phân vùng `points_ledger` và tiếp nhận khiếu nại thiếu điểm hóa đơn trong 30 ngày.

```markdown
/feature Hãy triển khai tính năng: Động Cơ Thẻ Thành Viên & Sổ Cái Điểm Bất Biến (Loyalty & Points Ledger Engine) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/04-loyalty-and-tier-api.md (Đầy đủ 7 API: 4.1 Xem hồ sơ, 4.2 Cập nhật thông tin, 4.3 Đổi SĐT/Email OTP, 4.4 Tải lên avatar, 4.5 Danh sách 6 hạng thẻ, 4.6 Lịch sử sổ cái điểm, 4.7 Khiếu nại thiếu điểm hóa đơn).
2. DB Design: docs/designs/db-design/03-loyalty-and-points-ledger-db.md (Bảng tier_definitions, member_profiles, points_ledger phân vùng RANGE theo năm, support_claims).
3. Migration: backend/src/main/resources/db/migration/V3__create_loyalty_and_tiers.sql, V4__create_points_ledger.sql.

Yêu cầu thực hiện:
- Tầng Domain: MemberProfile, MemberTier (New, Silver, Gold, Platinum, Diamond, Ruby), TransactionType (ORDER_EARN, VOUCHER_REDEEM, GIFT_REDEEM, TIER_BONUS, CLAIM_CREDIT, KIOSK_EARN), SupportClaim.
- Tầng Application:
  + GetMemberProfileUseCase: Trả về tên, hạng thẻ, currentPoints, lifetimePoints, barcodeToken, nextTier, progressPercentage, hasMissingPointsNotice.
  + UpdateProfileUseCase & UploadAvatarUseCase: Cập nhật thông tin cá nhân và đường dẫn ảnh đại diện.
  + AccruePointsUseCase: Ghi sổ cái bất biến points_ledger, tự động kiểm tra mốc thăng hạng và nâng bậc thẻ nếu đủ điều kiện (Silver 100, Gold 400, Platinum 1000, Diamond 2000, Ruby 4000).
  + GetPointsLedgerHistoryUseCase: Truy vấn dữ liệu phân trang từ bảng phân vùng points_ledger theo thời gian giảm dần.
  + SubmitSupportClaimUseCase: Tiếp nhận mã hóa đơn (vd: RCP-8921-X), storeId, số tiền và ảnh chụp biên lai trong vòng 30 ngày, tạo support_claims (PENDING).
- NGUYÊN TẮC BẤT BIẾN: Tuyệt đối không UPDATE trực tiếp cột điểm. Mọi biến động đều phải chèn dòng mới vào points_ledger.
- Tầng Presentation: MemberLoyaltyController, SupportClaimController với mã lỗi RFC 7807 (CLAIM_ALREADY_EXISTS, RECEIPT_EXPIRED).
- Kiểm thử: Unit tests kiểm tra kỹ công thức tính điểm, nâng hạng tự động và kiểm tra tính bất biến của sổ cái.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.5: Phân hệ Ví Voucher, Kho Đổi Thưởng & Drawer Ưu Đãi Giỏ Hàng (Reward Module)
> **Mục tiêu:** Quản lý ví cá nhân `My Rewards`, danh mục đổi thưởng `Get More Rewards` (Voucher 100 NOK, -30%, Cốc Tumbler), khóa phân tán Redisson chống race condition đổi quà, và toàn bộ 6 API phục vụ Drawer chọn ưu đãi trực tiếp trong luồng Checkout (gán/gỡ voucher, gán/gỡ quà hiện vật).

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Ví Voucher, Đổi Thưởng & Ưu Đãi Giỏ Hàng (Reward & Voucher Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/05-reward-and-voucher-api.md (Đầy đủ 11 API: 5.1 Ví My Rewards, 5.2 Kho đổi thưởng, 5.3 Đổi voucher, 5.4 Đổi cốc Tumbler, 5.5 Tổng quan ưu đãi, 5.6 Ưu đãi đủ điều kiện cho giỏ, 5.7 Validate mã promo nhập tay, 5.8 Áp dụng voucher, 5.9 Gỡ voucher, 5.10 Áp dụng quà hiện vật, 5.11 Gỡ quà hiện vật).
2. DB Design: docs/designs/db-design/05-rewards-and-vouchers-db.md (Bảng vouchers, user_vouchers, loyalty_gifts, user_gift_redemptions).
3. Migration: backend/src/main/resources/db/migration/V6__create_vouchers_and_rewards.sql.

Yêu cầu thực hiện:
- Tầng Domain: Voucher, UserVoucher (AVAILABLE, IN_USE, USED, EXPIRED), LoyaltyGift, UserGiftRedemption (PENDING, SELECTED, CLAIMED).
- Tầng Application:
  + RedeemVoucherUseCase: Kiểm tra số dư điểm, chiếm giữ Redisson Lock `lock:member:{id}`, trừ điểm khả dụng qua AccruePointsUseCase (VOUCHER_REDEEM), tạo bản ghi user_vouchers.
  + RedeemPhysicalGiftUseCase: Chiếm giữ đồng thời Redisson multi-lock `lock:member:{id}` và `lock:gift:{giftId}`, kiểm tra tồn kho stock_quantity > 0, trừ 1 tồn kho, sinh mã nhận quà 8 ký tự (redemption_code), tạo user_gift_redemptions.
  + GetAvailableOffersForCartUseCase: Lọc các voucher trong ví thỏa mãn giá trị đơn hàng tối thiểu min_order_value và quà tặng có thể chọn kèm.
  + ApplyVoucherToCartUseCase & RemoveVoucherFromCartUseCase: Khóa voucher sang IN_USE khi áp vào giỏ hàng và hoàn trả sang AVAILABLE nếu khách gỡ bỏ.
  + ApplyGiftToCartUseCase & RemoveGiftFromCartUseCase: Gán/gỡ quà hiện vật đi kèm đơn hàng checkout.
- QUY TẮC KẾT HỢP: Trong một đơn hàng cho phép kết hợp tối đa 01 Voucher VÀ 01 Quà hiện vật (Cốc Tumbler).
- Tầng Presentation: RewardController, CartOfferController với mã lỗi RFC 7807 (POINTS_INSUFFICIENT, GIFT_OUT_OF_STOCK, PROMO_CODE_NOT_FOUND, MIN_ORDER_NOT_MET).
- Kiểm thử: Unit tests cho kịch bản đủ điểm, thiếu điểm, hết tồn kho quà và race condition đồng thời.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.6: Phân hệ Giỏ Hàng, Thuế VAT 15%, Tips, Checkout & Hóa Đơn E-Receipt (Order Module)
> **Mục tiêu:** Quản lý giỏ hàng trên Redis Cache (TTL 7 ngày), thêm món kèm modifier, cập nhật số lượng/ghi chú, tính toán dự toán Checkout gồm thuế VAT 15% Na Uy (công thức $Total \times \frac{15}{115}$), tiền tip, xử lý đặt đơn qua Idempotency-Key và Redisson Lock, sinh số thứ tự nhận món `#005`, lưu `applied_gift_id`, tiến trình chuẩn bị món và xuất hóa đơn điện tử E-Receipt gửi Email PDF.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Giỏ Hàng, Đặt Món, Tính Thuế VAT, Tips, Checkout & Hóa Đơn E-Receipt (Order Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/06-order-and-checkout-api.md (Đầy đủ 10 API: 6.1 Lấy giỏ hàng, 6.2 Thêm món vào giỏ, 6.3 Sửa/Xóa món trong giỏ, 6.4 Ghi chú dị ứng, 6.5 Dự toán Checkout, 6.6 Chốt đơn & thanh toán, 6.7 Danh sách đơn hàng Active/Past, 6.8 Chi tiết đơn & Timeline bếp, 6.9 Đặt lại đơn cũ 1-click Reorder, 6.10 Gửi E-Receipt qua Email).
2. DB Design: docs/designs/db-design/06-orders-and-checkout-db.md (Bảng orders, order_items, order_item_modifiers, carts, cart_items).
3. Migration: backend/src/main/resources/db/migration/V7__create_orders_and_checkout.sql.

Yêu cầu thực hiện:
- Tầng Domain: Cart, CartItem, Order, OrderItem, OrderItemModifier, OrderStatus (PENDING, PREPARING, READY, COMPLETED, CANCELLED), PaymentStatus, PickupMethod (COUNTER, TABLE).
- Tầng Application:
  + CartService: Quản lý giỏ hàng trên Redis Cache key `cart:{tenantId}:{cartId}` (TTL 7 ngày). Thêm món kèm danh sách modifier đã chọn, cập nhật số lượng và ghi chú dị ứng tối đa 200 ký tự.
  + PreviewCheckoutCalculationUseCase: Tính toán chính xác Subtotal, Discount, Tip, VAT 15% ($Total \times 15 / 115$), Total và số điểm thưởng dự kiến: `pointsToEarn = floor((subtotal - discount) * tierRate)`. Tiền tip không được tính vào điểm thưởng.
  + CheckoutOrderUseCase:
    * Kiểm tra Idempotency-Key trên Redis (TTL 24h) chống thanh toán trùng lặp.
    * Chiếm giữ Redisson Lock `lock:order:checkout:{userId}`.
    * Tạo đơn hàng orders với order_number (#005), order_code (ORD-20261105-0891), applied_gift_id.
    * Chuyển user_vouchers sang USED.
    * Tự động gọi AccruePointsUseCase ghi sổ cái points_ledger (ORDER_EARN).
    * Xóa giỏ hàng khỏi Redis sau khi tạo đơn thành công.
  + ReorderPastOrderUseCase: Sao chép các món từ đơn hàng cũ vào giỏ hàng mới.
  + SendEReceiptEmailUseCase: Sinh hóa đơn điện tử PDF và gửi tới địa chỉ Email của khách qua JavaMailSender.
- Tầng Presentation: CartController, OrderController với đầy đủ mã lỗi RFC 7807 (CART_EMPTY, MIN_ORDER_VALUE_NOT_MET, IDEMPOTENT_OPERATION_CONFLICT, STOCK_UNAVAILABLE).
- Kiểm thử: Unit tests kiểm tra độ chính xác số học BigDecimal (scale = 2), công thức VAT 15%, chiết khấu voucher, và logic tích điểm.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.7: Phân hệ Trạm Cảm Ứng Kiosk POS, Tích Điểm Nhanh & Watchdog 60s (Kiosk Module)
> **Mục tiêu:** Cung cấp API chuyên dụng cho màn hình Kiosk cảm ứng toàn màn hình tại quầy thu ngân: nhận diện danh tính khách qua SĐT, tích điểm nhanh cho đơn giấy không cần đặt món trên máy tính bảng (`KIOSK_EARN`), tự đặt đơn tại Kiosk xuất phiếu số thứ tự `#A-08`, tự động reset phiên 60s idle timeout và giám sát phần cứng máy in/máy quẹt thẻ.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Kiosk POS & Tích Điểm Nhanh Tại Quầy (Kiosk Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/07-kiosk-pos-api.md (Đầy đủ 5 API: 7.1 Nhận diện SĐT, 7.2 Tích điểm nhanh, 7.3 Đặt đơn Kiosk, 7.4 Reset phiên, 7.5 Cấu hình thiết bị trạm).
2. DB Design: docs/designs/db-design/01-tenants-and-stores-db.md (Bảng kiosk_devices).
3. Migration: backend/src/main/resources/db/migration/V11__create_kiosk_devices.sql.
4. Frontend tham chiếu: frontend/src/pages/KioskPage.tsx.

Yêu cầu thực hiện:
- Tầng Domain: KioskSession, KioskDevice (printer_status, card_reader_status, idle_timeout_seconds).
- Tầng Application:
  + KioskIdentifyCustomerUseCase: Nhận diện SĐT từ bàn phím số Kiosk, tra cứu tên, hạng thẻ, số điểm hiện có và tỷ lệ hoàn điểm. Nếu khách mới trả về isRegistered = false để Kiosk cho phép bấm Skip.
  + KioskQuickEarnUseCase: Dùng khi khách thanh toán POS ngoài, khách gõ SĐT tích điểm -> tính điểm theo hạng thẻ, ghi sổ cái points_ledger (TransactionType = KIOSK_EARN), sinh số thứ tự queueNumber (#A-08) và gửi lệnh in nhiệt.
  + ResetKioskSessionUseCase: Xóa session tạm thời trên Redis khi khách bấm Start Over hoặc bộ đếm thời gian 60 giây hết hạn do khách bỏ đi.
  + GetKioskConfigUseCase: Cung cấp thông tin cấu hình trạm Kiosk theo deviceId và ghi nhận last_heartbeat_at.
- Tầng Presentation: KioskController với header bắt buộc X-Tenant-ID, X-Kiosk-Device-ID và mã lỗi RFC 7807 (INVALID_PHONE_NUMBER, KIOSK_DEVICE_NOT_FOUND, INVALID_STAFF_PIN).
- Kiểm thử: Unit tests cho luồng tra cứu khách hàng, tích điểm nhanh và reset phiên làm việc.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.8: Phân hệ Hộp Thư Đa Danh Mục & Hỗ Trợ Đối Soát Điểm Cũ (Notification & Support Module)
> **Mục tiêu:** Quản lý hộp thư thông báo đa danh mục ("Messages" & "News"), huy hiệu số lượng chưa đọc, đánh dấu đã đọc, và tiếp nhận phiếu yêu cầu hỗ trợ đối soát điểm cũ bị thất lạc sau khi nâng cấp hệ thống (màn hình `05.03 Missing Your Points?`).

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Hộp Thư Thông Báo & Hỗ Trợ Khách Hàng (Notification & Support Module) cho Backend.

Tài liệu kỹ thuật bắt buộc:
1. API Design: docs/designs/api-design/08-notification-api.md (Đầy đủ 6 API: 8.1 Đếm chưa đọc, 8.2 Danh sách thông báo đa danh mục, 8.3 Chi tiết thông báo, 8.4 Đánh dấu 1 tin đã đọc, 8.5 Đánh dấu tất cả đã đọc, 8.6 Gửi phiếu hỗ trợ thiếu điểm).
2. DB Design: docs/designs/db-design/08-notifications-and-support-db.md (Bảng system_notifications, user_notifications, support_inquiries).
3. Migration: backend/src/main/resources/db/migration/V12__create_notifications_and_support.sql.

Yêu cầu thực hiện:
- Tầng Domain: SystemNotification (Messages, News, Voucher, Loyalty Gifts), UserNotification, SupportInquiry (PENDING, IN_REVIEW, RESOLVED, REJECTED).
- Tầng Application:
  + GetUnreadNotificationCountUseCase: Đếm số lượng thông báo chưa đọc của hội viên hiển thị badge đỏ trên icon quả chuông.
  + GetNotificationsListUseCase: Lấy danh sách thông báo phân trang theo tab Messages hoặc News.
  + GetNotificationDetailUseCase: Lấy chi tiết bài viết, banner ảnh và tự động đánh dấu đã đọc.
  + MarkNotificationReadUseCase & MarkAllNotificationsReadUseCase: Cập nhật trạng thái đọc của người dùng trong bảng user_notifications.
  + SubmitMissingPointsInquiryUseCase: Tiếp nhận phiếu yêu cầu tra cứu điểm cũ (Email, SĐT, mã hóa đơn cũ, ghi chú và ảnh chứng từ), sinh ticket_code (INQ-20250317-009) để Admin đối soát cơ sở dữ liệu cũ.
- Tầng Presentation: NotificationController, SupportInquiryController với mã lỗi RFC 7807 (NOTIFICATION_NOT_FOUND, VALIDATION_FAILED).
- Kiểm thử: Unit tests cho các UseCases thông báo và gửi phiếu hỗ trợ.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

## Giai Đoạn 2: Tích Hợp Frontend PWA & Kiosk POS Với Backend API Thực Tế

### Prompt 2.1: Tích Hợp HTTP Client Tập Trung, Interceptors & State Contexts (PWA Mobile)
> **Mục tiêu:** Thay thế dữ liệu mock trong Frontend React bằng các lệnh gọi REST API thực tế tới Backend, duy trì trạng thái giỏ hàng, token đăng nhập và xử lý tự động refresh token.

```markdown
/feature Hãy thực hiện: Kết nối Frontend React 19 với Backend REST API thực tế (Full PWA Mobile API Integration).

Tài liệu tham chiếu:
- docs/designs/api-design/README.md (Chuẩn Envelope ApiResponse<T> và Headers).
- docs/designs/api-design/01-tenant-and-store-api.md đến 06-order-and-checkout-api.md.
- frontend/src/types/ và frontend/src/context/.

Yêu cầu thực hiện:
1. Tạo HTTP Client tập trung (Axios hoặc Fetch wrapper tại frontend/src/api/client.ts):
   - Tự động gắn header `X-Tenant-ID` từ tenant cấu hình hiện tại.
   - Tự động gắn header `Authorization: Bearer <accessToken>` nếu người dùng đã đăng nhập.
   - Đăng ký response interceptor: Nếu nhận mã lỗi 401 UNAUTHORIZED hoặc TOKEN_EXPIRED, tự động gọi POST /api/v1/auth/token/refresh với refreshToken để cấp lại token mới và phát lại request cũ mà không bắt người dùng đăng nhập lại.
2. Nối API cho AuthContext:
   - Đăng nhập/Đăng ký qua POST /api/v1/auth/otp/send và POST /api/v1/auth/otp/verify.
   - Đăng xuất qua POST /api/v1/auth/logout.
3. Nối API cho CartContext:
   - Thêm món qua POST /api/v1/cart/items kèm modifier.
   - Sửa/Xóa món qua PATCH/DELETE /api/v1/cart/items/{id}.
   - Tính toán dự toán đơn hàng qua POST /api/v1/orders/checkout/preview.
4. Nối API cho các trang: StoreDiscoveryPage, HomePage (Menu & Banner), RewardsPage (Voucher & Quà tặng), ProfilePage (Hồ sơ & 6 Hạng thẻ).
5. Kiểm tra build frontend bằng lệnh `npm.cmd run build` đảm bảo không có lỗi kiểu TypeScript.
6. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 2.2: Tích Hợp Màn Hình Cảm Ứng Kiosk POS Toàn Màn Hình (Kiosk Touch Terminal)
> **Mục tiêu:** Kết nối giao diện màn hình cảm ứng Kiosk POS (`KioskPage.tsx`) với Backend API thực tế, xử lý bàn phím số Touch Numpad, tích điểm nhanh và bộ đếm watchdog 60 giây.

```markdown
/feature Hãy thực hiện: Kết nối giao diện Kiosk POS (KioskPage.tsx) với Backend Kiosk API thực tế.

Tài liệu tham chiếu:
- docs/designs/api-design/07-kiosk-pos-api.md.
- frontend/src/pages/KioskPage.tsx.

Yêu cầu thực hiện:
1. Khi Kiosk khởi động, gọi GET /api/v1/kiosk/config/{deviceId} để tải thông tin chi nhánh và thời gian timeout cấu hình.
2. Thay thế mock handleLookup bằng lời gọi POST /api/v1/kiosk/identify với số điện thoại khách vừa gõ trên bàn phím cảm ứng. Hiển thị đúng thông tin thành viên (Tên, Hạng thẻ, Điểm khả dụng). Nếu isRegistered = false, hiển thị nút Skip để khách tích điểm mới.
3. Thay thế handleCompletePayment bằng lời gọi POST /api/v1/kiosk/quick-earn để ghi sổ cái tích điểm KIOSK_EARN và hiển thị số thứ tự hàng đợi thực tế (#A-08) kèm số điểm vừa cộng.
4. Đồng hồ đếm ngược 60s Watchdog: Mỗi thao tác chạm phím cảm ứng sẽ reset lại 60 giây. Khi hết hạn đếm ngược hoặc khách bấm Start Over, gọi POST /api/v1/kiosk/session/reset để xóa sạch session cache và trở về màn hình chờ ban đầu.
5. Sau khi thanh toán thành công, đếm ngược 5 giây rồi tự động gọi handleStartOver.
6. Kiểm tra build frontend bằng `npm.cmd run build`.
7. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 2.3: Tích Hợp Toàn Diện Các Modal Nghiệp Vụ (E-Receipt, Missing Points, Offer Drawer)
> **Mục tiêu:** Kết nối hoàn chỉnh các Popup / Modal tương tác sâu theo đúng Figma Prototype: E-Receipt gửi Email PDF, Modal khiếu nại điểm thiếu "Missing Your Points?", và Drawer chọn voucher/quà tặng trong màn hình Checkout.

```markdown
/feature Hãy thực hiện: Tích hợp Backend API cho các Modal và Drawer nghiệp vụ trên Frontend.

Tài liệu tham chiếu:
- docs/designs/api-design/05-reward-and-voucher-api.md (API 5.6 đến 5.11 - Drawer ưu đãi).
- docs/designs/api-design/06-order-and-checkout-api.md (API 6.10 - Gửi E-Receipt).
- docs/designs/api-design/08-notification-api.md (API 8.6 - Submit phiếu hỗ trợ điểm).
- Prototype: docs/prototype/6-gio-hang-don-hang-va-bien-nhan/ (06.08, 06.09, 06.10) và docs/prototype/8-chon-voucher-khi-checkout/ (08.01 đến 08.12).

Yêu cầu thực hiện:
1. VoucherSelectDrawer.tsx (Màn hình 08.01 - 08.12):
   - Gọi GET /api/v1/cart/offers để lấy danh sách voucher và quà hiện vật đủ điều kiện cho giỏ hàng hiện tại.
   - Gọi POST /api/v1/cart/promo/validate khi khách nhập tay mã khuyến mãi.
   - Gọi POST /api/v1/cart/voucher/apply và DELETE /api/v1/cart/voucher/remove khi khách chọn/bỏ voucher.
   - Gọi POST /api/v1/cart/gift/apply và DELETE /api/v1/cart/gift/remove khi khách chọn/bỏ cốc Tumbler.
2. EReceiptModal.tsx (Màn hình 06.08 - 06.10):
   - Nhập email và gọi POST /api/v1/orders/{orderId}/receipt/send-email.
   - Chuyển sang màn hình xác nhận "Email sent. The receipt is sent to the email" (06.10).
3. NotificationDetailModal.tsx & MissingPointsBanner.tsx (Màn hình 05.03):
   - Mở modal chi tiết thông báo "Missing Your Points?".
   - Khi bấm [Submit Form], gửi biểu mẫu qua POST /api/v1/support/missing-points/submit và hiển thị thông báo tiếp nhận thành công.
4. Kiểm tra build frontend bằng `npm.cmd run build`.
5. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

## Giai Đoạn 3: Kiểm Thử Toàn Diện, Bảo Mật, Container Hóa & CI/CD

### Prompt 3.1: Viết Bộ Test Tích Hợp Testcontainers (PostgreSQL 16 RLS + Redis 7)
> **Mục tiêu:** Đảm bảo toàn bộ hệ thống hoạt động hoàn hảo trên môi trường cơ sở dữ liệu và cache thật sử dụng Testcontainers, kiểm chứng chính sách phân lập Row-Level Security (RLS) và tính toàn vẹn của sổ cái điểm.

```markdown
/feature Hãy viết bộ kiểm thử tích hợp (Integration Tests) cho Backend sử dụng Testcontainers.

Yêu cầu thực hiện:
1. Cấu hình Testcontainers khởi động container PostgreSQL 16 và Redis 7 thật trong môi trường kiểm thử Spring Boot (`@SpringBootTest`).
2. Viết Integration Test cho luồng Checkout đặt món toàn trình:
   - Khởi tạo giỏ hàng -> Thêm món kèm modifier -> Áp mã voucher -> Khóa phân tán Redisson -> Trừ điểm -> Chốt đơn thành công -> Kiểm tra dữ liệu được ghi chuẩn xác vào bảng orders, order_items, và points_ledger.
3. Viết Integration Test cho bảo mật đa thuê (Multi-Tenant Row-Level Security RLS):
   - Tạo 2 tenants: Tenant A và Tenant B.
   - Đăng nhập với tư cách người dùng Tenant A, thực hiện truy vấn các bảng orders, member_profiles, vouchers.
   - Khẳng định (Assert) 100% không thể nhìn thấy hoặc thao tác dữ liệu thuộc Tenant B ngay cả khi truyền thẳng UUID của Tenant B (Ngăn chặn triệt để lỗ hổng IDOR).
4. Viết Integration Test cho tính bất biến của sổ cái:
   - Cố tình chạy lệnh UPDATE trên bảng points_ledger và xác nhận CSDL hoặc Hibernate ném ngoại lệ cấm cập nhật.
5. Chạy toàn bộ test suite bằng lệnh `mvn clean test` đảm bảo 100% test cases đều pass (BUILD SUCCESS).
6. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 3.2: Rà Soát Bảo Mật Chống IDOR, Quét Secret & Hoàn Thiện Docker Compose
> **Mục tiêu:** Rà soát bảo mật toàn diện theo Security Guidelines, đảm bảo không có API key / token hardcode, cấu hình Nginx reverse proxy an toàn và hoàn thiện docker-compose sẵn sàng khởi chạy.

```markdown
/feature Hãy rà soát bảo mật toàn diện và hoàn thiện cấu hình Docker Compose cho toàn bộ hệ thống Ordera.

Yêu cầu thực hiện:
1. Rà soát bảo mật mã nguồn theo nguyên tắc Security Guidelines:
   - Đảm bảo không có secret, mật khẩu DB, khóa bí mật JWT bị hardcode trong source code; toàn bộ phải đọc qua biến môi trường hoặc application.yml chuẩn hóa.
   - Xác thực 100% các endpoint nghiệp vụ đều kiểm tra quyền sở hữu tài nguyên thông qua TenantContextHolder.
2. Kiểm tra cấu hình Nginx trong thư mục infra/:
   - Đảm bảo reverse proxy định tuyến chính xác `/api/*` vào Backend (port 8080) và `/*` vào Frontend (port 5173/80).
   - Thiết lập các header bảo mật: X-Frame-Options, X-Content-Type-Options, Content-Security-Policy.
3. Kiểm tra file docker-compose.yml khởi động đầy đủ 5 services: postgres, redis, backend, frontend, nginx.
4. Kiểm tra pipeline GitHub Actions trong .github/workflows/ (backend-ci.yml, frontend-ci.yml) đảm bảo tự động build và chạy test khi push lên nhánh develop và main.
5. Commit thay đổi hoàn thiện vào nhánh develop theo skill action-commit.
```

---

### Prompt 3.3: Kiểm Thử Tự Động Hóa E2E Toàn Trình (End-to-End User Journeys)
> **Mục tiêu:** Viết kịch bản kiểm thử tự động hóa E2E (Playwright hoặc Cypress) mô phỏng chính xác hành vi của khách hàng từ lúc vào web đến khi nhận hóa đơn điện tử E-Receipt.

```markdown
/feature Hãy viết kịch bản kiểm thử tự động hóa toàn trình (E2E Tests) cho ứng dụng Ordera.

Yêu cầu kịch bản kiểm thử:
1. Luồng 1 - Khách hàng di động (Mobile Web PWA Flow):
   - Mở trang chủ -> Khám phá cửa hàng Linn Sushi Sentrum.
   - Đăng nhập qua số điện thoại OTP (+4797362748) -> Nhận diện thành công David Nguyen (Silver Member).
   - Chọn món Aurora (Size L, Topping Mango Popping) -> Thêm vào giỏ.
   - Mở giỏ hàng -> Mở Drawer chọn Voucher giảm giá 30% và Quà tặng Cốc Tumbler -> Áp dụng thành công.
   - Kiểm tra bảng tính tiền: Đúng tiền thuế VAT 15%, tiền tip, số tiền thanh toán thực tế và điểm dự kiến (+170 pts).
   - Bấm nút Pay -> Nhận mã số nhận món #005 và timeline chuẩn bị món của bếp.
   - Bấm gửi E-Receipt -> Nhập email info@ordera.no -> Hiển thị popup Email sent thành công.
2. Luồng 2 - Trạm Kiosk POS tại quầy (Kiosk Flow):
   - Mở màn hình Kiosk toàn màn hình -> Gõ số điện thoại trên bàn phím cảm ứng -> Hiển thị đúng hạng thẻ Gold Member.
   - Bấm tích điểm nhanh -> Xuất số thứ tự hàng đợi #A-08.
   - Đợi 60 giây idle hoặc bấm nút Start Over -> Xác nhận Kiosk tự động reset về màn hình chào ban đầu.
3. Chạy kiểm thử E2E và đảm bảo toàn bộ kịch bản đều Passed.
4. Commit thay đổi vào nhánh develop theo skill action-commit.
```
