Started at:  2026/11/08 20:56:00
Finished at: 2026/11/08 21:20:30
Total time: 24.5 minutes
---

# Implementation Walkthrough: Loyalty & Immutable Points Ledger Engine

## Execution Summary

- **Spec:** `docs/plans/loyalty/2026-11-08-spec.md`
- **Plan:** `docs/plans/loyalty/2026-11-08-plan.md`
- **Business Flows & Architecture Sequences:** `docs/plans/loyalty/business-flows.md`
- **Branch:** `develop`
- **Implementation Approach:** Standard
- **Execution Mode:** Sequential Parallel by Groups (Groups A, B, C, D, E)
- **Tasks Completed:** 12/12

---

## Task Execution Log

### Group A: Foundation, Redisson Configuration & Domain Models
- **Task 1: Add Redisson dependency & create `RedissonConfig`**
  - **Status:** Completed
  - **Files:** `backend/pom.xml`, `backend/src/main/java/com/ordera/config/RedissonConfig.java`
  - **Details:** Tích hợp `org.redisson:redisson-spring-boot-starter:3.37.0`, khởi tạo bean `RedissonClient` đọc cấu hình `spring.data.redis.host:port` với cơ chế retry và timeout an toàn.
- **Task 2: Implement Domain Models & `LoyaltyErrorCode`**
  - **Status:** Completed
  - **Files:**
    - `com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode`: Danh mục mã lỗi RFC 7807 (`MEMBER_PROFILE_NOT_FOUND`, `INVALID_TIER_TRANSITION`, `INSUFFICIENT_POINTS`, `RECEIPT_EXPIRED`, `CLAIM_ALREADY_EXISTS`, `CLAIM_NOT_FOUND`, `TENANT_HEADER_MISSING`, `FILE_TOO_LARGE`, `UNSUPPORTED_IMAGE_FORMAT`, `CONCURRENT_TRANSACTION_LOCK`, `EMAIL_ALREADY_EXISTS`, `MISSING_REQUIRED_FIELDS`).
    - Xóa bỏ class exception đơn lẻ legacy: `InvalidTierTransitionException.java`.
    - `com.ordera.modules.loyalty.domain.model.MemberTier`: 6 bậc thẻ chuẩn (`NEW`, `SILVER`, `GOLD`, `PLATINUM`, `DIAMOND`, `RUBY`) kèm helper `determineTierFromPoints`.
    - `com.ordera.modules.ledger.domain.model.TransactionType`: 8 loại biến động (`ORDER_EARN`, `VOUCHER_REDEEM`, `GIFT_REDEEM`, `TIER_BONUS`, `CLAIM_CREDIT`, `KIOSK_EARN`, `ADMIN_ADJUST`, `CLAIM_APPROVED`).
    - Cập nhật POJOs `MemberProfile`, `SupportClaim`.
- **Task 3: Implement `LoyaltyTierUtils`**
  - **Status:** Completed
  - **Files:** `backend/src/main/java/com/ordera/common/util/LoyaltyTierUtils.java`
  - **Details:** Tính toán chính xác `pointsToNextTier`, `progressPercentage` cho 6 hạng thẻ, xử lý mốc 0 điểm, mốc lưng chừng và mốc kịch trần Ruby (100%), có cơ chế fallback khi CSDL chưa có cấu hình riêng.

---

### Group B: Persistence Repositories & MapStruct Mappers
- **Task 4: Implement JPA Repositories**
  - **Status:** Completed
  - **Files:**
    - `MemberProfileJpaRepository` (& `MemberProfileRepository`)
    - `TierDefinitionJpaRepository` (& `TierDefinitionRepository`)
    - `SupportClaimJpaRepository` (& `SupportClaimRepository`)
    - `PointsLedgerJpaRepository` (& `PointsLedgerRepository`)
  - **Details:** 100% truy vấn dữ liệu được lọc theo `tenantId`, bảo đảm tương thích ngược với các module khác (`auth`, `order`).
- **Task 5: Implement MapStruct Mappers & DTOs**
  - **Status:** Completed
  - **Files:**
    - `com.ordera.common.api.PageResponse`
    - `LoyaltyDtoMapper` & `LedgerMapper`
    - Request & Response DTOs: `MemberProfileResponse`, `LoyaltyTierInfoResponse`, `QuickActionCountersResponse`, `UpdateProfileRequest`, `UpdateProfileResponse`, `CompleteProfileRequest`, `CompleteProfileResponse`, `AvatarUploadResponse`, `TierDefinitionResponse`, `SubmitSupportClaimRequest`, `SupportClaimResponse`, `PointsLedgerEntryResponse`.

