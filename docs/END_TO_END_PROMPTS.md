# Ordera Platform — End-to-End Development Prompt Playbook
## Sổ Tay Câu Lệnh Prompt Phát Triển Toàn Diện Từ Đầu Đến Cuối

> **Mục đích:** Tập hợp sẵn các câu lệnh Prompt chuẩn hóa theo từng giai đoạn (Phase) và từng phân hệ tính năng (Feature). Bạn chỉ cần **Copy & Paste** từng câu lệnh vào ô chat với AI Agent để hệ thống tự động thực thi tuần tự từ phân tích, lập kế hoạch, viết code, kiểm thử tự động đến commit git.
>
> **10 Trụ Cột Kiến Trúc Kỹ Thuật Áp Dụng Xuyên Suốt (Bắt buộc tuân thủ):**
> 1. **Kiến Trúc Doanh Nghiệp Toàn Diện (Enterprise Blueprint):** Tuân thủ tuyệt đối [docs/designs/architecture/enterprise-architecture.md](designs/architecture/enterprise-architecture.md) theo mô hình Modular Monolith phân tách rõ ràng 4 tiers: Client Tier, Edge Gateway Tier, Backend Modular Monolith Tier, và Data Tier.
> 2. **Clean Architecture 4 Lớp Tách Biệt Tuyệt Đối:**
>    - `domain` (`com.ordera.modules.<module>.domain`): Pure Java POJO chứa Entities, Value Objects, Domain Enums, Domain Rules, và **Domain Constants** (`domain/constant/<Module>Constants.java`). Hoàn toàn độc lập, không phụ thuộc Framework, Spring Context hay JPA/Database.
>    - `application` (`com.ordera.modules.<module>.application`): UseCase Interfaces & Implementations (`usecase/`, `usecase/impl/`), Domain Services (`service/`), Điều phối giao dịch ACID (`@Transactional`), và DTO Mapping contracts.
>    - `infrastructure` (`com.ordera.modules.<module>.infrastructure`): JPA Hibernate Entities (`*JpaEntity.java`), Spring Data Repositories, Redisson Distributed Locks, Adapters tích hợp bên thứ ba (SMS, Mail), MapStruct Mappers (`*DtoMapper.java`).
>    - `presentation` (`com.ordera.modules.<module>.presentation`): REST Controllers (`*Controller.java`), Request/Response DTOs, Bean Validation (`@Valid`, `@NotNull`, `@Pattern`, `@DecimalMin`), Chuẩn bọc `ApiResponse<T>`, mã lỗi Problem Details RFC 7807, và **Presentation Messages** (`presentation/constant/<Module>Messages.java`).
> 3. **Quy Chuẩn Quản Lý Hằng Số Tập Trung & Tách Biệt 2 Lớp (Constants vs Messages Separation):**
>    - **Tuyệt đối KHÔNG hardcode:** Nghiêm cấm magic numbers (15, 60, 120, 5242880, 20...) và magic strings (`"popular"`, `"image/png"`, `"lock:..."`, `"ORD-"`, regex, status...) rải rác trong code logic.
>    - **Domain Constants (`domain/constant/<Module>Constants.java`):** Chứa các hằng số quy tắc nghiệp vụ bất biến: giới hạn dung lượng upload, timeout, TTL cache, regex format, phân trang mặc định, ngưỡng thăng hạng, lock key pattern, cờ trạng thái. Không được chứa chuỗi thông điệp giao diện.
>    - **Presentation Messages (`presentation/constant/<Module>Messages.java`):** Chứa toàn bộ chuỗi thông điệp phong bì API (`ApiResponse.ok(MSG_*, data)`), thông báo thành công và mô tả lỗi API (`ERR_*`). Phân tách triệt để khỏi Domain Layer nhằm phục vụ đa ngôn ngữ I18n trong tương lai và giữ Domain Layer thuần khiết (pure).
> 4. **Chống God Class & God Method (Orchestrator Pattern & Clean Methods):**
>    - Tuân thủ nguyên tắc Single Responsibility: phương thức `execute()` của UseCase chỉ giữ vai trò nhạc trưởng (Orchestrator Pattern) điều phối các bước ở mức trừu tượng cao; độ dài tối đa 30–40 dòng.
>    - Cấm viết God Methods dài 50–100 dòng ôm đồm phân tích cú pháp, xác thực dữ liệu, tính toán số học, gọi repository và mapping DTO.
>    - Tách toàn bộ logic con thành private helper methods có tên thể hiện rõ ý định nghiệp vụ (`validateRequest()`, `verifyEligibility()`, `calculateRewardPoints()`, `persistLedgerEntry()`, `mapToResponse()`) hoặc chuyển sang Common Utility / Domain Service.
>    - Độ sâu lồng lề (nesting depth) tối đa 3 cấp; luôn dùng guard clauses (early returns) để làm phẳng luồng xử lý.
> 5. **Chuẩn Hóa Mã Lỗi Doanh Nghiệp (ErrorCode & BusinessException RFC 7807 Pattern):**
>    - Tuyệt đối **KHÔNG tạo class Exception con riêng lẻ** cho từng nghiệp vụ (như `UserNotFoundException`, `OtpInvalidException`...).
>    - Mọi module đều định nghĩa một Enum `<Module>ErrorCode` implement `com.ordera.common.error.ErrorCode` (chứa `HttpStatus`, mã lỗi `code`, thông điệp mặc định `message`).
>    - Ném lỗi thống nhất qua class duy nhất `BusinessException(ErrorCode, Object... args)` hoặc kèm message chi tiết từ `Messages.java`. `GlobalExceptionHandler` tự động chuyển đổi thành Problem Details RFC 7807.
> 6. **Xử Lý Ngoại Lệ An Toàn, Null-Safety & Tránh Rò Rỉ DB Connection:**
>    - Kiểm tra Null-Safety triệt để trên tất cả các trường nullable (như `user.getEmail()`, `order.getTip()`, headers, query params) trước khi gọi phương thức đối tượng.
>    - Nghiêm cấm nuốt lỗi (empty catch blocks) hoặc ném generic `RuntimeException` / `IllegalArgumentException` trong business logic.
>    - Với Khóa Phân Tán Redisson (`RLock`), luôn giải phóng trong khối `finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }`.
>    - Di chuyển transaction DB boundary vào bên trong lock bằng `TransactionTemplate` hoặc quản lý giao dịch cục bộ; KHÔNG bọc `@Transactional` bao trùm bên ngoài thời gian chờ acquire lock để tránh chiếm dụng và làm cạn kiệt connection pool của HikariCP.
> 7. **Tầng Tiện Ích Độc Lập (Common Util with `@UtilityClass`):**
>    - Logic tính toán thuần túy (công thức Haversine, regex token, format ngày giờ/khung giờ, bóc tách User ID / Principal, parse UUID an toàn) bắt buộc phải đặt trong `com.ordera.common.util.*` với Lombok `@UtilityClass`, không phụ thuộc Spring Context hay JPA Entities.
> 8. **Bảo Mật Phân Lập Multi-Tenant (Default ON):**
>    - Mọi truy vấn CSDL đều phải lọc theo `X-Tenant-ID` qua `TenantContextHolder` (ThreadLocal) và kích hoạt PostgreSQL Row-Level Security (RLS) `SET LOCAL app.current_tenant_id = ?` (ngăn chặn 100% lỗ hổng IDOR).
> 9. **Sổ Cái Điểm Kép Bất Biến (Immutable Points Accounting):**
>    - Tuyệt đối không UPDATE trực tiếp cột điểm. Mọi biến động đều ghi bản ghi mới vào `points_ledger` kèm `reference_id` và `reference_type`.
> 10. **Tài Liệu Hóa Luồng Nghiệp Vụ & Quy Chuẩn Commit (Flows & Action-Commit):**
>     - Mỗi phân hệ hoàn thành đều phải biên soạn file `docs/plans/<module>/business-flows.md` gồm:
>       + Biểu đồ Cây quyết định nghiệp vụ (Flowchart TD) thể hiện logic rẽ nhánh và kết quả nghiệp vụ (200 OK / 400 Bad Request / 404 Not Found), cấm đưa tên hàm unit test vào nhãn biểu đồ.
>       + Biểu đồ Trình tự tương tác phân tầng (Sequence Diagram) chuẩn hóa từ Client $\rightarrow$ Controller $\rightarrow$ UseCase $\rightarrow$ Common Util $\rightarrow$ Repository $\rightarrow$ Database $\rightarrow$ DtoMapper $\rightarrow$ Client.
>       + Sử dụng bảng màu Dark Mode High Contrast (`classDef ok`, `classDef fail`, `classDef c1/c2/c3` có thuộc tính `color:#...` rõ ràng).
>     - Quy chuẩn commit tuân thủ skill `action-commit` (`develop: ...` kèm trailer `Co-authored-by: AI Agent <agent@ai-kit>`).

