# Ordera — Multi-Tenant Loyalty & Ordering Platform

<p align="center">
  <img src="https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-19.0-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React 19" />
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/PostgreSQL-16_RLS-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 16" />
  <img src="https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7" />
  <img src="https://img.shields.io/badge/Architecture-Clean_DDD--Lite-000000?style=for-the-badge" alt="Clean Architecture" />
</p>

---

## 📖 1. Giới Thiệu & Mục Tiêu Nghiệp Vụ (Overview)

**Ordera** là nền tảng quản trị khách hàng thân thiết và đặt món ẩm thực đa thương hiệu (**Multi-Tenant White-Label Loyalty & Ordering Platform**) phục vụ ngành F&B / Nhà hàng (chuỗi nhà hàng, đại lý ủy quyền). 

Hệ thống cung cấp trải nghiệm số hóa khép kín:
- **Khách hàng (Mobile Web PWA):** Tự đặt món ăn tại bàn (quét mã QR) hoặc mang đi (Takeaway), chọn tùy biến món (Size L, đá, đường, topping), tích lũy điểm thưởng theo 6 hạng thẻ, đổi voucher giảm giá, nhận quà hiện vật và xem hóa đơn điện tử E-Receipt.
- **Tại quầy (Kiosk POS Touchscreen):** Trạm cảm ứng toàn màn hình tại quầy thu ngân cho phép khách nhập số điện thoại trên bàn phím số lớn để tích điểm nhanh (`KIOSK_EARN`) không cần mật khẩu, tự động reset phiên sau 60 giây không tương tác.
- **Vận hành & Nhà hàng:** Màn hình KDS bếp nhận đơn theo thời gian thực (WebSocket), đối soát hóa đơn giấy giải quyết khiếu nại thiếu điểm, quản lý trạng thái hết món và kết xuất báo cáo thuế VAT 15% Na Uy.

> **Thương hiệu tham chiếu mẫu (Standard Demo):**  
> **Linn Sushi Sentrum** — Địa chỉ: *Prinsens gate 10C, 0153 Oslo, Norway*.

---

## 🏗️ 2. Kiến Trúc Hệ Thống (Enterprise Clean Architecture)

Hệ thống được tổ chức theo mô hình **Polyglot Monorepo** áp dụng **Clean Architecture / Hexagonal Architecture (DDD-Lite 4 Layers)**:

```
orderaapp/
├── frontend/               # React 19, TypeScript, Vite, Tailwind CSS (Port 5173)
├── backend/                # Java 21 LTS, Spring Boot 3.3.5 (Port 8080)
│   └── src/main/java/com/ordera/
│       ├── common/         # Envelope ApiResponse<T>, RFC 7807, TenantContextHolder
│       └── modules/        # 7 Phân hệ nghiệp vụ độc lập (Modular Monolith)
│           ├── tenant/     # Thương hiệu, Chi nhánh, Bàn ăn QR
│           ├── auth/       # Xác thực OTP, JWT HMAC-SHA512, Redis Blacklist
│           ├── loyalty/    # Động cơ 6 Hạng thẻ (New -> Ruby) & Tiến trình thăng hạng
│           ├── ledger/     # Sổ cái điểm bất biến (Immutable Points Ledger)
│           ├── catalog/    # Danh mục thực đơn, Món ăn, Topping Modifier
│           ├── reward/     # Ví My Rewards, Đổi Voucher, Quà Tumbler, Khóa Redisson
│           └── order/      # Giỏ hàng, Thuế VAT 15%, Tips, Checkout, E-Receipt
├── infra/                  # Hạ tầng Docker, Nginx Gateway, Scripts CSDL
└── docs/                   # Trung tâm tài liệu thiết kế & kỹ thuật toàn diện
```

### 4 Tầng Trong Từng Module Backend:
1. **`domain/` (Cốt lõi nghiệp vụ):** Pure Java POJOs, Enums nghiệp vụ (`MemberTier`, `TransactionType`), Domain Exceptions. Hoàn toàn độc lập với framework.
2. **`application/` (Các ca sử dụng):** UseCases, Services nghiệp vụ, điều phối giao tác và khóa phân tán Redisson.
3. **`infrastructure/` (Tích hợp & Lưu trữ):** JPA Entities (`*JpaEntity.java`), Spring Data Repositories, MapStruct Mappers chuyển đổi Domain $\leftrightarrow$ Entity $\leftrightarrow$ DTO.
4. **`presentation/` (Giao tiếp người dùng):** REST Controllers, Request/Response DTOs chuẩn hóa, bắt lỗi toàn cục RFC 7807.

