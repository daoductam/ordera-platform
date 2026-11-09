package com.ordera.modules.notification.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.MarkAllNotificationsReadUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.MarkAllReadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkAllNotificationsReadUseCase Unit Tests")
class MarkAllNotificationsReadUseCaseTest {

    @Mock
    private SystemNotificationJpaRepository systemNotificationJpaRepository;

    @Mock
    private UserNotificationJpaRepository userNotificationJpaRepository;

    @InjectMocks
    private MarkAllNotificationsReadUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notifId1 = UUID.randomUUID();
    private final UUID notifId2 = UUID.randomUUID();

    @Test
    @DisplayName("Should mark all active unread notifications as read")
    void should_mark_all_active_notifications_as_read() {
        // Arrange
        when(systemNotificationJpaRepository.findActiveNotificationIds(eq(tenantId), any(Instant.class)))
                .thenReturn(List.of(notifId1, notifId2));

        UserNotificationJpaEntity existing = UserNotificationJpaEntity.builder()
                .userId(userId)
                .notificationId(notifId1)
                .read(false)
                .build();

        when(userNotificationJpaRepository.findByTenantIdAndUserId(tenantId, userId))
                .thenReturn(List.of(existing));

        // Act
        MarkAllReadResponse response = useCase.execute(tenantId, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMarkedCount()).isEqualTo(2L);
        assertThat(response.getUnreadCount()).isEqualTo(0L);

        ArgumentCaptor<List<UserNotificationJpaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(userNotificationJpaRepository).saveAll(captor.capture());
        List<UserNotificationJpaEntity> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(UserNotificationJpaEntity::isRead);
    }

    @Test
    @DisplayName("Should return 0 marked when no active notifications exist")
    void should_return_zero_when_no_active_notifications() {
        when(systemNotificationJpaRepository.findActiveNotificationIds(eq(tenantId), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        MarkAllReadResponse response = useCase.execute(tenantId, userId);

        assertThat(response.getMarkedCount()).isEqualTo(0L);
        assertThat(response.getUnreadCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_null() {
        assertThatThrownBy(() -> useCase.execute(null, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }
}