---

## 📑 Mục Lục Các Giai Đoạn (Execution Roadmap)

- [Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend (Clean Architecture 4 Lớp)](#giai-đoạn-1-triển-khai-toàn-diện-8-module-backend-clean-architecture-4-lớp)
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

## Giai Đoạn 1: Triển Khai Toàn Diện 8 Module Backend (Clean Architecture 4 Lớp)

### Prompt 1.1: Phân hệ Cửa Hàng, Chi Nhánh, Bàn Ăn & Thiết Bị Kiosk (Tenant & Store Module)
> **Mục tiêu:** Cung cấp thông tin đại lý thương hiệu, danh sách chi nhánh (Linn Sushi Sentrum), vị trí GPS, danh sách bàn ăn, giải mã QR Token 3 tầng, tùy chọn nhận hàng động (Dine-in, Takeaway, Delivery), yêu thích cửa hàng và quản lý thiết bị Kiosk quầy thu ngân.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Cửa Hàng, Chi Nhánh, Bàn Ăn & Thiết Bị Kiosk (Tenant & Store Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Clean Architecture 4 lớp & PostgreSQL RLS).
2. API Design: docs/designs/api-design/01-tenant-and-store-api.md (Đầy đủ 8 API: 1.1 Tenant info, 1.2 Danh sách chi nhánh, 1.3 Chi tiết chi nhánh, 1.4 Danh sách bàn, 1.5 Giải mã QR bàn, 1.6 Yêu thích cửa hàng, 1.7 Danh sách cửa hàng yêu thích, 1.8 Tùy chọn nhận hàng động & khung giờ).
3. DB Design: docs/designs/db-design/01-tenants-and-stores-db.md (Bảng tenants, stores, store_tables, customer_favorite_stores, kiosk_devices).
4. Migration: backend/src/main/resources/db/migration/V1__create_tenants_and_stores.sql & V11__create_kiosk_devices.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.tenant
- Tầng Domain (domain/):
  + Models: Tenant, Store, StoreTable, CustomerFavoriteStore, KioskDevice, FulfillmentType (DINE_IN, TAKEAWAY, DELIVERY).
  + Domain Constants (domain/constant/TenantConstants.java): Định nghĩa DEFAULT_PREPARING_TIME_MINUTES = 15, DEFAULT_PAGE_SIZE = 20... Tuyệt đối KHÔNG hardcode số hoặc chuỗi trong code.
  + ErrorCode: TenantErrorCode (TENANT_HEADER_MISSING, STORE_NOT_FOUND, TABLE_NOT_FOUND, INVALID_QR_TOKEN, INVALID_QUERY_PARAM, UNAUTHORIZED) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
  + Rules: Đảm bảo độc lập hoàn toàn với Spring Framework và JPA.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): GeoUtils (Haversine formula), QrCodeUtils (regex token, format số bàn), SecurityUtils (resolve user ID, parse UUID), DateTimeUtils (sinh slot 30 phút theo opening_hours thực).
  + UseCases: GetTenantInfoUseCase, GetStoresListUseCase, GetStoreDetailUseCase, GetStoreTablesUseCase, ResolveTableQrUseCase (giải mã 3 tầng: Exact ➜ Normalized ➜ Regex table number), ToggleFavoriteStoreUseCase, GetFavoriteStoresUseCase, GetFulfillmentOptionsUseCase.
  + Chống God Method (Orchestrator Pattern): execute() chỉ giữ vai trò điều phối tối đa 30–40 dòng; tách toàn bộ logic lọc chi nhánh, tính khoảng cách, giải mã QR thành private helper methods hoặc Common Util.
  + Null-Safety & Guard Clauses: Kiểm tra null an toàn cho latitude, longitude, keyword, userIdHeader; dùng early return, nesting depth tối đa 3.
  + Orchestration: Sử dụng @Transactional(readOnly = true) cho các truy vấn dữ liệu.