---

### Group C: Application Layer Use Cases & Core Engine
- **Task 6: Implement Core `AccruePointsUseCase` Engine**
  - **Status:** Completed
  - **Files:**
    - `AccruePointsCommand`, `AccruePointsUseCase`, `AccruePointsUseCaseImpl`
  - **Details:**
    - Chiếm giữ khóa phân tán Redisson `lock:tenant:{tenantId}:member:{memberId}` (wait 3s, lease 5s), unlock an toàn trong `finally`.
    - Kiểm tra số dư khi trừ điểm (ném `INSUFFICIENT_POINTS` nếu âm).
    - Tính số dư mới `newCurrentPoints` và `newLifetimePoints`.
    - Tự động thăng hạng thẻ dựa trên `min_points_required` cấu hình trong CSDL.
    - Chèn bản ghi mới bất biến vào `points_ledger` (PostgreSQL range partitioned - cấm sửa xóa).
    - Cập nhật cache số dư trên `member_profiles`.
- **Task 7: Member Profile UseCases**
  - **Status:** Completed
  - **Files:**
    - `GetMemberProfileUseCaseImpl`: Trả về thông tin hội viên, mã Barcode ảo, bậc thẻ, điểm thưởng, bộ đếm counters, cờ khiếu nại.
    - `UpdateProfileUseCaseImpl`: Cập nhật họ tên, email, ngày sinh, giới tính, kiểm tra trùng email trong tenant.
    - `CompleteProfileUseCaseImpl`: Kích hoạt hồ sơ sau OTP đăng ký.
    - `UploadAvatarUseCaseImpl`: Upload avatar kiểm tra dung lượng $\le 5\text{MB}$ và định dạng JPEG/PNG/WebP.
- **Task 8: History, Claims & Tiers UseCases**
  - **Status:** Completed
  - **Files:**
    - `GetTierDefinitionsUseCaseImpl`: Lấy danh sách 6 hạng thẻ.
    - `GetPointsLedgerHistoryUseCaseImpl`: Phân trang lịch sử sổ cái theo `createdAt DESC`.
    - `SubmitSupportClaimUseCaseImpl`: Kiểm tra hóa đơn trong hạn 30 ngày (`RECEIPT_EXPIRED`) và kiểm tra trùng lặp mã hóa đơn (`CLAIM_ALREADY_EXISTS`), tạo ticket trạng thái `PENDING`.

---

### Group D: Presentation Layer REST Controllers
- **Task 9: Member Loyalty Controller**
  - **Status:** Completed
  - **Files:** `MemberLoyaltyController.java`
  - **Endpoints:**
    - `GET /api/v1/loyalty/members/me`
    - `PUT /api/v1/loyalty/members/me`
    - `POST /api/v1/loyalty/members/complete-profile`
    - `POST /api/v1/loyalty/members/me/avatar`
    - `GET /api/v1/loyalty/tiers`
    - `GET /api/v1/loyalty/members/me/points`
- **Task 10: Support Claim Controller**
  - **Status:** Completed
  - **Files:** `SupportClaimController.java`
  - **Endpoint:**
    - `POST /api/v1/loyalty/claims` (HTTP 201 Created)
- **Security:**
  - Cập nhật `SecurityConfig.java`: Public truy cập `/api/v1/loyalty/tiers` cho khách hàng và kiosk xem hạng thẻ, các endpoint còn lại bắt buộc JWT hội viên.

---

### Group E: Verification & Automated Tests
- **Task 11: Unit Tests for Utils & Accrue Engine**
  - **Status:** Completed
  - **Files:**
    - `LoyaltyTierUtilsTest`: 4 tests (New, Silver 50%, Ruby max tier, fallback).
    - `AccruePointsUseCaseTest`: 6 tests (cộng điểm, thăng hạng New $\rightarrow$ Silver, thăng hạng lên Ruby, trừ điểm voucher, ném `INSUFFICIENT_POINTS`, khóa Redisson và kiểm tra tính bất biến không gọi delete).