---

## 🛡️ 3. Các Nguyên Tắc An Toàn & Bảo Mật Cốt Lõi

1. **Multi-Tenant Row-Level Security (RLS):**  
   100% Request gửi lên hệ thống đều bắt buộc mang Header `X-Tenant-ID: <UUID>`. Hibernate Interceptor thiết lập `SET LOCAL app.current_tenant_id = 'UUID'` trên từng phiên kết nối, PostgreSQL RLS tự động lọc dữ liệu ở cấp độ nhân CSDL $\rightarrow$ **Triệt tiêu 100% nguy cơ rò rỉ dữ liệu chéo đại lý (IDOR)**.
2. **Kế Toán Điểm Bất Biến (Immutable Points Accounting):**  
   Điểm thưởng được quản lý như tiền gửi ngân hàng. Nghiêm cấm tuyệt đối việc chạy lệnh UPDATE trực tiếp cột điểm. Mọi biến động đều phải chèn dòng mới vào `points_ledger` kèm `transaction_type` và `reference_id`.
3. **Chống Chi Tiêu Kép (Race Condition Prevention):**  
   Tích hợp **Redisson Distributed Lock** cho các thao tác đổi voucher và thanh toán (`lock:voucher:{id}` và `lock:order:checkout:{userId}`).
4. **Độ Chính Xác Tài Chính & Thuế VAT 15% Na Uy:**  
   Toàn bộ giá cả, thuế, tips sử dụng `BigDecimal` (scale = 2, rounding `HALF_UP`). Thuế VAT được tính ngược từ tổng tiền:
   $$\text{VAT} = \text{Total} \times \frac{15}{115}$$

---

## 📚 4. Trung Tâm Tài Liệu Dự Án (Documentation Suite)

Toàn bộ quy chuẩn kiến trúc và đặc tả đã được văn bản hóa đầy đủ trong thư mục [`docs/`](./docs/):

| Thư Mục / Tệp Tài Liệu | Mô Tả & Nội Dung Kỹ Thuật |
|:---|:---|
| 📋 [**`docs/designs/srs/`**](./docs/designs/srs/) | **Đặc tả Yêu cầu Nghiệp vụ (SRS - CarLLM Business Flow Spec)**:<br>• Phân tích chi tiết 8 Luồng Nghiệp vụ từ `BF-01` đến `BF-08`.<br>• Ma trận Phân quyền 7 Actors & Ma trận Truy vết Tính năng (RTM).<br>• Các bước tương tác chi tiết kèm kịch bản xử lý ngoại lệ (Exception Handling). |
| 🔌 [**`docs/designs/api-design/`**](./docs/designs/api-design/) | **Đặc tả 8 Phân Hệ RESTful API**:<br>• Quy chuẩn Header `X-Tenant-ID`, phong bì `ApiResponse<T>`.<br>• Từ điển 13 mã lỗi doanh nghiệp chuẩn **RFC 7807**.<br>• Chi tiết từng endpoint: Request Payload, Query/Path Params, Response 200/201. |
| 🗄️ [**`docs/designs/db-design/`**](./docs/designs/db-design/) | **Thiết Kế Chi Tiết Cơ Sở Dữ Liệu PostgreSQL 16**:<br>• Sơ đồ Master ERD và 18 bảng CSDL chuẩn hóa.<br>• Script kích hoạt bảo mật PostgreSQL Row-Level Security (RLS).<br>• Chiến lược phân vùng dữ liệu RANGE theo năm cho bảng `points_ledger`. |
| ⚡ [**`docs/END_TO_END_PROMPTS.md`**](./docs/END_TO_END_PROMPTS.md) | **Sổ Tay Câu Lệnh Prompt Phát Triển Tự Động (Prompt Playbook)**:<br>• Tập hợp các câu lệnh Prompt soạn sẵn theo từng module.<br>• Chỉ cần Copy & Paste để kích hoạt AI Agent thực thi từ phân tích đến code và commit. |
| 🏛️ [**`docs/designs/ordera-loyalty/`**](./docs/designs/ordera-loyalty/) | **Kiến Trúc Doanh Nghiệp & Hợp Đồng Dịch Vụ Tổng Thể**: Lược đồ luồng dữ liệu, tích hợp MailHog và nguyên tắc Clean Architecture. |

