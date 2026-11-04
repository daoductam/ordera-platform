Started at:  2026/11/03 23:33:00
Finished at: 2026/11/03 23:37:00
Total time: 4 minutes
---

# Architecture: Ordera Loyalty Program 2.0

## Source
- **Vision:** [2026-11-03-vision.md](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-vision.md)
- **Requirements:** [2026-11-03-requirements.md](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-requirements.md)

---

## 1. High-Level Component Architecture

Hệ thống được thiết kế theo mô hình **Modular Architecture (Headless Loyalty & Ordering)**, tách bạch hoàn toàn giữa tầng Trải nghiệm Người dùng (Frontend Clients) và tầng Dịch vụ Xử lý Nghiệp vụ lõi (Backend Loyalty Core).

```mermaid
flowchart TD
    subgraph Clients["Frontend Clients (React + Vite + Tailwind)"]
        MobileClient["Web Mobile App (Customer PWA)"]
        KioskClient["Kiosk POS Web App (In-Store Touchscreen)"]
    end

    subgraph Gateway["API Gateway / BFF"]
        APIGateway["Reverse Proxy / API Gateway<br>(JWT Auth, Rate Limiting, CORS)"]
    end

    subgraph CoreServices["Backend Core Services (REST API)"]
        AuthSvc["Auth & Identity Service<br>(OTP SMS/Email, JWT)"]
        LoyaltySvc["Loyalty & Tier Engine<br>(6 Tiers, Progress Calculation)"]
        LedgerSvc["Points Ledger Service<br>(Atomic Double-Entry Transactions)"]
        RewardSvc["Rewards & Voucher Engine<br>(Catalog, Voucher Wallet, Discounts)"]
        OrderSvc["Order & Cart Service<br>(Stores, Catalog, Checkout, Tips)"]
        NotifySvc["Notification & Ticket Service<br>(In-app Alert, Claim Missing Points)"]
    end

    subgraph DataLayer["Storage & Cache"]
        Postgres[(PostgreSQL Primary<br>ACID Transaction, Relations)]
        RedisCache[(Redis Cache<br>OTP, Rate Limit, Cart Session)]
    end

    MobileClient -->|HTTPS / REST| APIGateway
    KioskClient -->|HTTPS / REST| APIGateway

    APIGateway --> AuthSvc
    APIGateway --> LoyaltySvc
    APIGateway --> RewardSvc
    APIGateway --> OrderSvc
    APIGateway --> NotifySvc

    LoyaltySvc --> LedgerSvc
    RewardSvc --> LedgerSvc
    OrderSvc --> LoyaltySvc

    AuthSvc --> RedisCache
    AuthSvc --> Postgres
    LoyaltySvc --> Postgres
    LedgerSvc --> Postgres
    RewardSvc --> Postgres
    OrderSvc --> Postgres
```

### Chi tiết các thành phần (Components)

| Thành phần | Trách nhiệm chính | Công nghệ đề xuất | Giao thức |
|---|---|---|---|
| **Web Mobile App** | Trải nghiệm khách hàng di động (8 module Figma: Cửa hàng, Hạng thẻ, Đổi quà, Giỏ hàng) | React 19, Vite, Tailwind CSS, TypeScript, Lucide Icons | HTTPS / REST |
| **Kiosk Web App** | Giao diện màn hình cảm ứng tại cửa hàng (Tích điểm nhanh SĐT, Chọn tip, Tự hủy phiên 60s) | React 19, Vite, Tailwind CSS (Tối ưu giao diện cảm ứng lớn) | HTTPS / REST |
| **API Gateway / BFF** | Định tuyến, kiểm tra xác thực JWT, giải phóng tải tĩnh, chống brute-force OTP | Express / Fastify / Nginx Gateway | HTTPS / REST |
| **Auth Service** | Quản lý người dùng, sinh/kiểm tra OTP 6 số qua SMS/Email, cấp phát Access/Refresh Token | Node.js / TypeScript | REST + Redis |
| **Loyalty & Tier Engine** | Quản lý 6 cấp bậc thành viên (New -> Ruby), tính toán thăng/giáng hạng và tỷ lệ tích điểm | Node.js / TypeScript | REST + PostgreSQL |
| **Points Ledger Service** | Ghi nhận biến động điểm (Audit log), đảm bảo giao dịch nguyên tử, chống gian lận điểm | Node.js / TypeScript | PostgreSQL Transactions |
| **Rewards & Voucher Engine** | Quản lý kho voucher, xử lý trừ điểm đổi quà, kiểm tra điều kiện áp mã trong giỏ hàng | Node.js / TypeScript | REST + PostgreSQL |
| **Order & Cart Service** | Quản lý danh mục món ăn, cửa hàng, giỏ hàng, áp mã ưu đãi và chốt đơn hàng | Node.js / TypeScript | REST + PostgreSQL |

---

## 2. Data Architecture (Kiến trúc Dữ liệu)

