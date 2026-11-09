package com.ordera.modules.notification.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.MarkNotificationReadUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.MarkReadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkNotificationReadUseCase Unit Tests")
class MarkNotificationReadUseCaseTest {

    @Mock
    private SystemNotificationJpaRepository systemNotificationJpaRepository;

    @Mock
    private UserNotificationJpaRepository userNotificationJpaRepository;

    @InjectMocks
    private MarkNotificationReadUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notifId = UUID.randomUUID();

    @Test
    @DisplayName("Should successfully mark notification as read")
    void should_mark_notification_as_read() {
        // Arrange
        when(systemNotificationJpaRepository.existsByIdAndTenantId(notifId, tenantId)).thenReturn(true);
        when(userNotificationJpaRepository.findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notifId))
                .thenReturn(Optional.empty());

        // Act
        MarkReadResponse response = useCase.execute(tenantId, userId, notifId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(notifId);
        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();
        verify(userNotificationJpaRepository).save(any(UserNotificationJpaEntity.class));
    }

    @Test
    @DisplayName("Should throw NOTIFICATION_NOT_FOUND when notification does not exist in tenant")
    void should_throw_when_notification_not_exists() {
        when(systemNotificationJpaRepository.existsByIdAndTenantId(notifId, tenantId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, notifId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_null() {
        assertThatThrownBy(() -> useCase.execute(null, userId, notifId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }
}

