package com.ordera.modules.notification.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.GetUnreadNotificationCountUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.UnreadCountResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUnreadNotificationCountUseCase Unit Tests")
class GetUnreadNotificationCountUseCaseTest {

    @Mock
    private SystemNotificationJpaRepository systemNotificationJpaRepository;

    @Mock
    private UserNotificationJpaRepository userNotificationJpaRepository;

    @InjectMocks
    private GetUnreadNotificationCountUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Should return positive unread count when active notifications exceed read ones")
    void should_return_positive_unread_count_when_active_exceed_read() {
        // Arrange
        when(systemNotificationJpaRepository.countActiveNotifications(eq(tenantId), any(Instant.class)))
                .thenReturn(5L);
        when(userNotificationJpaRepository.countReadNotifications(tenantId, userId))
                .thenReturn(2L);

        // Act
        UnreadCountResponse response = useCase.execute(tenantId, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUnreadCount()).isEqualTo(3L);
        verify(systemNotificationJpaRepository).countActiveNotifications(eq(tenantId), any(Instant.class));
        verify(userNotificationJpaRepository).countReadNotifications(tenantId, userId);
    }

    @Test
    @DisplayName("Should return 0 unread count when all notifications have been read")
    void should_return_zero_when_all_notifications_read() {
        // Arrange
        when(systemNotificationJpaRepository.countActiveNotifications(eq(tenantId), any(Instant.class)))
                .thenReturn(4L);
        when(userNotificationJpaRepository.countReadNotifications(tenantId, userId))
                .thenReturn(4L);

        // Act
        UnreadCountResponse response = useCase.execute(tenantId, userId);

        // Assert
        assertThat(response.getUnreadCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_id_null() {
        assertThatThrownBy(() -> useCase.execute(null, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when userId is null")
    void should_throw_when_user_id_null() {
        assertThatThrownBy(() -> useCase.execute(tenantId, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.UNAUTHORIZED);
    }
}

