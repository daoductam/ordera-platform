# Implementation Walkthrough: Reward & Voucher Module

Started at:  2026/11/08 23:33:00  
Finished at: 2026/11/08 23:55:00  
Total time: 22 minutes  
---

## Execution Summary

- **Spec:** [2026-11-08-spec.md](file:///d:/vibe-code/orderaapp/docs/plans/reward/2026-11-08-spec.md)
- **Plan:** [2026-11-08-plan.md](file:///d:/vibe-code/orderaapp/docs/plans/reward/2026-11-08-plan.md)
- **Branch:** `develop`
- **Implementation approach:** TDD (Red -> Green -> Refactor)
- **Execution mode:** Sequential Parallel
- **Tasks completed:** 10/10

---

## Task Execution Log

### Task 1: Domain Models, Constants & ErrorCodes
- **Status:** Completed
- **Parallel group:** Group A
- **Sub-agent:** No — sequential
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/domain/constant/RewardConstants.java` (create)
  - `backend/src/main/java/com/ordera/modules/reward/domain/exception/RewardErrorCode.java` (create)
  - `backend/src/main/java/com/ordera/modules/reward/domain/model/LoyaltyGift.java` (modify)
  - `backend/src/main/java/com/ordera/modules/reward/domain/model/UserGiftRedemption.java` (modify)
- **Domain skills referenced:** `code-quality`, `security`
- **Key decisions:** Định nghĩa đầy đủ các hằng số không dùng magic numbers/strings, bao gồm `MAX_VOUCHERS_PER_ORDER = 1`, `MAX_PHYSICAL_GIFTS_PER_ORDER = 1`, `GIFT_REDEMPTION_CODE_LENGTH = 8`, `LOCK_ACQUIRE_TIMEOUT_SEC = 3`, `LOCK_LEASE_TIMEOUT_SEC = 5`, `LOCK_KEY_REWARD_PATTERN`.

### Task 2: Repositories, CartOfferPort, Adapter & DTO Mapper
- **Status:** Completed
- **Parallel group:** Group A
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/persistence/repository/VoucherJpaRepository.java`
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/persistence/repository/UserVoucherJpaRepository.java`
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/persistence/repository/LoyaltyGiftJpaRepository.java`
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/persistence/repository/UserGiftRedemptionJpaRepository.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/port/CartOfferPort.java`
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/adapter/CartOfferAdapter.java`
  - `backend/src/main/java/com/ordera/modules/reward/infrastructure/mapper/RewardDtoMapper.java`
  - `backend/src/main/java/com/ordera/modules/order/infrastructure/persistence/repository/CartJpaRepository.java`
  - `backend/src/main/java/com/ordera/modules/order/infrastructure/persistence/repository/CartItemJpaRepository.java`
- **Domain skills referenced:** `database-design`
- **Key decisions:** Triển khai `CartOfferPort` theo Dependency Inversion để giảm tối đa khớp nối giữa `reward` và `order`. `CartOfferAdapter` tính subtotal dựa trên số lượng và `basePrice` của sản phẩm.

### Task 3: UseCases Xem Ví, Danh Mục & Thống Kê (APIs 5.1, 5.2, 5.5)
- **Status:** Completed
- **Parallel group:** Group B
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/GetMyRewardsWalletUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/GetMyRewardsWalletUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/GetRewardsCatalogUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/GetRewardsCatalogUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/GetRewardsSummaryUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/GetRewardsSummaryUseCaseImpl.java`
- **Domain skills referenced:** `api-design`
- **Key decisions:** Tuân thủ Orchestrator Pattern, tách các hàm truy vấn `fetchUserVouchers`, `fetchUserGifts`, tính toán `canAfford` và `pointsNeeded` theo số dư điểm thực tế.

### Task 4 & Task 5: UseCases Đổi Điểm với Redisson Locks (APIs 5.3, 5.4)
- **Status:** Completed
- **Parallel group:** Group C
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/RedeemVoucherUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/RedeemVoucherUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/RedeemPhysicalGiftUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/RedeemPhysicalGiftUseCaseImpl.java`
- **Domain skills referenced:** `api-design`
- **Key decisions:**
  - `RedeemVoucherUseCase`: Khóa theo `lock:tenant:{tenantId}:reward:member:{memberId}`. Trừ điểm khả dụng qua `AccruePointsUseCase` (với `TransactionType.VOUCHER_REDEEM`), lưu bản ghi `user_vouchers` trạng thái `AVAILABLE`.
  - `RedeemPhysicalGiftUseCase`: Chiếm giữ Redisson Multi-Lock đồng thời trên Member và Gift (`lock:tenant:{tenantId}:reward:gift:{giftId}`). Trừ 1 tồn kho trong `loyalty_gifts`, sinh mã nhận quà 8 ký tự alphanumeric, trừ điểm qua `AccruePointsUseCase` (`TransactionType.GIFT_REDEEM`), tạo bản ghi `user_gift_redemptions` trạng thái `PENDING`.
  - Toàn bộ Redisson Lock đều được release trong khối `finally`.

### Task 6 & Task 7: Ưu Đãi Giỏ Hàng & Checkout (APIs 5.6 - 5.11)
- **Status:** Completed
- **Parallel group:** Group D
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/GetAvailableOffersForCartUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/GetAvailableOffersForCartUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/ValidatePromoCodeUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/ValidatePromoCodeUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/ApplyVoucherToCartUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/ApplyVoucherToCartUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/RemoveVoucherFromCartUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/RemoveVoucherFromCartUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/ApplyGiftToCartUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/ApplyGiftToCartUseCaseImpl.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/RemoveGiftFromCartUseCase.java`
  - `backend/src/main/java/com/ordera/modules/reward/application/usecase/impl/RemoveGiftFromCartUseCaseImpl.java`
- **Domain skills referenced:** `api-design`
- **Key decisions:**
  - Kiểm tra điều kiện `min_order_value` với `subtotalAmount`.
  - Khóa voucher sang `IN_USE` khi áp dụng và hoàn trả về `AVAILABLE` khi gỡ.
  - Chuyển quà sang `SELECTED` khi gán và hoàn trả về `PENDING` khi gỡ.
  - Thực thi quy tắc kết hợp: Tối đa 01 Voucher VÀ 01 Quà hiện vật cho mỗi giỏ hàng.

### Task 8: Presentation Messages, DTOs & Controllers
- **Status:** Completed
- **Parallel group:** Group E
- **Files created/modified:**
  - `backend/src/main/java/com/ordera/modules/reward/presentation/constant/RewardMessages.java`
  - 18 DTOs trong `backend/src/main/java/com/ordera/modules/reward/presentation/dto/`
  - `backend/src/main/java/com/ordera/modules/reward/presentation/controller/RewardController.java`
  - `backend/src/main/java/com/ordera/modules/reward/presentation/controller/CartOfferController.java`
  - `backend/src/main/java/com/ordera/config/SecurityConfig.java`
- **Domain skills referenced:** `api-design`
- **Key decisions:**
  - Trả về chuẩn `ApiResponse.ok(RewardMessages.MSG_*, data)`. Không hardcode chuỗi.
  - Phân quyền permitAll cho `/api/v1/rewards/**` và `/api/v1/cart/**` trong `SecurityConfig` để usecase kiểm soát lỗi `UNAUTHORIZED` 401 theo định dạng JSON chuẩn.

### Task 9: Technical Documentation
- **Status:** Completed
- **Parallel group:** Group F
- **Files created/modified:**
  - `docs/plans/reward/business-flows.md`
- **Domain skills referenced:** `sequence-diagram`
- **Key decisions:** Thiết kế Flowchart nghiệp vụ toàn cảnh và 4 Sequence Diagrams Clean Architecture chuẩn Dark Mode, mô tả rõ các tầng Client -> Controller -> UseCase -> Port -> Repository -> DB/Redis.

### Task 10: Unit & Concurrency Tests
- **Status:** Completed
- **Parallel group:** Group G
- **Files created/modified:**
  - `backend/src/test/java/com/ordera/modules/reward/domain/RewardConstantsAndErrorCodesTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/application/usecase/RedeemVoucherUseCaseTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/application/usecase/RedeemPhysicalGiftUseCaseTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/application/usecase/WalletAndCatalogUseCasesTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/application/usecase/CartOfferUseCasesTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/application/usecase/RewardConcurrencyTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/presentation/RewardControllerTest.java`
  - `backend/src/test/java/com/ordera/modules/reward/presentation/CartOfferControllerTest.java`
- **Domain skills referenced:** `test-strategy`
- **Key decisions:** Kiểm thử bao phủ toàn bộ các luồng: đủ điểm, thiếu điểm, hết tồn kho, kiểm tra tính độc nhất của mã nhận quà, quy tắc kết hợp, và kịch bản race condition 2 luồng đồng thời.

---

## Deviations from Plan

| # | Task | Planned | Actual | Reason |
|---|------|---------|--------|--------|
| - | - | - | - | Implementation followed the plan exactly. |

---

## Known Limitations

None identified. Implementation strictly follows Clean Architecture 4 layers, SOLID principles, and Ordera Multi-Tenant security rules.