- Tầng Infrastructure (infrastructure/):
  + Persistence: TenantJpaEntity, StoreJpaEntity, StoreTableJpaEntity, CustomerFavoriteStoreJpaEntity, KioskDeviceJpaEntity kế thừa BaseEntity (tenant_id).
  + Repositories: Spring Data JPA Repositories lọc tự động theo tenant_id qua TenantContext.
  + Mappers: TenantDtoMapper (MapStruct) ánh xạ Domain <-> JPA <-> DTO.
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/TenantMessages.java): Khai báo tập trung toàn bộ thông điệp phản hồi API MSG_* (MSG_STORES_RETRIEVED, MSG_STORE_DETAIL_RETRIEVED, MSG_TABLES_RETRIEVED, MSG_TABLE_RESOLVED, MSG_FULFILLMENT_OPTIONS_RETRIEVED, MSG_STORE_FAVORITE_ADDED, MSG_STORE_FAVORITE_REMOVED, MSG_FAVORITE_STORES_RETRIEVED, MSG_TENANT_DETAILS_RETRIEVED) và mô tả lỗi ERR_*.
  + Controllers: TenantController, StoreController trả về ResponseEntity.ok(ApiResponse.ok(TenantMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
  + Swagger UI: Cấu hình OpenApiConfig tự động chèn header X-Tenant-ID và nút Authorize toàn cục.
- Tài liệu hóa: Biên soạn docs/plans/tenant-store/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho 8 Use Cases.
- Kiểm thử: Viết Unit tests AAA (Arrange - Act - Assert) cho UseCases, Utils và WebMvc Controller (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.2: Phân hệ Xác Thực OTP, Định Danh & Cấp Phát JWT (Auth & Identity Module)
> **Mục tiêu:** Xây dựng luồng xác thực đăng nhập/đăng ký hội viên qua SMS/Email OTP (cooldown 120s, tối đa 5 lần thử trên Redis), quản lý ngày sinh `dateOfBirth`, cấp phát cặp Access Token (15m) + Refresh Token (7d) và thu hồi token vào Redis Blacklist khi đăng xuất.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Xác Thực OTP & Quản Lý Danh Tính (Auth & Identity Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Mô hình bảo mật JWT, Redis Cooldown & Blacklist).
2. API Design: docs/designs/api-design/03-auth-and-identity-api.md (Đầy đủ 5 API: 3.1 Gửi OTP, 3.2 Xác thực OTP, 3.3 Đăng ký hội viên, 3.4 Làm mới JWT, 3.5 Đăng xuất).
3. DB Design: docs/designs/db-design/02-users-and-identity-db.md (Bảng users, otp_audit_logs).
4. Migration: backend/src/main/resources/db/migration/V2__create_users_and_identity.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.auth
- Tầng Domain (domain/):
  + Models: User, UserRole (CUSTOMER, STAFF, ADMIN), OtpSession, TokenPair, OtpChannel (SMS, EMAIL).
  + Domain Constants (domain/constant/AuthConstants.java): Khai báo OTP_EXPIRATION_SECONDS = 120, OTP_COOLDOWN_SECONDS = 120, OTP_MAX_ATTEMPTS = 5, OTP_LOCK_DURATION_MINUTES = 15, ACCESS_TOKEN_VALIDITY_MS, REFRESH_TOKEN_VALIDITY_MS, REDIS_KEY_OTP_PREFIX, REDIS_KEY_BLACKLIST_PREFIX... Tuyệt đối KHÔNG hardcode số hoặc chuỗi.
  + ErrorCode: AuthErrorCode (OTP_INVALID, OTP_EXPIRED, OTP_COOLDOWN_ACTIVE, OTP_MAX_ATTEMPTS_EXCEEDED, PHONE_ALREADY_EXISTS, INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_REVOKED) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
  + Rules: Đảm bảo độc lập hoàn toàn với Spring Framework và JPA.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils; OtpUtils (sinh OTP 6 số, băm SHA-256), TokenUtils.
  + UseCases:
    * SendOtpUseCase & SendRegisterOtpUseCase: Kiểm tra cooldown 120s trên Redis, sinh OTP, băm SHA-256 lưu Redis TTL 120s, ghi nhật ký otp_audit_logs.
    * VerifyOtpUseCase & VerifyRegisterOtpUseCase: Kiểm tra số lần nhập sai (tối đa 5 lần), đối soát mã, trả về token và cờ isProfileComplete.
    * RegisterMemberUseCase: Tiếp nhận thông tin (David Nguyen, SĐT, Email, dateOfBirth), tạo user và khởi tạo member_profiles.
    * RefreshTokenUseCase & LogoutUseCase: Cấp lại token và đưa token thu hồi vào Redis Blacklist.
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách các private helper methods (checkCooldown(), generateAndSaveOtp(), auditLog(), verifyFailedAttempts()).
  + Null-Safety: Kiểm tra user.getEmail() != null trước khi so sánh, bảo vệ an toàn các trường optional.
  + Services: JwtTokenProviderService (HMAC-SHA512, claims: userId, tenantId, role).
- Tầng Infrastructure (infrastructure/):
  + Persistence: UserJpaEntity, OtpAuditLogJpaEntity và UserJpaRepository.
  + Adapters: TelnyxSmsAdapter (gửi SMS), MailHogEmailAdapter (gửi Email OTP), RedisTokenBlacklistAdapter.
  + Mappers: AuthDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/AuthMessages.java): Khai báo tập trung toàn bộ chuỗi thông điệp phản hồi MSG_* (MSG_OTP_SENT_SMS, MSG_OTP_SENT_EMAIL, MSG_LOGIN_COMPLETE, MSG_LOGIN_INCOMPLETE_PROFILE, MSG_REGISTER_OTP_SENT_TEMPLATE, MSG_REGISTER_SUCCESS_TEMPLATE, MSG_TOKEN_REFRESHED, MSG_LOGOUT_SUCCESS).
  + Controllers: AuthController trả về ResponseEntity.ok(ApiResponse.ok(AuthMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/auth/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho cả 5 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests kiểm tra đầy đủ các kịch bản thành công, sai OTP, hết hạn OTP, quá 5 lần thử và refresh token.
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.3: Phân hệ Thực Đơn Đa Cấp, Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module)
> **Mục tiêu:** Cung cấp danh mục thực đơn chính, danh mục con (Subcategories), cây thực đơn phân cấp, chi tiết món ăn (Aurora 89 Kr, Tuna Maki) kèm nhãn ăn kiêng `dietaryTags`, số lượng miếng `piecesText`, giá gốc `originalPrice`, các nhóm modifier (Size L, đá, đường, topping phụ thu) và banner khuyến mãi trang chủ.

```markdown
/feature Hãy triển khai tính năng: Phân hệ Thực Đơn, Món Ăn, Nhóm Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Clean Architecture 4 lớp & Caching).
2. API Design: docs/designs/api-design/02-catalog-and-menu-api.md (Đầy đủ 6 API: 2.1 Danh mục chính, 2.2 Danh mục con, 2.3 Cây thực đơn phân cấp, 2.4 Chi tiết món ăn, 2.5 Nhóm Modifiers, 2.6 Banner khuyến mãi).
3. DB Design: docs/designs/db-design/04-catalog-and-menu-db.md (Bảng categories, subcategories, products, product_modifier_groups, product_modifiers, promotion_banners).
4. Migration: backend/src/main/resources/db/migration/V5__create_catalog_and_modifiers.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.catalog
- Tầng Domain (domain/):
  + Models: Category, Subcategory, Product, ModifierGroup, Modifier, PromotionBanner, DietaryTag (GLUTEN_FREE, VEGAN, HALAL, NUT_FREE).
  + Domain Constants (domain/constant/CatalogConstants.java): Khai báo MIN_SEARCH_QUERY_LENGTH = 2, TAG_POPULAR = "popular", EMPTY_JSON_ARRAY = "[]", DEFAULT_DISPLAY_ORDER = 0, DEFAULT_PAGE_SIZE = 20... Tuyệt đối KHÔNG hardcode số hoặc chuỗi trong code.
  + ErrorCode: CatalogErrorCode (CATEGORY_NOT_FOUND, PRODUCT_NOT_FOUND, MODIFIER_GROUP_NOT_FOUND, INVALID_MODIFIER_SELECTION) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
  + Rules: Đảm bảo độc lập hoàn toàn với Spring Framework và JPA.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils để parse UUID và thông tin request an toàn.
  + UseCases:
    * GetCategoriesUseCase & GetSubcategoriesUseCase: Lấy danh mục món theo thứ tự hiển thị display_order và chi nhánh.
    * GetStoreMenuUseCase: Trả về cây thực đơn phân cấp hoàn chỉnh (Categories ➜ Subcategories ➜ Products).
    * GetProductDetailUseCase: Lấy chi tiết món kèm originalPrice, piecesText, dietaryTags và các nhóm modifier (bắt buộc/tùy chọn).
    * GetProductModifiersUseCase: Lấy danh sách nhóm modifiers theo sản phẩm.
    * GetPromotionBannersUseCase: Lấy danh sách banner quảng cáo đang kích hoạt (Hot Deals, Seasonal).
    * SearchProductsUseCase: Tìm kiếm sản phẩm theo keyword (độ dài tối thiểu MIN_SEARCH_QUERY_LENGTH).
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách logic duyệt cây phân cấp, ánh xạ món ăn, lọc modifier thành các private helper methods.
  + Null-Safety & Guard Clauses: Kiểm tra null an toàn cho query search, dietary tags; early return, nesting depth <= 3.
- Tầng Infrastructure (infrastructure/):
  + Persistence: CategoryJpaEntity, SubcategoryJpaEntity, ProductJpaEntity, ProductModifierGroupJpaEntity, ProductModifierJpaEntity, PromotionBannerJpaEntity.
  + Repositories: Tự động lọc theo tenant_id và cờ is_available = true.
  + Mappers: CatalogDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/CatalogMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_CATEGORIES_RETRIEVED, MSG_SUBCATEGORIES_RETRIEVED, MSG_MENU_RETRIEVED, MSG_PRODUCT_DETAIL_RETRIEVED, MSG_MODIFIERS_RETRIEVED, MSG_BANNERS_RETRIEVED, MSG_PRODUCTS_SEARCHED).
  + Controllers: CategoryController, ProductController, PromotionController trả về ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/catalog/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho cả 6 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests kiểm tra tính đúng đắn của dữ liệu phân cấp và nhóm modifier (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.4: Phân hệ Hồ Sơ Khách Hàng, Động Cơ 6 Hạng Thẻ & Sổ Cái Điểm Bất Biến (Loyalty Module)
> **Mục tiêu:** Quản lý hồ sơ David Nguyen (hạng Silver, 120 điểm, cờ `hasMissingPointsNotice`), mã vạch tích điểm nhanh `barcodeToken`, cập nhật thông tin cá nhân/ảnh đại diện, định nghĩa 6 hạng thẻ (`New` ➜ `Silver` ➜ `Gold` ➜ `Platinum` ➜ `Diamond` ➜ `Ruby`), lịch sử sổ cái phân trang từ bảng phân vùng `points_ledger` và tiếp nhận khiếu nại thiếu điểm hóa đơn trong 30 ngày.

```markdown
/feature Hãy triển khai tính năng: Động Cơ Thẻ Thành Viên & Sổ Cái Điểm Bất Biến (Loyalty & Points Ledger Engine) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Mục 3.1 Sổ cái điểm kép bất biến & Mục 3.2 Khóa phân tán Redisson).
2. API Design: docs/designs/api-design/04-loyalty-and-tier-api.md (Đầy đủ 7 API: 4.1 Xem hồ sơ, 4.2 Cập nhật thông tin, 4.3 Đổi SĐT/Email OTP, 4.4 Tải lên avatar, 4.5 Danh sách 6 hạng thẻ, 4.6 Lịch sử sổ cái điểm, 4.7 Khiếu nại thiếu điểm hóa đơn).
3. DB Design: docs/designs/db-design/03-loyalty-and-points-ledger-db.md (Bảng tier_definitions, member_profiles, points_ledger phân vùng RANGE theo năm, support_claims).
4. Migration: backend/src/main/resources/db/migration/V3__create_loyalty_and_tiers.sql, V4__create_points_ledger.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.loyalty & com.ordera.modules.ledger
- Tầng Domain (domain/):
  + Models: MemberProfile, MemberTier (NEW, SILVER, GOLD, PLATINUM, DIAMOND, RUBY), TransactionType (ORDER_EARN, VOUCHER_REDEEM, GIFT_REDEEM, TIER_BONUS, CLAIM_CREDIT, KIOSK_EARN), SupportClaim.
  + Domain Constants (domain/constant/LoyaltyConstants.java & LedgerConstants.java):
    * LoyaltyConstants: MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024 (5MB), ALLOWED_AVATAR_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp"), RECEIPT_CLAIM_MAX_DAYS = 30, BARCODE_PREFIX = "ORD-", REDISSON_LOCK_ACQUIRE_TIMEOUT_SEC = 3, REDISSON_LOCK_LEASE_TIMEOUT_SEC = 5, LOCK_KEY_MEMBER_PATTERN = "lock:tenant:%s:member:%s".
    * LedgerConstants: DEFAULT_PAGE_SIZE = 20, MAX_PAGE_SIZE = 50, REF_TYPE_ORDER, REF_TYPE_VOUCHER, REF_TYPE_GIFT, REF_TYPE_CLAIM, REF_TYPE_KIOSK.
    * Tuyệt đối KHÔNG hardcode số hoặc chuỗi trong code logic.
  + ErrorCode: LoyaltyErrorCode (INVALID_TIER_TRANSITION, RECEIPT_EXPIRED, CLAIM_ALREADY_EXISTS, CLAIM_NOT_FOUND, INSUFFICIENT_POINTS, INVALID_RECEIPT_DATE) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
  + NGUYÊN TẮC BẤT BIẾN: Tuyệt đối không UPDATE trực tiếp cột điểm. Mọi biến động đều phải chèn bản ghi mới vào points_ledger.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils; LoyaltyTierUtils (tính tỷ lệ thăng hạng, ngưỡng điểm 6 hạng thẻ).
  + UseCases:
    * GetMemberProfileUseCase: Trả về thông tin hội viên, hạng thẻ, currentPoints, lifetimePoints, barcodeToken, nextTier, progressPercentage, hasMissingPointsNotice.
    * CompleteProfileUseCase & UpdateProfileUseCase: Cập nhật thông tin hội viên David Nguyen.
    * UploadAvatarUseCase: Thẩm định kích thước (<= 5MB) và định dạng (JPEG, PNG, WEBP), lưu file và cập nhật đường dẫn avatar.
    * AccruePointsUseCase: Chiếm giữ Redisson Lock, dùng TransactionTemplate bên trong lock (không mở @Transactional bao ngoài lock tránh cạn kiệt HikariCP pool), ghi sổ cái points_ledger, tự động thăng hạng thẻ (Silver 100, Gold 400, Platinum 1000, Diamond 2000, Ruby 4000).
    * GetPointsLedgerHistoryUseCase: Phân trang lịch sử điểm (mặc định 20, tối đa 50) từ bảng points_ledger.
    * SubmitSupportClaimUseCase: Tiếp nhận hóa đơn (trong vòng 30 ngày), storeId, số tiền và ảnh biên lai, tạo support_claims (STATUS_PENDING).
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối tối đa 30–40 dòng; tách các private helper methods (validateAvatarFile(), findAndValidateUser(), updateProfileFields(), persistLedgerTransaction(), calculateTierProgress()).
  + Null-Safety: Kiểm tra user.getEmail() != null trước khi so sánh tránh NPE; kiểm tra multipart file rỗng; nesting depth <= 3.
- Tầng Infrastructure (infrastructure/):
  + Persistence: MemberProfileJpaEntity, TierDefinitionJpaEntity, PointsLedgerJpaEntity, SupportClaimJpaEntity (tenant_id từ BaseTenantEntity).
  + Repositories: MemberProfileJpaRepository, PointsLedgerJpaRepository, SupportClaimJpaRepository.
  + Mappers: LoyaltyDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/LoyaltyMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_PROFILE_RETRIEVED, MSG_PROFILE_COMPLETED, MSG_PROFILE_UPDATED, MSG_AVATAR_UPLOADED, MSG_TIERS_RETRIEVED, MSG_POINTS_ACCRUED, MSG_LEDGER_HISTORY_RETRIEVED, MSG_CLAIM_SUBMITTED) và mô tả lỗi ERR_*.
  + Controllers: MemberLoyaltyController, SupportClaimController trả về ResponseEntity.ok(ApiResponse.ok(LoyaltyMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/loyalty/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho 7 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests kiểm tra công thức nâng hạng tự động, khóa phân tán Redisson và kiểm tra tính bất biến của sổ cái (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.5: Phân hệ Ví Voucher, Kho Đổi Thưởng & Drawer Ưu Đãi Giỏ Hàng (Reward Module)
> **Mục tiêu:** Quản lý ví cá nhân `My Rewards`, danh mục đổi thưởng `Get More Rewards` (Voucher 100 NOK, -30%, Cốc Tumbler), khóa phân tán Redisson chống race condition đổi quà, và toàn bộ 6 API phục vụ Drawer chọn ưu đãi trực tiếp trong luồng Checkout (gán/gỡ voucher, gán/gỡ quà hiện vật).

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Ví Voucher, Đổi Thưởng & Ưu Đãi Giỏ Hàng (Reward & Voucher Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Khóa phân tán Redisson chống đổi quà đồng thời).
2. API Design: docs/designs/api-design/05-reward-and-voucher-api.md (Đầy đủ 11 API: 5.1 Ví My Rewards, 5.2 Kho đổi thưởng, 5.3 Đổi voucher, 5.4 Đổi cốc Tumbler, 5.5 Tổng quan ưu đãi, 5.6 Ưu đãi đủ điều kiện cho giỏ, 5.7 Validate mã promo nhập tay, 5.8 Áp dụng voucher, 5.9 Gỡ voucher, 5.10 Áp dụng quà hiện vật, 5.11 Gỡ quà hiện vật).
3. DB Design: docs/designs/db-design/05-rewards-and-vouchers-db.md (Bảng vouchers, user_vouchers, loyalty_gifts, user_gift_redemptions).
4. Migration: backend/src/main/resources/db/migration/V6__create_vouchers_and_rewards.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.reward
- Tầng Domain (domain/):
  + Models: Voucher, UserVoucher (AVAILABLE, IN_USE, USED, EXPIRED), LoyaltyGift, UserGiftRedemption (PENDING, SELECTED, CLAIMED).
  + Domain Constants (domain/constant/RewardConstants.java): Khai báo MAX_VOUCHERS_PER_ORDER = 1, MAX_PHYSICAL_GIFTS_PER_ORDER = 1, GIFT_REDEMPTION_CODE_LENGTH = 8, LOCK_ACQUIRE_TIMEOUT_SEC = 3, LOCK_LEASE_TIMEOUT_SEC = 5, LOCK_KEY_REWARD_PATTERN = "lock:tenant:%s:reward:%s", PROMO_CODE_MIN_LENGTH = 3, PROMO_CODE_MAX_LENGTH = 20. Cấm tuyệt đối magic numbers/strings.
  + ErrorCode: RewardErrorCode (INSUFFICIENT_POINTS, GIFT_OUT_OF_STOCK, PROMO_CODE_NOT_FOUND, MINIMUM_ORDER_NOT_MET, VOUCHER_ALREADY_USED, VOUCHER_EXPIRED, INVALID_REWARD_COMBINATION) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
  + QUY TẮC KẾT HỢP: Trong một đơn hàng cho phép kết hợp tối đa 01 Voucher VÀ 01 Quà hiện vật (Cốc Tumbler).
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils để parse UUID và bóc tách User ID an toàn.
  + UseCases:
    * RedeemVoucherUseCase: Kiểm tra số dư điểm, chiếm giữ Redisson Lock, trừ điểm khả dụng qua AccruePointsUseCase (VOUCHER_REDEEM), tạo bản ghi user_vouchers.
    * RedeemPhysicalGiftUseCase: Chiếm giữ Redisson lock, kiểm tra tồn kho stock_quantity > 0, trừ 1 tồn kho, sinh mã nhận quà 8 ký tự, tạo user_gift_redemptions.
    * GetAvailableOffersForCartUseCase: Lọc các voucher trong ví thỏa mãn giá trị đơn hàng tối thiểu min_order_value và quà tặng có thể chọn kèm.
    * ApplyVoucherToCartUseCase & RemoveVoucherFromCartUseCase: Khóa voucher sang IN_USE khi áp vào giỏ hàng và hoàn trả sang AVAILABLE nếu khách gỡ bỏ.
    * ApplyGiftToCartUseCase & RemoveGiftFromCartUseCase: Gán/gỡ quà hiện vật đi kèm đơn hàng checkout.
    * ValidatePromoCodeUseCase: Kiểm tra mã promo nhập tay hợp lệ.
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách logic thẩm định điểm, trừ kho quà, áp voucher thành private helper methods có tên rõ nghĩa.
  + Concurrency & Transaction: Đảm bảo giải phóng Redisson Lock trong khối finally; transaction DB đặt bên trong lock boundary.
- Tầng Infrastructure (infrastructure/):
  + Persistence: VoucherJpaEntity, UserVoucherJpaEntity, LoyaltyGiftJpaEntity, UserGiftRedemptionJpaEntity.
  + Repositories: VoucherJpaRepository, UserVoucherJpaRepository, LoyaltyGiftJpaRepository.
  + Mappers: RewardDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/RewardMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_USER_VOUCHERS_RETRIEVED, MSG_REWARD_CATALOG_RETRIEVED, MSG_VOUCHER_REDEEMED, MSG_GIFT_REDEEMED, MSG_OFFERS_SUMMARY_RETRIEVED, MSG_ELIGIBLE_OFFERS_RETRIEVED, MSG_PROMO_CODE_VALIDATED, MSG_VOUCHER_APPLIED, MSG_VOUCHER_REMOVED, MSG_GIFT_APPLIED, MSG_GIFT_REMOVED) và mô tả lỗi ERR_*.
  + Controllers: RewardController, CartOfferController trả về ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/reward/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho toàn bộ Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests cho kịch bản đủ điểm, thiếu điểm, hết tồn kho quà và race condition đồng thời (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.6: Phân hệ Giỏ Hàng, Thuế VAT 15%, Tips, Checkout & Hóa Đơn E-Receipt (Order Module)
> **Mục tiêu:** Quản lý giỏ hàng trên Redis Cache (TTL 7 ngày), thêm món kèm modifier, cập nhật số lượng/ghi chú, tính toán dự toán Checkout gồm thuế VAT 15% Na Uy (công thức $Total \times \frac{15}{115}$), tiền tip, xử lý đặt đơn qua Idempotency-Key và Redisson Lock, sinh số thứ tự nhận món `#005`, lưu `applied_gift_id`, tiến trình chuẩn bị món và xuất hóa đơn điện tử E-Receipt gửi Email PDF.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Giỏ Hàng, Đặt Món, Tính Thuế VAT, Tips, Checkout & Hóa Đơn E-Receipt (Order Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (ACID Transaction, Redisson Idempotency & Concurrency Lock).
2. API Design: docs/designs/api-design/06-order-and-checkout-api.md (Đầy đủ 10 API: 6.1 Lấy giỏ hàng, 6.2 Thêm món vào giỏ, 6.3 Sửa/Xóa món trong giỏ, 6.4 Ghi chú dị ứng, 6.5 Dự toán Checkout, 6.6 Chốt đơn & thanh toán, 6.7 Danh sách đơn hàng Active/Past, 6.8 Chi tiết đơn & Timeline bếp, 6.9 Đặt lại đơn cũ 1-click Reorder, 6.10 Gửi E-Receipt qua Email).
3. DB Design: docs/designs/db-design/06-orders-and-checkout-db.md (Bảng orders, order_items, order_item_modifiers, carts, cart_items).
4. Migration: backend/src/main/resources/db/migration/V7__create_orders_and_checkout.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.order
- Tầng Domain (domain/):
  + Models: Cart, CartItem, Order, OrderItem, OrderItemModifier, OrderStatus (PENDING, PREPARING, READY, COMPLETED, CANCELLED), PaymentStatus, PickupMethod (COUNTER, TABLE).
  + Domain Constants (domain/constant/OrderConstants.java): Khai báo VAT_RATE = new BigDecimal("0.15"), VAT_DIVISOR = new BigDecimal("1.15"), CART_TTL_DAYS = 7, IDEMPOTENCY_KEY_TTL_HOURS = 24, MAX_ALLERGY_NOTE_LENGTH = 200, ORDER_CODE_PREFIX = "ORD-", QUEUE_NUMBER_PREFIX = "#", DECIMAL_SCALE = 2, LOCK_KEY_CHECKOUT_PATTERN = "lock:order:checkout:%s", REDISSON_LOCK_ACQUIRE_TIMEOUT_SEC = 5, REDISSON_LOCK_LEASE_TIMEOUT_SEC = 10. Cấm tuyệt đối magic numbers/strings.
  + ErrorCode: OrderErrorCode (CART_EMPTY, MINIMUM_ORDER_VALUE_NOT_MET, IDEMPOTENT_OPERATION_CONFLICT, STOCK_UNAVAILABLE, ORDER_NOT_FOUND, INVALID_ORDER_STATUS) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils; OrderCalculationUtils (tính Subtotal, Discount, Tip, VAT 15% Total * 15 / 115, và pointsToEarn bằng BigDecimal scale = 2).
  + UseCases:
    * CartService: Quản lý giỏ hàng trên Redis Cache (TTL 7 ngày). Thêm món kèm modifiers, cập nhật số lượng và ghi chú dị ứng tối đa MAX_ALLERGY_NOTE_LENGTH (200 ký tự).
    * PreviewCheckoutCalculationUseCase: Tính toán chính xác Subtotal, Discount, Tip, VAT 15% (Total * 15 / 115), Total và số điểm thưởng dự kiến: pointsToEarn = floor((subtotal - discount) * tierRate). Tiền tip không được tính vào điểm thưởng.
    * CheckoutOrderUseCase:
      - Kiểm tra Idempotency-Key trên Redis (TTL 24h) chống thanh toán trùng lặp.
      - Chiếm giữ Redisson Lock lock:order:checkout:{userId}.
      - Tạo đơn hàng orders với order_number (#005), order_code (ORD-20261105-0891), applied_gift_id, applied_gift_title, e_receipt_email.
      - Chuyển user_vouchers sang USED.
      - Tự động gọi AccruePointsUseCase ghi sổ cái points_ledger (ORDER_EARN).
      - Xóa giỏ hàng khỏi Redis sau khi tạo đơn thành công.
    * ReorderPastOrderUseCase: Sao chép các món từ đơn hàng cũ vào giỏ hàng mới.
    * SendEReceiptEmailUseCase: Sinh hóa đơn điện tử PDF và gửi tới địa chỉ Email của khách qua JavaMailSender.
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách nhỏ thành các private helper methods (checkIdempotency(), acquireCheckoutLock(), validateAndCalculateAmounts(), createOrderEntities(), applyRewardsAndPoints(), clearCartCache(), dispatchReceiptEmail()).
  + Null-Safety: Kiểm tra tip nullable (order.getTip() != null ? order.getTip() : BigDecimal.ZERO), ghi chú dị ứng, voucher applied nullable; nesting depth <= 3.
- Tầng Infrastructure (infrastructure/):
  + Persistence: OrderJpaEntity, OrderItemJpaEntity, OrderItemModifierJpaEntity.
  + Repositories: OrderJpaRepository, OrderItemJpaRepository.
  + Adapters: RedisCartStorageAdapter, JavaMailReceiptAdapter.
  + Mappers: OrderDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/OrderMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_CART_RETRIEVED, MSG_ITEM_ADDED_TO_CART, MSG_CART_ITEM_UPDATED, MSG_CART_ITEM_REMOVED, MSG_ALLERGY_NOTE_UPDATED, MSG_CHECKOUT_PREVIEW_CALCULATED, MSG_ORDER_CHECKOUT_SUCCESS, MSG_ORDERS_LIST_RETRIEVED, MSG_ORDER_DETAIL_RETRIEVED, MSG_REORDER_SUCCESS, MSG_RECEIPT_EMAIL_SENT) và mô tả lỗi ERR_*.
  + Controllers: CartController, OrderController trả về ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/order/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho toàn bộ 10 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests kiểm tra độ chính xác số học BigDecimal (scale = 2), công thức VAT 15%, chiết khấu voucher, và logic tích điểm (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.7: Phân hệ Trạm Cảm Ứng Kiosk POS, Tích Điểm Nhanh & Watchdog 60s (Kiosk Module)
> **Mục tiêu:** Cung cấp API chuyên dụng cho màn hình Kiosk cảm ứng toàn màn hình tại quầy thu ngân: nhận diện danh tính khách qua SĐT, tích điểm nhanh cho đơn giấy không cần đặt món trên máy tính bảng (`KIOSK_EARN`), tự đặt đơn tại Kiosk xuất phiếu số thứ tự `#A-08`, tự động reset phiên 60s idle timeout và giám sát phần hardware máy in/máy quẹt thẻ.

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Kiosk POS & Tích Điểm Nhanh Tại Quầy (Kiosk Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Cơ chế Kiosk Watchdog 60s & Hardware Monitoring).
2. API Design: docs/designs/api-design/07-kiosk-pos-api.md (Đầy đủ 5 API: 7.1 Nhận diện SĐT, 7.2 Tích điểm nhanh, 7.3 Đặt đơn Kiosk, 7.4 Reset phiên, 7.5 Cấu hình thiết bị trạm).
3. DB Design: docs/designs/db-design/01-tenants-and-stores-db.md (Bảng kiosk_devices).
4. Prototype Kiosk: docs/prototype/kiosk/README.md (32 màn hình Kiosk Touch Terminal).
5. Migration: backend/src/main/resources/db/migration/V11__create_kiosk_devices.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.kiosk
- Tầng Domain (domain/):
  + Models: KioskSession, KioskDevice, HardwareStatus (ONLINE, OFFLINE, OUT_OF_PAPER, DECLINED).
  + Domain Constants (domain/constant/KioskConstants.java): Khai báo WATCHDOG_IDLE_TIMEOUT_SECONDS = 60, POST_PAYMENT_REDIRECT_SECONDS = 5, QUEUE_NUMBER_FORMAT = "#A-%02d", PHONE_REGEX = "^(\\+47)?[49]\\d{7}$", LOCK_KEY_KIOSK_PATTERN = "lock:kiosk:device:%s", SESSION_CACHE_TTL_MINUTES = 30. Cấm tuyệt đối magic numbers/strings.
  + ErrorCode: KioskErrorCode (INVALID_PHONE_NUMBER, KIOSK_DEVICE_NOT_FOUND, INVALID_STAFF_PIN, KIOSK_SESSION_EXPIRED) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils; KioskQueueUtils (sinh số thứ tự dạng #A-08).
  + UseCases:
    * KioskIdentifyCustomerUseCase: Nhận diện SĐT từ bàn phím số Kiosk, tra cứu tên, hạng thẻ, số điểm hiện có và tỷ lệ hoàn điểm. Nếu khách mới trả về isRegistered = false để Kiosk cho phép bấm Skip.
    * KioskQuickEarnUseCase: Dùng khi khách thanh toán POS ngoài, khách gõ SĐT tích điểm -> tính điểm theo hạng thẻ, ghi sổ cái points_ledger (TransactionType = KIOSK_EARN), sinh số thứ tự queueNumber (#A-08) và gửi lệnh in nhiệt.
    * ResetKioskSessionUseCase: Xóa session tạm thời trên Redis khi khách bấm Start Over hoặc bộ đếm thời gian 60 giây hết hạn do khách bỏ đi.
    * GetKioskConfigUseCase: Cung cấp thông tin cấu hình trạm Kiosk theo deviceId và ghi nhận last_heartbeat_at.
    * CreateKioskOrderUseCase: Đặt đơn trực tiếp tại màn hình cảm ứng Kiosk.
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách private helper methods (validateKioskDevice(), lookupCustomerByPhone(), calculateEarnPoints(), persistKioskLedgerEntry(), formatQueueTicket()).
  + Null-Safety & Guard Clauses: Kiểm tra deviceId, phone input hợp lệ; early return, nesting depth <= 3.
- Tầng Infrastructure (infrastructure/):
  + Persistence: KioskDeviceJpaEntity và KioskDeviceJpaRepository.
  + Adapters: RedisKioskSessionAdapter, ThermalPrinterAdapter.
  + Mappers: KioskDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/KioskMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_CUSTOMER_IDENTIFIED, MSG_QUICK_POINTS_EARNED, MSG_KIOSK_ORDER_PLACED, MSG_SESSION_RESET, MSG_KIOSK_CONFIG_RETRIEVED) và mô tả lỗi ERR_*.
  + Controllers: KioskController với header bắt buộc X-Tenant-ID, X-Kiosk-Device-ID và trả về ResponseEntity.ok(ApiResponse.ok(KioskMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/kiosk/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho cả 5 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests cho luồng tra cứu khách hàng, tích điểm nhanh và reset phiên làm việc (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

### Prompt 1.8: Phân hệ Hộp Thư Đa Danh Mục & Hỗ Trợ Đối Soát Điểm Cũ (Notification & Support Module)
> **Mục tiêu:** Quản lý hộp thư thông báo đa danh mục ("Messages" & "News"), huy hiệu số lượng chưa đọc, đánh dấu đã đọc, và tiếp nhận phiếu yêu cầu hỗ trợ đối soát điểm cũ bị thất lạc sau khi nâng cấp hệ thống (màn hình `05.03 Missing Your Points?` và mã QR Kiosk `K03.03`).

```markdown
/feature Hãy triển khai tính năng: Phân Hệ Hộp Thư Thông Báo & Hỗ Trợ Khách Hàng (Notification & Support Module) cho Backend theo Clean Architecture 4 lớp.

Tài liệu kỹ thuật bắt buộc:
1. Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Clean Architecture & Multi-Tenant Notification Dispatching).
2. API Design: docs/designs/api-design/08-notification-api.md (Đầy đủ 6 API: 8.1 Đếm chưa đọc, 8.2 Danh sách thông báo đa danh mục, 8.3 Chi tiết thông báo, 8.4 Đánh dấu 1 tin đã đọc, 8.5 Đánh dấu tất cả đã đọc, 8.6 Gửi phiếu hỗ trợ thiếu điểm).
3. DB Design: docs/designs/db-design/08-notifications-and-support-db.md (Bảng system_notifications, user_notifications, support_inquiries).
4. Migration: backend/src/main/resources/db/migration/V12__create_notifications_and_support.sql.

Yêu cầu thực hiện theo cấu trúc Clean Architecture:
- Package: com.ordera.modules.notification
- Tầng Domain (domain/):
  + Models: SystemNotification (Category: MESSAGES, NEWS, VOUCHER, GIFTS), UserNotification, SupportInquiry (PENDING, IN_REVIEW, RESOLVED, REJECTED).
  + Domain Constants (domain/constant/NotificationConstants.java): Khai báo DEFAULT_PAGE_SIZE = 20, MAX_PAGE_SIZE = 50, TICKET_CODE_PREFIX = "INQ-", MAX_INQUIRY_NOTE_LENGTH = 500, MAX_RECEIPT_IMAGE_SIZE_BYTES = 5 * 1024 * 1024, ALLOWED_ATTACHMENT_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf"). Cấm tuyệt đối magic numbers/strings.
  + ErrorCode: NotificationErrorCode (NOTIFICATION_NOT_FOUND, INQUIRY_NOT_FOUND, INQUIRY_VALIDATION_FAILED) implement ErrorCode interface, ném lỗi qua BusinessException. Cấm tạo Exception classes riêng lẻ.
- Tầng Application (application/):
  + Common Util (com.ordera.common.util): Tái sử dụng SecurityUtils để parse UUID và bóc tách User ID an toàn.
  + UseCases:
    * GetUnreadNotificationCountUseCase: Đếm số lượng thông báo chưa đọc của hội viên hiển thị badge đỏ trên icon quả chuông.
    * GetNotificationsListUseCase: Lấy danh sách thông báo phân trang theo tab Messages hoặc News.
    * GetNotificationDetailUseCase: Lấy chi tiết bài viết, banner ảnh và tự động đánh dấu đã đọc.
    * MarkNotificationReadUseCase & MarkAllNotificationsReadUseCase: Cập nhật trạng thái đọc của người dùng trong bảng user_notifications.
    * SubmitMissingPointsInquiryUseCase: Tiếp nhận phiếu yêu cầu tra cứu điểm cũ (Email, SĐT, mã hóa đơn cũ, ghi chú và ảnh chứng từ), sinh ticket_code (INQ-20250317-009) để Admin đối soát cơ sở dữ liệu cũ.
  + Chống God Method (Orchestrator Pattern): execute() chỉ điều phối các bước tối đa 30–40 dòng; tách nhỏ thành private helper methods (validateInquiryInput(), generateTicketCode(), saveInquiryRecord(), updateReadStatus()).
  + Null-Safety & Guard Clauses: Kiểm tra null an toàn cho file upload, email/sđt; early return, nesting depth <= 3.
- Tầng Infrastructure (infrastructure/):
  + Persistence: SystemNotificationJpaEntity, UserNotificationJpaEntity, SupportInquiryJpaEntity.
  + Repositories: SystemNotificationJpaRepository, UserNotificationJpaRepository, SupportInquiryJpaRepository.
  + Mappers: NotificationDtoMapper (MapStruct).
- Tầng Presentation (presentation/):
  + Presentation Messages (presentation/constant/NotificationMessages.java): Khai báo tập trung chuỗi phản hồi MSG_* (MSG_UNREAD_COUNT_RETRIEVED, MSG_NOTIFICATIONS_LIST_RETRIEVED, MSG_NOTIFICATION_DETAIL_RETRIEVED, MSG_NOTIFICATION_MARKED_READ, MSG_ALL_NOTIFICATIONS_MARKED_READ, MSG_INQUIRY_SUBMITTED) và mô tả lỗi ERR_*.
  + Controllers: NotificationController, SupportInquiryController trả về ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_*, data)). Tuyệt đối cấm hardcode string trong ApiResponse.
- Tài liệu hóa: Biên soạn docs/plans/notification/business-flows.md với Mermaid Flowchart nghiệp vụ và Sequence Diagram Clean Architecture cho 6 Use Cases (chuẩn Dark Mode, không đưa tên hàm test vào biểu đồ).
- Kiểm thử: Unit tests cho các UseCases thông báo và gửi phiếu hỗ trợ (đạt 100% pass).
- Kiểm tra `mvn test` và commit vào nhánh develop theo skill action-commit.
```

---

## Giai Đoạn 2: Tích Hợp Frontend PWA & Kiosk POS Với Backend API Thực Tế

### Prompt 2.1: Tích Hợp HTTP Client Tập Trung, Interceptors & State Contexts (PWA Mobile)
> **Mục tiêu:** Thay thế dữ liệu mock trong Frontend React bằng các lệnh gọi REST API thực tế tới Backend thông qua Nginx Gateway Tier, duy trì trạng thái giỏ hàng, token đăng nhập và xử lý tự động refresh token.

```markdown
/feature Hãy thực hiện: Kết nối Frontend React 19 với Backend REST API thực tế (Full PWA Mobile API Integration) tuân thủ Kiến trúc Doanh nghiệp.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Mục 1 Sơ đồ Edge & Gateway Tier).
- API Design: docs/designs/api-design/README.md (Chuẩn Envelope ApiResponse<T> và Headers).
- API Specs: docs/designs/api-design/01-tenant-and-store-api.md đến 06-order-and-checkout-api.md.
- Source code: frontend/src/types/ và frontend/src/context/.

Yêu cầu thực hiện:
1. Tạo HTTP Client tập trung (Axios hoặc Fetch wrapper tại frontend/src/api/client.ts):
   - Quản lý constants tập trung tại frontend/src/constants/api.constants.ts (BASE_URL, ENDPOINTS, STORAGE_KEYS, DEFAULT_TIMEOUT_MS = 10000). Tuyệt đối không hardcode URL hoặc timeout rải rác.
   - Tự động gắn header `X-Tenant-ID` từ tenant cấu hình hiện tại.
   - Tự động gắn header `Authorization: Bearer <accessToken>` nếu người dùng đã đăng nhập.
   - Đăng ký response interceptor: Nếu nhận mã lỗi 401 UNAUTHORIZED hoặc TOKEN_EXPIRED, tự động gọi POST /api/v1/auth/token/refresh với refreshToken để cấp lại token mới và phát lại request cũ mà không bắt người dùng đăng nhập lại.
2. Nối API cho AuthContext:
   - Đăng nhập/Đăng ký qua POST /api/v1/auth/otp/send và POST /api/v1/auth/otp/verify.
   - Đăng xuất qua POST /api/v1/auth/logout.
   - Giải nén envelope ApiResponse<T>, kiểm tra response.data.success trước khi cập nhật state.
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
/feature Hãy thực hiện: Kết nối giao diện Kiosk POS (KioskPage.tsx) với Backend Kiosk API thực tế tuân thủ Kiến trúc Kiosk.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md.
- Kiosk Prototype Spec: docs/prototype/kiosk/README.md (32 màn hình cảm ứng).
- API Design: docs/designs/api-design/07-kiosk-pos-api.md.
- Source code: frontend/src/pages/KioskPage.tsx.

Yêu cầu thực hiện:
1. Quản lý hằng số Kiosk tập trung (frontend/src/constants/kiosk.constants.ts): KIOSK_IDLE_TIMEOUT_MS = 60000, SUCCESS_REDIRECT_DELAY_MS = 5000. Cấm hardcode timer trong component.
2. Khi Kiosk khởi động, gọi GET /api/v1/kiosk/config/{deviceId} để tải thông tin chi nhánh và thời gian timeout cấu hình.
3. Thay thế mock handleLookup bằng lời gọi POST /api/v1/kiosk/identify với số điện thoại khách vừa gõ trên bàn phím cảm ứng. Hiển thị đúng thông tin thành viên (Tên, Hạng thẻ, Điểm khả dụng). Nếu isRegistered = false, hiển thị nút Skip để khách tích điểm mới.
4. Thay thế handleCompletePayment bằng lời gọi POST /api/v1/kiosk/quick-earn để ghi sổ cái tích điểm KIOSK_EARN và hiển thị số thứ tự hàng đợi thực tế (#A-08) kèm số điểm vừa cộng.
5. Đồng hồ đếm ngược 60s Watchdog: Mỗi thao tác chạm phím cảm ứng sẽ reset lại 60 giây. Khi hết hạn đếm ngược hoặc khách bấm Start Over, gọi POST /api/v1/kiosk/session/reset để xóa sạch session cache và trở về màn hình chờ ban đầu.
6. Sau khi thanh toán thành công, đếm ngược 5 giây rồi tự động gọi handleStartOver.
7. Kiểm tra build frontend bằng `npm.cmd run build`.
8. Commit thay đổi vào nhánh develop theo skill action-commit.
```

---

### Prompt 2.3: Tích Hợp Toàn Diện Các Modal Nghiệp Vụ (E-Receipt, Missing Points, Offer Drawer)
> **Mục tiêu:** Kết nối hoàn chỉnh các Popup / Modal tương tác sâu theo đúng Figma Prototype: E-Receipt gửi Email PDF, Modal khiếu nại điểm thiếu "Missing Your Points?", và Drawer chọn voucher/quà tặng trong màn hình Checkout.

```markdown
/feature Hãy thực hiện: Tích hợp Backend API cho các Modal và Drawer nghiệp vụ trên Frontend.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md.
- API Design: docs/designs/api-design/05-reward-and-voucher-api.md (API 5.6 đến 5.11 - Drawer ưu đãi).
- API Design: docs/designs/api-design/06-order-and-checkout-api.md (API 6.10 - Gửi E-Receipt).
- API Design: docs/designs/api-design/08-notification-api.md (API 8.6 - Submit phiếu hỗ trợ điểm).
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
/feature Hãy viết bộ kiểm thử tích hợp (Integration Tests) cho Backend sử dụng Testcontainers theo chuẩn Architecture.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Mục 7 Quy trình Tự động hóa CI/CD & Testcontainers).

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
/feature Hãy rà soát bảo mật toàn diện và hoàn thiện cấu hình Docker Compose cho toàn bộ hệ sinh thái Ordera.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md (Mục 4 Multi-Tenancy Security & Mục 6 Docker Compose).

Yêu cầu thực hiện:
1. Rà soát bảo mật mã nguồn theo nguyên tắc Security Guidelines:
   - Đảm bảo không có secret, mật khẩu DB, khóa bí mật JWT bị hardcode trong source code; toàn bộ phải đọc qua biến môi trường hoặc application.yml chuẩn hóa.
   - Xác thực 100% các endpoint nghiệp vụ đều kiểm tra quyền sở hữu tài nguyên thông qua TenantContextHolder.
2. Kiểm tra cấu hình Nginx trong thư mục infra/:
   - Đảm bảo reverse proxy định tuyến chính xác `/api/*` vào Backend (port 8080) và `/*` vào Frontend (port 5173/80).
   - Thiết lập các header bảo mật: X-Frame-Options, X-Content-Type-Options, Content-Security-Policy.
3. Kiểm tra file docker-compose.yml khởi động đầy đủ 5 services: postgres, redis, backend, frontend, gateway (nginx), mailhog.
4. Kiểm tra pipeline GitHub Actions trong .github/workflows/ (backend-ci.yml, frontend-ci.yml) đảm bảo tự động build và chạy test khi push lên nhánh develop và main.
5. Commit thay đổi hoàn thiện vào nhánh develop theo skill action-commit.
```

---

### Prompt 3.3: Kiểm Thử Tự Động Hóa E2E Toàn Trình (End-to-End User Journeys)
> **Mục tiêu:** Viết kịch bản kiểm thử tự động hóa E2E (Playwright hoặc Cypress) mô phỏng chính xác hành vi của khách hàng từ lúc vào web đến khi nhận hóa đơn điện tử E-Receipt.

```markdown
/feature Hãy viết kịch bản kiểm thử tự động hóa toàn trình (E2E Tests) cho ứng dụng Ordera theo Architecture Blueprint.

Tài liệu tham chiếu:
- Architecture Blueprint: docs/designs/architecture/enterprise-architecture.md.
- Prototype: docs/prototype/README.md (Toàn bộ 8 luồng PWA và 7 luồng Kiosk POS).

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
