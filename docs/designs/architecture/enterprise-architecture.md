# Enterprise Full-Stack System Architecture Blueprint
## Ordera Multi-Tenant Loyalty & Ordering Platform (Java Spring Boot 3.x Edition)

> **Tài liệu Kiến trúc Chuẩn Doanh Nghiệp (System Architecture Document - SAD)**  
> **Phiên bản:** 2.0 (Harmonized Enterprise Edition)  
> **Tech Stack phê duyệt:**  
> - **Frontend:** React 19, TypeScript, Vite, Tailwind CSS, Lucide Icons  
> - **Backend:** Java 21 LTS, Spring Boot 3.3.5 (Spring Security 6, Spring Data JPA, Flyway, MapStruct, Redisson)  
> - **Kiến trúc Backend:** Modular Monolith (Clean Architecture 4 lớp: Presentation ➜ Application ➜ Domain ➜ Infrastructure)  
> - **Cơ sở dữ liệu & Cache:** PostgreSQL 16 (Multi-tenant với Row-Level Security RLS) + Redis 7 (Cache, Rate-limit, Distributed Lock)  
> - **Infra & DevOps:** Docker, Docker Compose, Nginx Reverse Proxy, MailHog, GitHub Actions CI/CD  

---

## 1. Tầm nhìn Kiến trúc & Định hình Hệ thống (System Architecture Vision)

Ordera là nền tảng số khép kín phục vụ ngành F&B / Nhà hàng theo mô hình **Đa thương hiệu (Multi-Tenant)**. Hệ thống cung cấp hai điểm chạm người dùng chính:
1. **Khách hàng cá nhân (Mobile Web PWA):** Đặt món giao tận nơi hoặc nhận tại bàn, quản lý 6 hạng thẻ thành viên, ví ưu đãi My Rewards, đổi quà tặng hiện vật và gửi hóa đơn điện tử E-Receipt.
2. **Khách hàng tại quầy (Kiosk POS Touchscreen):** Trạm cảm ứng tự phục vụ đặt món độc lập hoặc tích điểm nhanh qua số điện thoại kèm tiền tip cảm ứng.
3. **Thương hiệu & Đại lý (Agency / Tenant):** Quản trị danh mục thực đơn đa cấp, chính sách thăng hạng thẻ, kho voucher và báo cáo giao dịch độc lập tuyệt đối.

```mermaid
flowchart TB
    subgraph ClientTier["1. CLIENT TIER (User Experience)"]
        CustomerApp["Customer Mobile Web PWA<br>(React 19 + Vite + Tailwind)<br>docs/prototype/1-8"]
        KioskApp["In-Store Kiosk Touchscreen<br>(React 19 + Fullscreen Mode)<br>docs/prototype/kiosk"]
        MerchantPortal["Merchant & Agency Admin<br>(React Admin Dashboard)"]
    end

    subgraph GatewayTier["2. EDGE & GATEWAY TIER"]
        NginxGateway["Nginx API Gateway & Reverse Proxy (Port 80/443)<br>- SSL / TLS Termination<br>- Same-Origin Routing (Tránh lỗi CORS)<br>- Rate Limiting OTP & Brute-force Prevention"]
    end

    subgraph BackendTier["3. BACKEND TIER: Java 21 & Spring Boot 3.3.5 (Modular Monolith)"]
        SpringSecurity["Spring Security 6 (JWT Auth, RBAC, Tenant Filter)"]
        
        subgraph DomainModules["Core Business Domains (Clean Architecture 4 Lớp)"]
            TenantMod["1. Tenant & Store Management<br>(Agency Context, Multi-branch, Tables, Kiosk Devices)"]
            AuthMod["2. Identity & OTP Service<br>(Register, Login, SMS/Email OTP, JWT, Profile)"]
            CatalogMod["3. Catalog & Menu Engine<br>(Categories, Subcategories, Modifiers, Banners)"]
            LoyaltyMod["4. Loyalty Tier Engine<br>(6 Hạng: New -> Ruby, Progress Stepper)"]
            LedgerMod["5. Points Ledger Engine<br>(Double-Entry Audit, Immutable Accounting)"]
            RewardMod["6. Vouchers & Rewards Wallet<br>(Min order, % / Fixed discount, Gift redemption)"]
            OrderMod["7. Cart, Order & Checkout<br>(VAT 15%, Tips, E-Receipt, Reorder)"]
            NotifyMod["8. Notification & Support Claims<br>(In-app alerts 4 tabs, Missing points QR)"]
        end
    end

    subgraph DataTier["4. DATA & STORAGE TIER"]
        Postgres[(PostgreSQL 16 Primary DB<br>- Flyway Migrations (V1 -> V11)<br>- Row-Level Security (RLS)<br>- ACID Transactions cho Orders & Ledger)]
        Redis[(Redis 7 Stack<br>- OTP Storage & 120s TTL Cooldown<br>- Session & User Token Blacklist<br>- Redisson Distributed Lock cho Trừ điểm & Voucher)]
        MailHog[(MailHog Mock SMTP<br>- Bắt mã OTP Email & E-Receipt trong Local Dev)]
    end

    ClientTier -->|HTTP / HTTPS| NginxGateway
    NginxGateway -->|/api/v1/*| SpringSecurity
    NginxGateway -->|/*| CustomerApp

    SpringSecurity --> DomainModules

    TenantMod -->|Truy vấn chi nhánh & bàn ăn| Postgres
    AuthMod -->|Lưu OTP TTL 120s & Token Blacklist| Redis
    AuthMod -->|Lưu Users & Audit Logs| Postgres
    CatalogMod -->|Truy vấn thực đơn & Modifiers| Postgres
    LoyaltyMod -->|Đồng bộ cấp bậc & 6 hạng thẻ| Postgres
    LedgerMod -->|Ghi nhận sổ cái điểm kép bất biến| Postgres
    RewardMod -->|Truy vấn kho voucher & quà tặng| Postgres
    OrderMod -->|Transaction đơn hàng + tính VAT + tips| Postgres
    OrderMod -->|Redisson Lock đảm bảo chỉ áp dụng voucher 1 lần| Redis
    NotifyMod -->|Lưu khiếu nại & gửi thông báo| Postgres
```

