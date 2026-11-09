Started at:  2026/11/09 22:04:00
Finished at: 2026/11/09 22:21:00
Total time: 17 minutes
---

# Walkthrough: Phân Hệ Hộp Thư Thông Báo & Hỗ Trợ Khách Hàng (Notification & Support Module)

## 1. Tổng Quan Triển Khai (Implementation Summary)

Chúng tôi đã hoàn thành toàn bộ mã nguồn Backend cho Phân hệ Thông Báo & Hỗ Trợ Khách Hàng (`com.ordera.modules.notification`) theo đúng chuẩn **Clean Architecture 4 Lớp**, tuân thủ nghiêm ngặt các quy tắc thiết kế kiến trúc doanh nghiệp:
1. **Multi-Tenant Data Isolation:** Mọi truy vấn đọc/ghi đều lọc bắt buộc qua `tenant_id` từ `TenantContextHolder` và header `X-Tenant-ID`.
2. **Cơ chế Fan-out on Read:**
   - Các thông báo hệ thống / chiến dịch lưu tại `system_notifications` (1 bản ghi/tenant).
   - Trạng thái đọc của hội viên lưu tại `user_notifications` (`is_read`, `read_at`).
   - Tự động đánh dấu đã đọc khi xem chi tiết (Figma 05.03).
   - Hỗ trợ đánh dấu 1 tin đã đọc hoặc toàn bộ hộp thư ("Read all").
3. **Tiếp nhận Phiếu Khiếu Nại Thiếu Điểm (Missing Points Inquiry):**
   - Hỗ trợ cả hội viên và khách vãng lai (chưa đăng nhập).
   - Tự động sinh mã phiếu đối soát định dạng `INQ-YYYYMMDD-XXX` (ví dụ: `INQ-20261109-001`).
   - Validate thông tin liên hệ: Email RFC 5322, SĐT E.164, Note 5-500 ký tự, Tiền >= 0.00.
4. **Không Magic Numbers / Magic Strings:** Gom tập trung về `NotificationConstants` và `NotificationMessages`.
5. **Mã lỗi chuẩn hóa:** Khai báo qua `NotificationErrorCode` implement `ErrorCode`, ném qua `BusinessException`.

---

## 2. Danh Mục Files Đã Tạo & Chỉnh Sửa (Artifacts)

