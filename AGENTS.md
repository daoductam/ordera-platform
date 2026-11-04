# AGENTS.md — Ordera Multi-Tenant Loyalty & Ordering Platform

## 1. Business Goal & Purpose
Ordera là nền tảng quản trị khách hàng thân thiết và đặt món đa thương hiệu (Multi-Tenant Loyalty & Ordering Platform) phục vụ ngành F&B / Nhà hàng (chuỗi, đại lý như Linn Sushi Sentrum). 
Hệ thống cung cấp trải nghiệm số khép kín:
- **Khách hàng:** Mobile Web PWA đặt món (nhận tại quầy/bàn), tích lũy điểm thưởng theo 6 hạng thẻ, đổi quà và áp mã giảm giá.
- **Tại quầy:** Chế độ Kiosk màn hình chạm toàn màn hình cho khách tích điểm nhanh qua SĐT và tip.
- **Thương hiệu / Đại lý:** Quản lý menu, voucher, cấu hình tỷ lệ tích điểm và báo cáo giao dịch độc lập.

## 2. Tech Stack
- **Frontend (`frontend/`):** React 19, TypeScript, Vite, Tailwind CSS, Lucide Icons, Context API (`CartContext`).
- **Backend (`backend/`):** Java 21 LTS, Spring Boot 3.3.5 (Spring Security 6, Spring Data JPA, Flyway, MapStruct, Lombok, SpringDoc OpenAPI 3.0, Redisson).
- **Database & Cache:** PostgreSQL 16 (Hỗ trợ Row-Level Security RLS cho Multi-tenant) + Redis 7 (Cache token blacklist, cooldown OTP 120s, distributed locks).
- **Hạ tầng & DevOps (`infra/`):** Docker, Docker Compose, Nginx Reverse Proxy (SSL & Same-Origin Routing), MailHog (Mock SMTP cho local dev).
- **CI/CD (`.github/workflows/`):** GitHub Actions tự động hóa:
  - `backend-ci.yml`: Maven build, JDK 21, quét lỗ hổng Gitleaks, chạy unit/integration tests.
  - `frontend-ci.yml`: Node 22, kiểm tra kiểu tĩnh TypeScript (`tsc -b`), build production bundle.

## 3. High-Level Architecture
Hệ thống được tổ chức theo mô hình **Phân tầng Doanh nghiệp (Enterprise Polyglot Structure)**:
- **`frontend/`:** Giao diện khách hàng và kiosk (port 5173).
- **`backend/`:** **Modular Monolith** áp dụng **Clean Architecture** (Presentation -> Application -> Domain -> Infrastructure) với 6 domain modules:
  1. `tenant`: Quản lý thương hiệu, đại lý và phân quyền chi nhánh.
  2. `auth`: Đăng ký, đăng nhập, sinh/kiểm tra OTP SMS/Email và cấp phát JWT.
  3. `loyalty`: Động cơ 6 cấp bậc thành viên (`New` -> `Silver` -> `Gold` -> `Platinum` -> `Diamond` -> `Ruby`) và tiến độ thăng hạng.
  4. `ledger`: Sổ cái điểm kép bất biến (`points_ledger`) chống gian lận điểm.
  5. `reward`: Kho voucher, quà tặng tumbler và quy tắc áp mã giỏ hàng.
  6. `order`: Giỏ hàng, tính thuế VAT, tips, hình thức nhận quầy/bàn và E-Receipt.
- **`infra/`:** Nginx định tuyến `/api/*` vào Backend (port 8080) và `/*` vào Frontend.

## 4. Core Features (Figma 2.0 Mapping)
- **Nhóm 01:** Khám phá & Chọn cửa hàng (Store discovery, danh mục món ăn, chi tiết sản phẩm).
- **Nhóm 02 & 03:** Đăng nhập & Đăng ký xác thực thành viên qua số điện thoại / email OTP.
- **Nhóm 04 & 05:** Chi tiết hồ sơ David Nguyen, 6 hạng thẻ thành viên, chỉnh sửa thông tin cá nhân và khiếu nại thiếu điểm.
- **Nhóm 06:** Giỏ hàng (`ReorderCartView`), Checkout tính thuế/tips, và hóa đơn điện tử E-Receipt.
- **Nhóm 07:** Ví `My Rewards`, đổi quà `Get More Rewards` (voucher 100 NOK, -30%, cốc tumbler), áp dụng voucher thành công (`07.08`), và trạng thái rỗng (`07.05`, `07.06`).
- **Nhóm 08:** Drawer chọn ưu đãi trực tiếp trong luồng Checkout.
- **Kiosk POS:** Giao diện cảm ứng toàn màn hình tại quầy thu ngân.

## 5. Project Conventions & Security Rules
- **Multi-Tenant Isolation (Bắt buộc):** Mọi truy vấn database nghiệp vụ đều phải lọc theo `tenant_id` qua `TenantContextHolder` (ngăn chặn 100% lỗ hổng IDOR).
- **Immutable Points Accounting:** Tuyệt đối không update trực tiếp cột điểm. Mọi biến động đều phải ghi vào `points_ledger` kèm `reference_id`.
- **Code Quality:** Áp dụng nguyên tắc SOLID, cấu trúc kiểm thử AAA (Arrange - Act - Assert), không dùng magic numbers/strings, tuân thủ Clean Architecture.