---

## 2. Tổ chức Thư mục Chuẩn Enterprise (Repository Layout)

Cấu trúc phân tầng dự án tách bạch rõ ràng giữa Frontend (React/Vite), Backend (Java 21/Maven), và Infrastructure (Docker/Nginx):

```tree
orderaapp/
├── .github/                           # CI/CD Workflows
│   └── workflows/
│       ├── backend-ci.yml             # Maven build, JDK 21, Checkstyle, Semgrep, Unit Test
│       └── frontend-ci.yml            # Lint, Type-check, Vite build
│
├── frontend/                          # Client Web & Kiosk SPA (React 19, Vite, Tailwind CSS)
│   ├── src/
│   │   ├── components/                # UI Components (Header, Modals, Drawers, Cards)
│   │   ├── context/                   # CartContext, AuthContext, LoyaltyContext
│   │   ├── pages/                     # StoreListPage, MenuPage, KioskPage, ProfilePage,...
│   │   ├── services/                  # Axios HTTP client, API interceptors
│   │   └── types/                     # TypeScript domain models & API envelopes
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── backend/                           # Enterprise Backend API (Java 21, Spring Boot 3.3.5)
│   ├── pom.xml                        # Maven Parent POM
│   ├── mvnw / mvnw.cmd                # Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/ordera/
│       │   │   ├── OrderaApplication.java
│       │   │   ├── common/            # BaseEntity, GlobalExceptionHandler, ApiResponse<T>, TenantContextHolder
│       │   │   ├── config/            # SecurityConfig, RedisConfig, OpenApiSwaggerConfig, JpaAuditingConfig
│       │   │   └── modules/           # 8 Domain Modules (Clean Architecture)
│       │   │       ├── tenant/        # Agency, Store, StoreTable, KioskDevice
│       │   │       ├── auth/          # User, OTP Service, JWT Token Provider, Blacklist
│       │   │       ├── catalog/       # Category, Subcategory, Product, ModifierGroup, Banner
│       │   │       ├── loyalty/       # 6 Hạng thẻ (New->Ruby), Tier Calculator, Barcode Token
│       │   │       ├── ledger/        # PointsLedger (Sổ cái kép bất biến, partitioned)
│       │   │       ├── reward/        # VoucherCatalog, UserVoucher, LoyaltyGift, Offers Drawer
│       │   │       ├── order/         # Cart, OrderItem, Checkout, VAT 15%, Tip, E-Receipt
│       │   │       └── notification/  # In-app notifications (4 tabs), Missing points claims QR
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/      # Flyway SQL Scripts (V1 -> V11)
│       └── test/                      # Unit & Integration Tests (Testcontainers + JUnit 5)
│
├── infra/                             # Hạ tầng & Môi trường triển khai
│   ├── docker/
│   │   ├── docker-compose.yml         # 5 services (gateway, backend, frontend, postgres, redis, mailhog)
│   │   ├── Dockerfile.backend         # Multi-stage Docker build cho Spring Boot (Eclipse Temurin 21)
│   │   ├── Dockerfile.frontend        # Multi-stage Docker build cho React (Node 22 -> Nginx static)
│   │   └── nginx/
│   │       ├── nginx.conf             # API Gateway Reverse Proxy
│   │       └── default.conf
│   └── scripts/
│       ├── init-database.sql          # Khởi tạo database ordera_db và extensions RLS
│       └── wait-for-it.sh
│
├── docs/                              # Toàn bộ tài liệu kỹ thuật chuẩn hóa
│   ├── designs/
│   │   ├── architecture/              # BẢN VẼ KIẾN TRÚC DOANH NGHIỆP TỔNG THỂ (SAD)
│   │   ├── api-design/                # ĐẶC TẢ 8 PHÂN HỆ RESTFUL API & KIOSK POS
│   │   ├── db-design/                 # MASTER ERD & 20 BẢNG POSTGRESQL RLS
│   │   └── srs/                       # ĐẶC TẢ YÊU CẦU NGHIỆP VỤ (CARLLM SRS)
│   ├── prototype/                     # 8 PHÂN HỆ MOBILE WEB (73 MÀN) & 7 PHÂN HỆ KIOSK (32 MÀN)
│   └── END_TO_END_PROMPTS.md          # SỔ TAY CÂU LỆNH PROMPT PHÁT TRIỂN TOÀN DIỆN
├── Makefile                           # Phím tắt dev: make up, make down, make build, make test
├── .gitignore
└── README.md
```