| Tầng Kiến Trúc | File Path | Mục Đích / Trách Nhiệm |
|---|---|---|
| **Domain** | `backend/.../notification/domain/constant/NotificationConstants.java` | Khai báo hằng số phân hệ |
| **Domain** | `backend/.../notification/domain/exception/NotificationErrorCode.java` | Khai báo mã lỗi RFC 7807 |
| **Domain** | `backend/.../notification/domain/model/NotificationCategory.java` | Enum: MESSAGES, NEWS, VOUCHER, GIFTS |
| **Domain** | `backend/.../notification/domain/model/InquiryStatus.java` | Enum: PENDING, IN_REVIEW, RESOLVED, REJECTED |
| **Domain** | `backend/.../notification/domain/model/SystemNotification.java` | Domain model bài viết thông báo |
| **Domain** | `backend/.../notification/domain/model/UserNotification.java` | Domain model trạng thái đọc |
| **Domain** | `backend/.../notification/domain/model/SupportInquiry.java` | Domain model phiếu khiếu nại |
| **Infrastructure** | `backend/.../notification/infrastructure/persistence/repository/SystemNotificationJpaRepository.java` | Spring Data JPA repository |
| **Infrastructure** | `backend/.../notification/infrastructure/persistence/repository/UserNotificationJpaRepository.java` | Spring Data JPA repository |
| **Infrastructure** | `backend/.../notification/infrastructure/persistence/repository/SupportInquiryJpaRepository.java` | Spring Data JPA repository |
| **Infrastructure** | `backend/.../notification/infrastructure/mapper/NotificationDtoMapper.java` | MapStruct DTO/Entity mapper |
| **Presentation** | `backend/.../notification/presentation/constant/NotificationMessages.java` | Tập trung chuỗi thông điệp |
| **Presentation** | `backend/.../notification/presentation/dto/request/SubmitInquiryRequest.java` | Request DTO gửi phiếu đối soát |
| **Presentation** | `backend/.../notification/presentation/dto/response/UnreadCountResponse.java` | DTO đếm chưa đọc |
| **Presentation** | `backend/.../notification/presentation/dto/response/NotificationSummaryResponse.java` | DTO danh sách thông báo |
| **Presentation** | `backend/.../notification/presentation/dto/response/NotificationDetailResponse.java` | DTO chi tiết thông báo |
| **Presentation** | `backend/.../notification/presentation/dto/response/MarkReadResponse.java` | DTO đánh dấu 1 tin đã đọc |
| **Presentation** | `backend/.../notification/presentation/dto/response/MarkAllReadResponse.java` | DTO đánh dấu tất cả đã đọc |
| **Presentation** | `backend/.../notification/presentation/dto/response/SubmitInquiryResponse.java` | DTO tiếp nhận phiếu hỗ trợ |
| **Application** | `backend/.../notification/application/usecase/GetUnreadNotificationCountUseCase.java` | Interface UseCase 8.1 |
| **Application** | `backend/.../notification/application/usecase/GetNotificationsListUseCase.java` | Interface UseCase 8.2 |
| **Application** | `backend/.../notification/application/usecase/GetNotificationDetailUseCase.java` | Interface UseCase 8.3 |
| **Application** | `backend/.../notification/application/usecase/MarkNotificationReadUseCase.java` | Interface UseCase 8.4 |
| **Application** | `backend/.../notification/application/usecase/MarkAllNotificationsReadUseCase.java` | Interface UseCase 8.5 |
| **Application** | `backend/.../notification/application/usecase/SubmitMissingPointsInquiryUseCase.java` | Interface UseCase 8.6 |
| **Application** | `backend/.../notification/application/usecase/impl/GetUnreadNotificationCountUseCaseImpl.java` | Triển khai UseCase 8.1 |
| **Application** | `backend/.../notification/application/usecase/impl/GetNotificationsListUseCaseImpl.java` | Triển khai UseCase 8.2 |
| **Application** | `backend/.../notification/application/usecase/impl/GetNotificationDetailUseCaseImpl.java` | Triển khai UseCase 8.3 |
| **Application** | `backend/.../notification/application/usecase/impl/MarkNotificationReadUseCaseImpl.java` | Triển khai UseCase 8.4 |
| **Application** | `backend/.../notification/application/usecase/impl/MarkAllNotificationsReadUseCaseImpl.java` | Triển khai UseCase 8.5 |
| **Application** | `backend/.../notification/application/usecase/impl/SubmitMissingPointsInquiryUseCaseImpl.java` | Triển khai UseCase 8.6 |
| **Presentation** | `backend/.../notification/presentation/controller/NotificationController.java` | REST Controller APIs 8.1 - 8.5 |
| **Presentation** | `backend/.../notification/presentation/controller/SupportInquiryController.java` | REST Controller API 8.6 |
| **Configuration** | `backend/.../config/SecurityConfig.java` | Thêm whitelist route public |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/GetUnreadNotificationCountUseCaseTest.java` | 4 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/GetNotificationsListUseCaseTest.java` | 4 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/GetNotificationDetailUseCaseTest.java` | 5 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/MarkNotificationReadUseCaseTest.java` | 3 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/MarkAllNotificationsReadUseCaseTest.java` | 3 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/application/usecase/SubmitMissingPointsInquiryUseCaseTest.java` | 7 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/presentation/controller/NotificationControllerTest.java` | 6 tests |
| **Unit Tests** | `backend/src/test/java/.../notification/presentation/controller/SupportInquiryControllerTest.java` | 1 test |
| **Docs** | `docs/plans/notification/business-flows.md` | Tài liệu Mermaid Dark Mode cho 6 Use Cases |

---

## 3. Kết Quả Kiểm Thử (Test Execution Results)

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.ordera.modules.notification.application.usecase.GetNotificationDetailUseCaseTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.application.usecase.GetNotificationsListUseCaseTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.application.usecase.GetUnreadNotificationCountUseCaseTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.application.usecase.MarkAllNotificationsReadUseCaseTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.application.usecase.MarkNotificationReadUseCaseTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.application.usecase.SubmitMissingPointsInquiryUseCaseTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.presentation.controller.NotificationControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.ordera.modules.notification.presentation.controller.SupportInquiryControllerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
- **Tổng số tests:** 33 tests
- **Tỷ lệ pass:** 100% (0 failures, 0 errors, 0 skipped).

