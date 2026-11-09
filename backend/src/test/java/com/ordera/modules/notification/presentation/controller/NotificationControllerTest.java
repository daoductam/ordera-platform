package com.ordera.modules.notification.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.api.PageResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.*;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.presentation.constant.NotificationMessages;
import com.ordera.modules.notification.presentation.dto.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock
    private GetUnreadNotificationCountUseCase getUnreadNotificationCountUseCase;

    @Mock
    private GetNotificationsListUseCase getNotificationsListUseCase;

    @Mock
    private GetNotificationDetailUseCase getNotificationDetailUseCase;

    @Mock
    private MarkNotificationReadUseCase markNotificationReadUseCase;

    @Mock
    private MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

    @InjectMocks
    private NotificationController controller;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notifId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("8.1: Should get unread count successfully")
    void should_get_unread_count() {
        when(getUnreadNotificationCountUseCase.execute(tenantId, userId))
                .thenReturn(UnreadCountResponse.builder().unreadCount(3L).build());

        ResponseEntity<ApiResponse<UnreadCountResponse>> response =
                controller.getUnreadCount(tenantId, userId.toString(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_UNREAD_COUNT_RETRIEVED);
        assertThat(response.getBody().getData().getUnreadCount()).isEqualTo(3L);
        verify(getUnreadNotificationCountUseCase).execute(tenantId, userId);
    }

    @Test
    @DisplayName("8.2: Should list notifications successfully")
    void should_list_notifications() {
        PageResponse<NotificationSummaryResponse> pageData = PageResponse.<NotificationSummaryResponse>builder()
                .content(List.of(NotificationSummaryResponse.builder().id(notifId).title("Test").build()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(getNotificationsListUseCase.execute(tenantId, userId, "MESSAGES", null, 0, 20))
                .thenReturn(pageData);

        ResponseEntity<ApiResponse<PageResponse<NotificationSummaryResponse>>> response =
                controller.listNotifications(tenantId, userId.toString(), "MESSAGES", null, 0, 20, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_NOTIFICATIONS_LIST_RETRIEVED);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("8.3: Should get notification detail successfully")
    void should_get_notification_detail() {
        NotificationDetailResponse detail = NotificationDetailResponse.builder()
                .id(notifId)
                .title("Notice")
                .isRead(true)
                .build();

        when(getNotificationDetailUseCase.execute(tenantId, userId, notifId)).thenReturn(detail);

        ResponseEntity<ApiResponse<NotificationDetailResponse>> response =
                controller.getNotificationDetail(tenantId, userId.toString(), notifId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_NOTIFICATION_DETAIL_RETRIEVED);
        assertThat(response.getBody().getData().getId()).isEqualTo(notifId);
    }

    @Test
    @DisplayName("8.4: Should mark notification as read")
    void should_mark_notification_as_read() {
        MarkReadResponse markResponse = MarkReadResponse.builder()
                .id(notifId)
                .isRead(true)
                .readAt(Instant.now())
                .build();

        when(markNotificationReadUseCase.execute(tenantId, userId, notifId)).thenReturn(markResponse);

        ResponseEntity<ApiResponse<MarkReadResponse>> response =
                controller.markAsRead(tenantId, userId.toString(), notifId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_NOTIFICATION_MARKED_READ);
        assertThat(response.getBody().getData().isRead()).isTrue();
    }

    @Test
    @DisplayName("8.5: Should mark all notifications as read")
    void should_mark_all_notifications_as_read() {
        MarkAllReadResponse markAllResponse = MarkAllReadResponse.builder()
                .markedCount(5L)
                .unreadCount(0L)
                .build();

        when(markAllNotificationsReadUseCase.execute(tenantId, userId)).thenReturn(markAllResponse);

        ResponseEntity<ApiResponse<MarkAllReadResponse>> response =
                controller.markAllAsRead(tenantId, userId.toString(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_ALL_NOTIFICATIONS_MARKED_READ);
        assertThat(response.getBody().getData().getMarkedCount()).isEqualTo(5L);
        assertThat(response.getBody().getData().getUnreadCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when no user identity is present")
    void should_throw_unauthorized_when_no_user() {
        assertThatThrownBy(() -> controller.getUnreadCount(tenantId, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.UNAUTHORIZED);
    }
}