---

## 3. Kiến trúc Chi Tiết Backend Java Spring Boot (Clean Architecture 4 Lớp)

Mọi module nghiệp vụ trong Backend đều tuân thủ chặt chẽ **Clean Architecture 4 lớp** để tách biệt hoàn toàn giữa nghiệp vụ lõi (Domain), luồng điều phối (Application), giao tiếp bên ngoài (Presentation), và kỹ thuật lưu trữ (Infrastructure):

```tree
src/main/java/com/ordera/modules/<module_name>/
├── presentation/                      # 1. LỚP TRÌNH DIỄN & GIAO TIẾP (Inbound Layer)
│   ├── <Module>Controller.java        # REST Controller định tuyến HTTP endpoints
│   ├── constant/
│   │   └── <Module>Messages.java      # Thông điệp phản hồi người dùng ApiResponse.ok(MSG_*, data) & ERR_*
│   └── dto/                           # Request & Response DTOs
│       ├── <Action>Request.java       # DTO gắn annotations validation (@NotNull, @Valid, @Pattern)
│       └── <Action>Response.java      # DTO trả về bọc trong ApiResponse<T>
│
├── application/                       # 2. LỚP ỨNG DỤNG & ĐIỀU PHỐI (Service / Use Case Layer)
│   ├── usecase/                       # Giao diện và Hiện thực hóa từng Use Case độc lập
│   │   ├── <Action>UseCase.java       # Interface định nghĩa hành vi nghiệp vụ
│   │   └── impl/
│   │       └── <Action>UseCaseImpl.java # Orchestrator Pattern (<= 30-40 dòng/execute, cấm God Method)
│   └── service/                       # Domain / Application Services phối hợp liên module
│       └── <Module>DomainService.java
│
├── domain/                            # 3. LỚP NGHIỆP VỤ LÕI & THỰC THỂ (Core Domain Layer - Pure Java)
│   ├── model/                         # Pure Java POJO (Không phụ thuộc Framework hay Database)
│   │   ├── <Entity>.java              # Thực thể nghiệp vụ
│   │   └── <ValueObject>.java         # Giá trị bất biến (Value Objects, Enums)
│   ├── constant/                      # Hằng số nghiệp vụ thuần túy (Business rules, limits, timeouts, regex)
│   │   └── <Module>Constants.java     # Cấm tuyệt đối magic numbers/strings rải rác trong code
│   └── exception/                     # Danh mục mã lỗi chuẩn hóa RFC 7807 Problem Details
│       └── <Module>ErrorCode.java     # Enum implement ErrorCode (HttpStatus, code, default message)
│
└── infrastructure/                    # 4. LỚP HẠ TẦNG & TRUY XUẤT DỮ LIỆU (Outbound Layer)
    ├── persistence/
    │   ├── entity/                    # JPA Hibernate Entities (@Entity, @Table, @Column)
    │   │   └── <Entity>JpaEntity.java
    │   └── repository/                # Spring Data JPA Repositories
    │       └── <Entity>JpaRepository.java
    ├── adapter/                       # Adapters kết nối bên thứ 3 (SMS, Email, Lock)
    │   ├── TelnyxSmsAdapter.java
    │   └── RedissonDistributedLockAdapter.java
    └── mapper/                        # MapStruct Mappers chuyển đổi DTO <-> Domain <-> JPA
        └── <Module>DtoMapper.java
```