- **Task 12: Unit Tests for Controllers & UseCases**
  - **Status:** Completed
  - **Files:**
    - `SubmitSupportClaimUseCaseTest`: 3 tests (hợp lệ, quá hạn 30 ngày, trùng lặp mã).
    - `MemberLoyaltyControllerTest`: 6 tests WebMvc (me, update, complete, tiers, points history, 404 error).
    - `SupportClaimControllerTest`: 3 tests WebMvc (201 Created, 409 Conflict, 422 Unprocessable Entity).

---

## Test Suite Execution Results

```
[INFO] Results:
[INFO] 
[INFO] Tests run: 161, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  47.985 s
```

---

## Deviations from Plan

| # | Task | Planned | Actual | Reason |
|---|---|---|---|---|
| 1 | Task 2 | Xóa `InvalidTierTransitionException` | Đã xóa và bổ sung `LoyaltyErrorCode` implement `ErrorCode` | Chuẩn hóa theo kiến trúc chung và loại bỏ exception class riêng lẻ |
| 2 | Task 4 | Đổi tên Repositories | Tạo `MemberProfileJpaRepository` và để `MemberProfileRepository` kế thừa | Giữ tương thích 100% với các use cases sẵn có trong `auth` module |

---

## Post-Implementation Refactoring & Code Review (Phase 1.4 Hardening)

Sau khi hoàn tất cài đặt ban đầu, hệ thống đã thực hiện đợt rà soát chất lượng mã nguồn chuyên sâu (`/code-reviewer`) và hoàn tất các cải tiến kiến trúc:

1. **Loại bỏ hoàn toàn mã nguồn chết (Dead/Legacy Code Elimination):**
   - Đã xóa 5 file legacy chứa lỗ hổng IDOR, `IllegalArgumentException` thô và không còn sử dụng:
     * `LoyaltyApplicationService.java`
     * `CalculateTierProgressUseCase.java`
     * `TierUpgradeProgressResponse.java`
     * `MemberStatusResponse.java`
     * `LoyaltyMapper.java`

2. **Chống God Class & God Method (Orchestrator Pattern Refactoring):**
   - Refactor `CompleteProfileUseCaseImpl` và `UpdateProfileUseCaseImpl`: phương thức `execute()` rút gọn còn dưới 30 dòng, đóng vai trò nhạc trưởng điều phối các private helper methods (`findAndValidateUser`, `updateUserEntity`, `getOrCreateMemberProfile`, `validateAvatarFile`).
   - Xử lý triệt để Null-Safety: kiểm tra `user.getEmail() != null` trước khi so sánh `equalsIgnoreCase`.

3. **Tách biệt 2 Lớp Hằng Số (Domain Constants vs Presentation Messages):**
   - **`domain/constant/LoyaltyConstants.java` & `LedgerConstants.java`:** Chứa quy tắc nghiệp vụ bất biến, giới hạn dung lượng upload (5MB), MIME types hợp lệ, thời gian khóa phân tán Redisson (wait 3s, lease 5s), hạn 30 ngày hóa đơn khiếu nại, phân trang 20/50.
   - **`presentation/constant/LoyaltyMessages.java`:** Chứa toàn bộ thông điệp phản hồi API `MSG_*` cho controller bọc trong `ApiResponse.ok(...)`.

4. **Tối Ưu Hóa Khóa Phân Tán Redisson & Tránh Cạn Kiệt Connection Pool:**
   - Trong `AccruePointsUseCaseImpl`, di chuyển phạm vi giao dịch database (`TransactionTemplate`) vào bên trong khối `lock.tryLock(...)` thay vì bọc `@Transactional` ở mức phương thức bên ngoài lock, triệt tiêu rủi ro chiếm dụng connection pool của HikariCP trong thời gian chờ acquire lock.

5. **Bộ Lọc Xác Thực Toàn Cục `JwtAuthenticationFilter`:**
   - Bổ sung `JwtAuthenticationFilter` hỗ trợ trích xuất JWT Token, kiểm tra blacklist trên Redis và tự động nạp `SecurityContext` + `TenantContext`.
   - Hỗ trợ chế độ kiểm thử nhanh cho Swagger UI / Local Dev qua header `X-User-ID: <UUID>`.

6. **Kết Quả Kiểm Thử Toàn Trình:**
   - Tổng số test cases tăng lên **167 tests** (bổ sung `CompleteProfileUseCaseTest`, `UpdateProfileUseCaseTest` và cập nhật controller tests), đạt tỷ lệ **167/167 tests passed (100% BUILD SUCCESS)**.

