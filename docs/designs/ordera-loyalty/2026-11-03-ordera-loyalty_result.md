Started at:  2026/11/03 23:50:00
Finished at: 2026/11/03 23:53:00
Total time: 3 minutes
---

# Design Result: Ordera Loyalty Program 2.0

## Verdict: APPROVED

Toàn bộ 4 giai đoạn thiết kế hệ thống (**Vision ➔ Requirements ➔ Architecture ➔ Contracts**) đã hoàn thành đạt chuẩn kỹ thuật cao, đáp ứng 100% các màn hình trong bản vẽ Figma `Ordera Loyalty Program 2.0 (Copy) (Copy)`.

---

## 1. Artifacts Registry

| Giai đoạn | Tài liệu | Đường dẫn (File Path) | Trạng thái |
|---|---|---|---|
| **Phase 1: Vision** | Tầm nhìn, phạm vi, stakeholders & rủi ro | [`docs/designs/ordera-loyalty/2026-11-03-vision.md`](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-vision.md) | **Approved** |
| **Phase 2: Requirements** | 14 Use Cases, 5 Business Rules, NFRs & Ràng buộc | [`docs/designs/ordera-loyalty/2026-11-03-requirements.md`](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-requirements.md) | **Approved** |
| **Phase 3: Architecture** | Modular Component Diagram, ERD, Sequence Diagrams & 4 ADRs | [`docs/designs/ordera-loyalty/2026-11-03-architecture.md`](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-architecture.md) | **Approved** |
| **Phase 4: Contracts** | 17 RESTful Endpoints (`ApiResponse<T>`), 8 Bảng DDL PostgreSQL, DTOs | [`docs/designs/ordera-loyalty/2026-11-03-contracts.md`](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-contracts.md) | **Approved** |
| **Phase 5: Finalization** | Đánh giá tổng thể & Lộ trình thực thi (Roadmap) | [`docs/designs/ordera-loyalty/2026-11-03-ordera-loyalty_result.md`](file:///d:/vibe-code/orderaapp/docs/designs/ordera-loyalty/2026-11-03-ordera-loyalty_result.md) | **Approved** |

---

## 2. Review Checklist (Rà soát toàn diện)

- [x] **Phủ kín yêu cầu giao diện Figma:** 17 Endpoints và các DTOs bao quát đủ 8 module Web Mobile (`01 Cửa hàng` ➔ `08 Checkout`) và luồng Kiosk tại quầy.
- [x] **Chuẩn hóa phản hồi API:** 100% Endpoints sử dụng vỏ bọc `ApiResponse<T>`, `ApiPaginatedResponse<T>` và `ApiErrorResponse`.
- [x] **Toàn vẹn dữ liệu giao dịch điểm:** Thiết kế Sổ cái điểm bất biến (`points_ledger`) chuẩn ACID, chống gian lận và double-spending.
- [x] **Mô hình ID phân tán:** Sử dụng UUID v7 cho toàn bộ bảng, tối ưu hiệu năng ghi đĩa và bảo vệ bí mật kinh doanh.
- [x] **Bảo mật phiên công cộng:** Cơ chế tự động đếm ngược 60s hủy phiên Kiosk (Auto-logout) tránh lộ thông tin khách hàng.

---

## 3. Implementation Roadmap (Lộ trình Triển khai Code)

Lộ trình được chia thành **4 giai đoạn thực thi rõ ràng**, ưu tiên đưa giao diện Frontend vào hoạt động với Mock Data ngay lập tức:

### 🚀 Giai đoạn 1: Khởi tạo Khung Dự án & Design System Tokens (Day 1)
- **Mục tiêu:** Tạo bộ khung source code React + Vite + Tailwind CSS chuyên nghiệp, cấu hình TypeScript, Routing và cài đặt bộ UI component.
- **Nhiệm vụ cụ thể:**
  1. Khởi tạo project `React + Vite + TypeScript` tại thư mục hiện tại.
  2. Cấu hình Tailwind CSS, Lucide Icons, cấu trúc thư mục chuẩn (`/components`, `/pages`, `/hooks`, `/services`, `/types`, `/mock`).
  3. Trích xuất màu sắc 6 hạng thẻ từ Figma vào `tailwind.config.js` (`tier-new`, `tier-silver`, `tier-gold`, `tier-platinum`, `tier-diamond`, `tier-ruby`).
  4. Tạo lớp API Client với bộ dữ liệu giả lập chuẩn `mockData.ts` dựa trên `contracts.md`.

---

### 🎨 Giai đoạn 2: Phát triển Giao diện Web Mobile - Nhóm Loyalty Core (Day 2 - 3)
- **Mục tiêu:** Dựng các màn hình cốt lõi liên quan đến thành viên và tài khoản.
- **Màn hình triển khai (Figma 02, 03, 04, 05):**
  1. **Module 02 & 03:** Modal / Trang Đăng nhập & Đăng ký xác thực OTP 6 số (giao diện đếm ngược, chuyển tab SĐT/Email).
  2. **Module 04:** Màn hình Hồ sơ & Thẻ thành viên (Hiển thị thẻ gradient đổi màu theo hạng, thanh tiến trình % thăng hạng, lịch sử biến động điểm).
  3. **Module 05:** Trung tâm thông báo và form gửi yêu cầu khiếu nại "Thiếu điểm loyalty".

---

### 🛍️ Giai đoạn 3: Phát triển Giao diện Web Mobile - Nhóm Đổi thưởng & Checkout (Day 4 - 5)
- **Mục tiêu:** Dựng luồng đặt món, đổi voucher và áp mã giảm giá trong giỏ hàng.
- **Màn hình triển khai (Figma 01, 06, 07, 08):**
  1. **Module 01:** Khám phá & chọn cửa hàng (Takeaway / Delivery), xem thực đơn theo danh mục.
  2. **Module 07:** Kho ưu đãi (Tab "My Rewards" chứa voucher đã có & Tab "Get More Rewards" đổi điểm lấy quà).
  3. **Module 08 & 06:** Giỏ hàng & Checkout (Áp dụng voucher, chọn quà tặng theo hạng, tính tổng tiền, biên nhận điện tử e-receipt).

---

### 🖥️ Giai đoạn 4: Giao diện Kiosk Màn hình Chạm & Backend Integration (Day 6+)
- **Mục tiêu:** Dựng trang Kiosk cho thiết bị cảm ứng tại quầy và kết nối Backend thật.
- **Nhiệm vụ cụ thể:**
  1. Dựng giao diện Kiosk (`/kiosk`): Numpad to rõ để nhập SĐT tích điểm, màn hình chọn tip, in hóa đơn và timer 60s đếm ngược tự động quay về trang chủ.
  2. Chuyển đổi từ `mockData` sang gọi Backend API thật khi Backend sẵn sàng.

