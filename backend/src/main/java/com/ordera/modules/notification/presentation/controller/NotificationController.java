package com.ordera.modules.notification.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.api.PageResponse;
import com.ordera.common.context.TenantContextHolder;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.notification.application.usecase.*;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.presentation.constant.NotificationMessages;
import com.ordera.modules.notification.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST Controller cho Phân hệ Hộp Thư Thông Báo (APIs 8.1 - 8.5).
 * Tuyệt đối cấm hardcode string trong ApiResponse.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "08. Notifications", description = "Hộp thư thông báo cá nhân & tin tức hệ thống (Figma 05.01 - 05.03)")
public class NotificationController {

    private final GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;
    private final GetNotificationsListUseCase getNotificationsListUseCase;
    private final GetNotificationDetailUseCase getNotificationDetailUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

    @GetMapping("/unread-count")
    @Operation(summary = "8.1: Lấy số lượng thông báo chưa đọc (render huy hiệu đỏ trên icon chuông)")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = resolveUserId(userIdHeader, principal);

        UnreadCountResponse data = getUnreadNotificationCountUseCase.execute(tenantId, userId);
        return ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_UNREAD_COUNT_RETRIEVED, data));
    }

    @GetMapping
    @Operation(summary = "8.2: Danh sách thông báo đa danh mục phân trang (Messages hoặc News)")
    public ResponseEntity<ApiResponse<PageResponse<NotificationSummaryResponse>>> listNotifications(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @RequestParam(value = "type", required = false, defaultValue = "MESSAGES") String type,
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = resolveUserId(userIdHeader, principal);

        PageResponse<NotificationSummaryResponse> data =
                getNotificationsListUseCase.execute(tenantId, userId, type, isRead, page, size);
        return ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_NOTIFICATIONS_LIST_RETRIEVED, data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "8.3: Chi tiết thông báo & tự động đánh dấu đã đọc")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> getNotificationDetail(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @PathVariable("id") UUID notificationId,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = resolveUserId(userIdHeader, principal);

        NotificationDetailResponse data = getNotificationDetailUseCase.execute(tenantId, userId, notificationId);
        return ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_NOTIFICATION_DETAIL_RETRIEVED, data));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "8.4: Đánh dấu một thông báo đã đọc")
    public ResponseEntity<ApiResponse<MarkReadResponse>> markAsRead(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @PathVariable("id") UUID notificationId,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = resolveUserId(userIdHeader, principal);

        MarkReadResponse data = markNotificationReadUseCase.execute(tenantId, userId, notificationId);
        return ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_NOTIFICATION_MARKED_READ, data));
    }

    @PutMapping("/read-all")
    @Operation(summary = "8.5: Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<ApiResponse<MarkAllReadResponse>> markAllAsRead(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = resolveUserId(userIdHeader, principal);

        MarkAllReadResponse data = markAllNotificationsReadUseCase.execute(tenantId, userId);
        return ResponseEntity.ok(ApiResponse.ok(NotificationMessages.MSG_ALL_NOTIFICATIONS_MARKED_READ, data));
    }

    private UUID resolveTenantId(UUID headerTenantId) {
        if (headerTenantId != null) {
            return headerTenantId;
        }
        UUID contextTenantId = TenantContextHolder.getTenantId();
        if (contextTenantId != null) {
            return contextTenantId;
        }
        throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
    }

    private UUID resolveUserId(String userIdHeader, Principal principal) {
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);
        if (userId == null) {
            throw new BusinessException(NotificationErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}

