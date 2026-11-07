# Kiến Trúc Hệ Thống Chuẩn Doanh Nghiệp (Enterprise System Architecture)
## Ordera Multi-Tenant Loyalty & Ordering Platform

Chào mừng bạn đến với trung tâm tài liệu Kiến trúc Hệ thống của nền tảng **Ordera Multi-Tenant Loyalty & Ordering Platform**.

Tài liệu này xác lập toàn bộ các nguyên tắc thiết kế, cấu trúc phân tầng, phân vùng dữ liệu, và mô hình bảo mật áp dụng thống nhất trên toàn bộ hệ thống.

---

## 📑 Tài Liệu Kiến Trúc Cốt Lõi

- 🏛️ [**`enterprise-architecture.md`**](./enterprise-architecture.md) — **Bản vẽ Kiến trúc Toàn diện (System Architecture Document - SAD)**:
  1. **Tầm nhìn & Định hình Hệ thống:** Sơ đồ luồng dữ liệu 4 tầng (Client Tier, Edge Gateway Tier, Backend Tier, Data & Storage Tier).
  2. **Tổ chức Thư mục Chuẩn Enterprise:** Bố cục mã nguồn `frontend/`, `backend/`, `infra/`, `.github/`, `docs/`.
  3. **Kiến trúc Chi tiết Backend Clean Architecture 4 Lớp:**
     - `presentation`: REST Controller, DTOs, Bean Validation.
     - `application`: UseCase interfaces, UseCase implementations, Domain Services.
     - `domain`: Pure POJO Entities, Value Objects, Domain Exceptions.
     - `infrastructure`: JPA Entities, Spring Data Repositories, Redisson Locks, Adapters.
  4. **Kế toán Điểm Kép Bất Biến (Immutable Points Ledger Engine):** Cơ chế bảo toàn điểm thưởng chống gian lận và khóa phân tán Redisson chống race-condition.
  5. **Bảo Mật Phân Lập Dữ Liệu Multi-Tenant:** `TenantContextHolder` (ThreadLocal), `TenantFilter` và PostgreSQL Row-Level Security (RLS).
  6. **Chuẩn Phong Bì & Từ Điển Lỗi RFC 7807:** `ApiResponse<T>` và mã lỗi chuẩn hóa.
  7. **Hạ Tầng Cục Bộ Docker Compose:** Cấu hình 5 dịch vụ (`gateway`, `backend`, `frontend`, `postgres`, `redis`, `mailhog`).
  8. **Tự Động Hóa CI/CD:** GitHub Actions workflows và kiểm thử tích hợp bằng Testcontainers.

---

## 🔗 Liên Kết Phối Hợp Các Khối Thiết Kế

Tài liệu Kiến trúc này được đặt ngang hàng và liên kết trực tiếp với:
- 🔌 [**Tài liệu Thiết kế RESTful API**](../api-design/): Đặc tả 8 phân hệ API và trạm Kiosk POS.
- 🗄️ [**Tài liệu Thiết kế Cơ sở Dữ liệu PostgreSQL**](../db-design/): Sơ đồ Master ERD, 20 bảng CSDL và script phân vùng.
- 📋 [**Tài liệu Đặc tả Yêu cầu Nghiệp vụ SRS**](../srs/): Ma trận phân quyền 7 Actors và 8 Luồng nghiệp vụ cốt lõi.
- 📱 [**Tài liệu Phân tích Prototype**](../../prototype/): 73 màn hình Mobile Web PWA & 32 màn hình Kiosk POS Touch Terminal.
- ⚡ [**Sổ Tay Câu Lệnh Prompt Phát Triển**](../../END_TO_END_PROMPTS.md): Kịch bản phát triển tự động hóa cho AI Agent.