### 2.1 Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--|| MEMBER_PROFILES : "has"
    TIER_LEVELS ||--o{ MEMBER_PROFILES : "categorizes"
    MEMBER_PROFILES ||--o{ POINTS_LEDGER : "records"
    VOUCHERS ||--o{ USER_VOUCHERS : "claimed_as"
    MEMBER_PROFILES ||--o{ USER_VOUCHERS : "owns"
    STORES ||--o{ ORDERS : "places_at"
    USERS ||--o{ ORDERS : "creates"
    ORDERS ||--o{ ORDER_ITEMS : "contains"
    PRODUCTS ||--o{ ORDER_ITEMS : "references"
    ORDERS ||--o| USER_VOUCHERS : "applies"

    USERS {
        uuid id PK
        string phone UK
        string email UK
        string password_hash
        string status
        timestamp created_at
    }

    MEMBER_PROFILES {
        uuid id PK
        uuid user_id FK
        string full_name
        date dob
        string tier_id FK
        int current_points
        int lifetime_points
        timestamp tier_updated_at
    }

    TIER_LEVELS {
        string code PK "NEW, SILVER, GOLD, PLATINUM, DIAMOND, RUBY"
        string name
        int min_points
        int max_points
        decimal earn_rate "1.0, 1.2, 1.5, 2.0, 2.5, 3.0"
        jsonb perks
    }

    POINTS_LEDGER {
        uuid id PK
        uuid profile_id FK
        string transaction_type "EARN, REDEEM, EXPIRE, ADJUST"
        int points_delta
        int balance_after
        string reference_id "order_id or voucher_id"
        string note
        timestamp created_at
    }

    VOUCHERS {
        uuid id PK
        string code UK
        string title
        string discount_type "PERCENT, FIXED_AMOUNT, FREE_ITEM"
        decimal discount_value
        int points_cost
        decimal min_order_value
        timestamp valid_from
        timestamp valid_to
    }

    USER_VOUCHERS {
        uuid id PK
        uuid voucher_id FK
        uuid profile_id FK
        string status "ACTIVE, USED, EXPIRED"
        uuid used_in_order_id FK
        timestamp redeemed_at
    }

    STORES {
        uuid id PK
        string name
        string address
        string phone
        boolean is_active
    }

    ORDERS {
        uuid id PK
        uuid user_id FK
        uuid store_id FK
        string order_type "TAKEAWAY, DELIVERY, KIOSK"
        string status "PENDING, PAID, COMPLETED, CANCELLED"
        decimal subtotal
        decimal discount_amount
        decimal tip_amount
        decimal total_amount
        int points_earned
        timestamp created_at
    }
```

### 2.2 Chiến lược Dữ liệu (Database Decisions)

| Hạng mục | Quyết định | Lý do & Lợi ích |
|---|---|---|
| **Hệ quản trị CSDL** | **PostgreSQL 16+** | Hỗ trợ chuẩn ACID nghiêm ngặt cho giao dịch điểm, hỗ trợ kiểu dữ liệu JSONB cho cấu hình linh hoạt (Perks, Voucher conditions), index phong phú. |
| **Chiến lược sinh ID (ID Strategy)** | **UUID v7** | Kết hợp giữa tính ngẫu nhiên (chống lộ số lượng đơn/khách hàng) và tính sắp xếp theo thời gian (Time-ordered), giúp tối ưu hiệu năng B-Tree Index vượt trội so với UUID v4. |
| **Cơ chế Sổ cái Điểm (Ledger Mechanism)** | **Append-Only Ledger (Bất biến)** | Số dư `current_points` trên Profile chỉ là bản sao tổng hợp (Materialized View / Cached column). Nguồn dữ liệu chân thực (Single Source of Truth) là bảng `POINTS_LEDGER`. Mọi thao tác đều dùng `SELECT ... FOR UPDATE` khi giao dịch. |
| **Caching Layer** | **Redis 7+** | Lưu mã OTP (TTL 3 phút), rate-limit counters theo IP/SĐT, và phiên Kiosk tạm thời. |

---

## 3. Sequence Diagrams cho các luồng nghiệp vụ trọng yếu

### 3.1 Luồng Tích điểm & Checkout tại quầy Kiosk

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách tại Kiosk
    participant Kiosk as Kiosk Web App
    participant Gateway as API Gateway
    participant Auth as Auth Service
    participant OrderSvc as Order & Cart Service
    participant Loyalty as Loyalty Core
    participant DB as PostgreSQL

    Customer->>Kiosk: Nhập SĐT tích điểm
    Kiosk->>Gateway: POST /api/v1/auth/kiosk-lookup { phone }
    Gateway->>Auth: Kiểm tra SĐT
    Auth->>DB: Query User & Profile by Phone
    alt User đã có tài khoản
        DB-->>Auth: Profile info (Name, Tier, Points)
        Auth-->>Kiosk: Trả về Profile & Hạng thẻ
        Kiosk-->>Customer: Hiển thị lời chào & Hạng (Bỏ qua bước đăng ký)
    else User chưa có tài khoản
        Auth-->>Kiosk: User Not Found
        Kiosk-->>Customer: Cho phép nhập Tên hoặc Bấm "Bỏ qua (Skip)"
        Customer->>Kiosk: Bấm Bỏ qua
        Kiosk-->>Kiosk: Gắn Phone vào giỏ hàng vãng lai
    end

    Customer->>Kiosk: Chọn Tip & Bấm Thanh toán (Pay)
    Kiosk->>Gateway: POST /api/v1/orders/checkout { cart, phone, tip }
    Gateway->>OrderSvc: Xử lý Đơn hàng
    OrderSvc->>DB: Tạo bản ghi Order
    OrderSvc->>Loyalty: Tính điểm tích lũy dựa trên Tier
    Loyalty->>DB: INSERT INTO POINTS_LEDGER (Cộng điểm)
    OrderSvc-->>Kiosk: Đặt hàng thành công + Mã Order + In hóa đơn
    Kiosk-->>Customer: Hiển thị Order Number & Bắt đầu đếm ngược 60s
    Note over Kiosk: Sau 60s hoặc khi bấm "Start Over", tự động dọn sạch phiên và trở về trang chủ
```

### 3.2 Luồng Đổi Voucher và Áp dụng trong Giỏ hàng

```mermaid
sequenceDiagram
    autonumber
    actor User as Thành viên
    participant App as Web Mobile App
    participant RewardSvc as Rewards Service
    participant Ledger as Ledger Service
    participant DB as PostgreSQL

    User->>App: Bấm "Đổi thưởng" (Voucher 50K - Cần 500 điểm)
    App->>RewardSvc: POST /api/v1/rewards/redeem { voucherId }
    RewardSvc->>DB: BEGIN TRANSACTION
    RewardSvc->>DB: SELECT current_points FROM MEMBER_PROFILES WHERE id = ? FOR UPDATE
    alt Không đủ điểm
        DB-->>RewardSvc: current_points < 500
        RewardSvc-->>App: Lỗi: Số dư điểm không khả dụng
    else Đủ điểm (>= 500)
        RewardSvc->>Ledger: Ghi bản ghi POINTS_LEDGER (-500 điểm, REDEEM)
        RewardSvc->>DB: INSERT INTO USER_VOUCHERS (profile_id, voucher_id, status='ACTIVE')
        RewardSvc->>DB: UPDATE MEMBER_PROFILES SET current_points = current_points - 500
        RewardSvc->>DB: COMMIT TRANSACTION
        RewardSvc-->>App: Đổi thành công, voucher đã đưa vào My Rewards
    end

    User->>App: Mở Checkout & Chọn áp dụng voucher vừa đổi
    App->>App: Kiểm tra điều kiện min_order_value
    App-->>User: Cập nhật ngay số tiền được giảm & Tổng thanh toán mới
```

---

## 4. Architecture Decision Records (ADRs)

| # | Quyết định (Decision) | Phương án thay thế | Lý do lựa chọn (Rationale) |
|---|---|---|---|
| **ADR-1** | Cấu trúc **Frontend Single-Page App (React + Vite)** cho cả Web Mobile và Kiosk | Next.js Server-Side Rendering (SSR) | Web Mobile và Kiosk là ứng dụng nội bộ/khách hàng tương tác sau khi scan QR/tại quầy, không yêu cầu SEO công cộng phức tạp. Vite mang lại tốc độ phản hồi cực nhanh, bundle siêu gọn và chạy mượt trên mọi màn hình cảm ứng di động/kiosk. |
| **ADR-2** | Dùng **UUID v7** làm Khóa chính (Primary Key) | Auto-increment INT hoặc UUID v4 | Chống lại việc dự đoán mã đơn hàng hoặc tổng số lượng thành viên (Security/Business privacy), đồng thời giữ trật tự theo thời gian để tối ưu hóa việc phân trang và ghi đĩa trên PostgreSQL. |
| **ADR-3** | Mô hình **Points Ledger (Audit Trail bất biến)** | Cột `user.points` đơn thuần | Đảm bảo 100% tính kiểm toán tài chính, truy vết nguồn gốc từng điểm số được sinh ra hoặc tiêu thụ, giải quyết triệt để vấn đề gian lận hoặc khiếu nại thiếu điểm. |
| **ADR-4** | UI Component Architecture theo **shadcn/ui + Tailwind CSS** | Ant Design / Material UI | Cực kỳ nhẹ, dễ dàng tùy biến 100% theo đúng design tokens của Figma (màu thẻ Silver, Gold, Platinum, Ruby, bo góc, bóng mờ drop-shadow), không bị phụ thuộc vào thư viện bên thứ 3 cồng kềnh. |

---

## 5. Domain Skills Referenced
- **`api-design`:** Phân tách rõ ràng giữa Public Client API và Internal Service contracts.
- **`database-design`:** Chuẩn hóa lược đồ quan hệ 3NF, cơ chế khóa bản ghi bi-directional cho Points Ledger, chiến lược phân chỉ mục (Index) trên `user_id`, `profile_id`, `created_at`.
- **`log-processing`:** Định nghĩa các sự kiện kiểm toán trọng yếu để phục vụ giám sát hệ thống.