### 3.1 Quy Tắc Thiết Kế Sổ Cái Điểm Bất Biến (Immutable Points Ledger Engine)
Để bảo vệ doanh nghiệp trước mọi rủi ro gian lận điểm, hệ thống thiết lập nguyên tắc kế toán kép bất biến:
1. **Tuyệt đối không chạy lệnh `UPDATE` trên số dư điểm:** Cột `current_points` trên bảng `member_profiles` chỉ là giá trị cache được cập nhật đồng thời với giao dịch ghi sổ cái.
2. **Mọi biến động đều tạo một dòng mới trong bảng `points_ledger`:**
   - `id`: UUID định danh duy nhất của giao dịch điểm.
   - `tenant_id`: Định danh thương hiệu/đại lý (Multi-tenant isolation).
   - `member_id`: Khách hàng nhận hoặc tiêu điểm.
   - `transaction_type`: `ORDER_EARN`, `VOUCHER_REDEEM`, `GIFT_REDEEM`, `TIER_BONUS`, `CLAIM_CREDIT`, `KIOSK_EARN`.
   - `points_delta`: Số nguyên dương (cộng điểm) hoặc âm (trừ điểm).
   - `balance_after`: Số dư điểm khả dụng ngay sau khi thực hiện giao dịch.
   - `reference_id`: Khóa ngoại tham chiếu (`order_id`, `voucher_id`, `claim_id`).
   - `created_at`: Dấu thời gian bất biến (Partitioned theo năm `RANGE (created_at)`).