---

## 🚀 5. Hướng Dẫn Cài Đặt & Khởi Chạy (Quick Start)

### Yêu Cầu Môi Trường (Prerequisites)
- **Node.js:** Phiên bản 20+ hoặc 22 LTS, `npm`.
- **Java Development Kit (JDK):** JDK 21 LTS (Oracle OpenJDK hoặc Eclipse Temurin).
- **Apache Maven:** 3.9+.
- **Docker & Docker Compose:** Đã cài đặt và đang chạy.

---

### Bước 1: Khởi động Hạ tầng Docker (Postgres, Redis, MailHog, Nginx)

Khởi động toàn bộ các dịch vụ phụ trợ nền tảng chỉ với 1 câu lệnh:

```bash
# Di chuyển vào thư mục infra/docker
cd infra/docker

# Khởi chạy các container nền tảng
docker-compose up -d
```

Các dịch vụ sẽ sẵn sàng tại:
- **PostgreSQL 16:** `localhost:5432` (Database: `ordera_db`, User: `ordera_user`, Pass: `ordera_secret`)
- **Redis 7:** `localhost:6379`
- **MailHog (Web UI xem thư OTP):** [`http://localhost:8025`](http://localhost:8025)
- **Nginx Gateway:** [`http://localhost:80`](http://localhost:80)

---

### Bước 2: Khởi động Backend (Spring Boot 3.3.5)

```bash
# Di chuyển vào thư mục backend
cd backend

# Chạy kiểm thử tự động (Unit Tests)
mvn clean test

# Khởi chạy ứng dụng máy chủ
mvn spring-boot:run
```
- **Backend API URL:** `http://localhost:8080`
- **Tài liệu Swagger OpenAPI 3.0:** `http://localhost:8080/swagger-ui.html`

---

### Bước 3: Khởi động Frontend (React 19 + TypeScript)

```bash
# Mở một cửa sổ dòng lệnh mới và chạy
npm run dev

# Hoặc chạy trực tiếp từ thư mục frontend:
npm --prefix frontend run dev
```
- **Mobile Web PWA Khách hàng:** [`http://localhost:5173/`](http://localhost:5173/)
- **Trạm Cảm ứng Kiosk POS Quầy:** [`http://localhost:5173/kiosk`](http://localhost:5173/kiosk)

---

## 🧪 6. Kiểm Thử & Đảm Bảo Chất Lượng (Quality Assurance)

Dự án tuân thủ nghiêm ngặt nguyên tắc kiểm thử và tiêu chuẩn mã sạch:
- **Cấu trúc kiểm thử:** AAA Pattern (Arrange - Act - Assert).
- **Frontend Type Safety:** Kiểm tra toàn diện kiểu tĩnh TypeScript không dùng `any` bừa bãi:
  ```bash
  npm.cmd run build
  ```
- **Backend Test Suite:** Đảm bảo toàn bộ UseCase và Entity tests pass 100%:
  ```bash
  cd backend && mvn test
  ```
- **Quy chuẩn Git Flow & Commit Message:** Tuân thủ chuẩn `action-commit`:
  ```bash
  develop: [Tóm tắt thay đổi dưới 15 từ]

  Co-authored-by: AI Agent <agent@ai-kit>
  ```

---

## 👥 7. Đóng Góp & Quản Trị Dự Án

- **Nhánh phát triển chính:** `develop`
- **Nhánh phát hành ổn định:** `master`
- Mọi đóng góp tính năng mới bắt buộc phải kích hoạt qua câu lệnh mẫu trong [`docs/END_TO_END_PROMPTS.md`](./docs/END_TO_END_PROMPTS.md) để đảm bảo tuân thủ thiết kế và không phá vỡ kiến trúc Clean Architecture.