### 3.2 Khóa Phân Tán Redisson (Distributed Concurrency Lock)
Khi xảy ra các thao tác nhạy cảm liên quan đến tài sản điểm thưởng hoặc mã giảm giá:
- Đổi điểm lấy voucher / quà tặng (`Redeem Reward`).
- Áp dụng voucher vào đơn hàng tại checkout (`Apply Voucher`).
- Trừ điểm thanh toán đơn hàng.
Backend sử dụng **Redisson Distributed Lock**:
```java
RLock lock = redissonClient.getLock("lock:tenant:" + tenantId + ":member:" + memberId);
try {
    if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
        // Thực thi nghiệp vụ trừ điểm & ghi sổ cái trong Transaction ACID
    } else {
        throw new ConcurrentOperationException("Yêu cầu đang được xử lý, vui lòng không nhấn liên tục.");
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

---

## 4. Kiến trúc Multi-Tenant & Bảo Mật Doanh Nghiệp (Multi-Tenancy Security)

Chính sách bảo mật của Ordera quy định: **Bảo mật Multi-Tenant luôn được BẬT MẶC ĐỊNH trên 100% các luồng truy xuất**.

### 4.1 Cơ chế Phân Lập Dữ Liệu & Xác Thực Ba Tầng (Triple-Layer Security & Tenant Isolation)
1. **Tầng Ứng Dụng (TenantInterceptor & TenantContext):**
   - Mọi HTTP Request gửi tới `/api/v1/*` đều phải mang Header `X-Tenant-ID: <UUID>` hoặc được trích xuất từ JWT Claim (`tenantId`).
   - `TenantInterceptor` xác minh sự tồn tại của Tenant và lưu vào `TenantContext` (ThreadLocal):
     ```java
     public class TenantContext {
         private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
         public static void setTenantId(UUID tenantId) { CURRENT_TENANT.set(tenantId); }
         public static UUID getTenantId() { return CURRENT_TENANT.get(); }
         public static void clear() { CURRENT_TENANT.remove(); }
     }
     ```
2. **Tầng Xác Thực JWT & Ngữ Cảnh Danh Tính (JwtAuthenticationFilter):**
   - Trích xuất JWT Bearer Token từ header `Authorization: Bearer <token>`.
   - Kiểm tra Token Blacklist trên Redis (`blacklist:token:{jti}`) để từ chối các token đã bị thu hồi sau khi Đăng xuất.
   - Giải mã HMAC-SHA512 claims, trích xuất `userId`, `tenantId`, `role` và thiết lập `SecurityContextHolder`.
   - Hỗ trợ fallback linh hoạt cho môi trường phát triển cục bộ và Swagger UI thông qua header `X-User-ID: <UUID>`.
3. **Tầng Cơ Sở Dữ Liệu (PostgreSQL Row-Level Security - RLS):**
   - Mọi bảng dữ liệu nghiệp vụ (`stores`, `users`, `member_profiles`, `points_ledger`, `orders`, `vouchers`,...) đều chứa cột `tenant_id UUID NOT NULL`.
   - Cơ chế Hibernate Statement Inspector tự động kích hoạt biến phiên làm việc trước khi thực hiện câu lệnh SQL:
     ```sql
     SET LOCAL app.current_tenant_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
     ```
   - Chính sách PostgreSQL RLS tự động lọc dữ liệu:
     ```sql
     CREATE POLICY tenant_isolation_policy ON orders
     AS RESTRICTIVE
     USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);
     ```
   - Điều này triệt tiêu 100% lỗ hổng **IDOR (Insecure Direct Object Reference)** ngay cả khi lập trình viên quên thêm điều kiện `WHERE tenant_id = ?` trong mã nguồn.

---

## 5. Chuẩn Hóa Phản Hồi API & Bảng Mã Lỗi (RFC 7807 Problem Details)

Mọi phản hồi từ Backend đều được chuẩn hóa qua lớp phong bì `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Chi tiết thao tác",
  "data": { ... },
  "timestamp": "2026-11-07T17:00:00.000Z"
}
```

Khi có lỗi phát sinh, hệ thống chuyển giao cho `GlobalExceptionHandler` trả về cấu trúc lỗi chuẩn RFC 7807:

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_POINTS",
    "message": "Số điểm tích lũy của bạn không đủ để đổi ưu đãi này.",
    "details": {
      "requiredPoints": 1000,
      "currentPoints": 120
    }
  },
  "timestamp": "2026-11-07T17:00:00.000Z"
}
```

### Từ Điển Mã Lỗi Trọng Yếu:
| HTTP Status | Error Code | Ý nghĩa nghiệp vụ |
|:---:|:---|:---|
| `400` | `TENANT_HEADER_MISSING` | Thiếu Header bắt buộc `X-Tenant-ID`. |
| `400` | `INVALID_INPUT` | Dữ liệu đầu vào vi phạm Bean Validation (`@Valid`). |
| `401` | `OTP_INVALID` / `OTP_EXPIRED` | Mã OTP không đúng hoặc đã quá hạn 120s. |
| `401` | `UNAUTHORIZED` | Token JWT hết hạn, không hợp lệ hoặc nằm trong Redis Blacklist. |
| `403` | `FORBIDDEN_CROSS_TENANT` | Cố ý truy cập tài nguyên của thương hiệu khác. |
| `404` | `STORE_NOT_FOUND` / `PRODUCT_NOT_FOUND` | Không tìm thấy cửa hàng hoặc món ăn tương ứng. |
| `409` | `OTP_COOLDOWN_ACTIVE` | Yêu cầu gửi lại OTP khi chưa hết thời gian chờ 120s. |
| `409` | `VOUCHER_ALREADY_USED` | Mã voucher đã được sử dụng hoặc đang bị khóa. |
| `422` | `INSUFFICIENT_POINTS` | Điểm khả dụng không đủ để thực hiện đổi quà. |
| `422` | `MINIMUM_ORDER_NOT_MET` | Giá trị đơn hàng chưa đạt mức tối thiểu để áp dụng mã giảm giá. |

---

## 6. Môi Trường Hạ Tầng Cục Bộ & Docker Compose (Local Dev Stack)

Toàn bộ hệ sinh thái Ordera được đóng gói trong tệp `infra/docker/docker-compose.yml`, sẵn sàng khởi chạy bằng lệnh `docker-compose up -d`:

```yaml
version: '3.8'

services:
  # 1. Reverse Proxy & API Gateway (Port 80)
  gateway:
    image: nginx:1.27-alpine
    container_name: ordera-gateway
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/conf.d/default.conf:ro
    depends_on:
      - backend
      - frontend

  # 2. Enterprise Backend API (Port 8080)
  backend:
    build:
      context: ../../backend
      dockerfile: ../infra/docker/Dockerfile.backend
    container_name: ordera-backend-api
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ordera_db
      - SPRING_DATASOURCE_USERNAME=ordera_user
      - SPRING_DATASOURCE_PASSWORD=ordera_secret
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
      - SPRING_MAIL_HOST=mailhog
      - SPRING_MAIL_PORT=1025
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started

  # 3. Client Frontend PWA & Kiosk (Port 5173)
  frontend:
    build:
      context: ../../frontend
      dockerfile: ../infra/docker/Dockerfile.frontend.dev
    container_name: ordera-frontend
    ports:
      - "5173:5173"
    environment:
      - VITE_API_URL=/api/v1
    volumes:
      - ../../frontend:/app
      - /app/node_modules

  # 4. PostgreSQL 16 Primary Database (Port 5432)
  postgres:
    image: postgres:16-alpine
    container_name: ordera-postgres
    environment:
      - POSTGRES_DB=ordera_db
      - POSTGRES_USER=ordera_user
      - POSTGRES_PASSWORD=ordera_secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ../scripts/init-database.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ordera_user -d ordera_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  # 5. Redis 7 In-Memory Cache & Distributed Lock (Port 6379)
  redis:
    image: redis:7-alpine
    container_name: ordera-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # 6. MailHog Mock SMTP Server (Port 8025 / 1025)
  mailhog:
    image: mailhog/mailhog:latest
    container_name: ordera-mailhog
    ports:
      - "8025:8025" # Web UI xem email OTP & E-Receipt
      - "1025:1025" # SMTP Port

volumes:
  postgres_data:
  redis_data:
```

---

## 7. Quy Trình Tự Động Hóa CI/CD & Kiểm Thử (GitHub Actions & Testcontainers)

1. **Backend CI (`.github/workflows/backend-ci.yml`):**
   - **Môi trường:** JDK 21 (Eclipse Temurin) + Maven Cache.
   - **Kiểm tra chất lượng mã nguồn:** Checkstyle, Spotbugs và rà soát các nguyên tắc SOLID.
   - **Quét bảo mật tự động:** Gitleaks phát hiện lộ lọt Secrets/API keys; Semgrep SAST kiểm tra SQL Injection và XSS.
   - **Kiểm thử tích hợp (Testcontainers):** Tự động khởi chạy container PostgreSQL 16 thật và Redis 7 thật trong bộ test suite, kiểm tra tính đúng đắn của RLS và Redisson Lock mà không dùng H2 giả lập.
2. **Frontend CI (`.github/workflows/frontend-ci.yml`):**
   - **Môi trường:** Node.js 22 LTS + npm Cache.
   - **Kiểm tra kiểu tĩnh:** `tsc -b` xác nhận 100% type safety.
   - **Kiểm tra Bundle:** `vite build` kiểm tra kích thước chunk, tree-shaking và không có lỗi styling.

---

## 8. Mối Liên Kết Toàn Bộ Hồ Sơ Thiết Kế Dự Án (Design Suite Cross-Reference)

Tài liệu Kiến trúc này đóng vai trò là kim chỉ nam phối hợp chặt chẽ với:
- 🔌 **Tài liệu Thiết kế API:** [`docs/designs/api-design/`](../api-design/) (8 modules chi tiết từ `01-tenant-and-store-api.md` đến `08-notification-api.md`).
- 🗄️ **Tài liệu Thiết kế CSDL:** [`docs/designs/db-design/`](../db-design/) (Master ERD, 20 bảng chuẩn hóa và chính sách phân quyền RLS).
- 📋 **Tài liệu Đặc tả Nghiệp vụ SRS:** [`docs/designs/srs/`](../srs/) (Ma trận phân quyền, 8 luồng nghiệp vụ `BF-01` ➜ `BF-08`).
- 📱 **Tài liệu Phân tích Prototype:** [`docs/prototype/`](../../prototype/) (73 màn hình Mobile Web PWA & 32 màn hình Kiosk POS Touch Terminal).
- ⚡ **Sổ Tay Câu Lệnh Prompt Phát Triển:** [`docs/END_TO_END_PROMPTS.md`](../../END_TO_END_PROMPTS.md) (Bộ câu lệnh chuẩn hóa cho AI Agent).
