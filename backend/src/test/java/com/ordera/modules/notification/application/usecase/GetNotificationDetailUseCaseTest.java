package com.ordera.modules.notification.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.GetNotificationDetailUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.NotificationDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetNotificationDetailUseCase Unit Tests")
class GetNotificationDetailUseCaseTest {

    @Mock
    private SystemNotificationJpaRepository systemNotificationJpaRepository;

    @Mock
    private UserNotificationJpaRepository userNotificationJpaRepository;

    @Mock
    private NotificationDtoMapper notificationDtoMapper;

    @InjectMocks
    private GetNotificationDetailUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notifId = UUID.randomUUID();

    @Test
    @DisplayName("Should return notification detail and automatically mark as read")
    void should_return_detail_and_auto_mark_as_read() {
        // Arrange
        SystemNotificationJpaEntity entity = SystemNotificationJpaEntity.builder()
                .title("Missing Points?")
                .content("Detailed instructions...")
                .build();
        entity.setId(notifId);
        entity.setTenantId(tenantId);

        when(systemNotificationJpaRepository.findByIdAndTenantId(notifId, tenantId))
                .thenReturn(Optional.of(entity));
        when(userNotificationJpaRepository.findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notifId))
                .thenReturn(Optional.empty());

        NotificationDetailResponse expectedResponse = NotificationDetailResponse.builder()
                .id(notifId)
                .title("Missing Points?")
                .isRead(true)
                .build();
        when(notificationDtoMapper.toDetailResponse(entity, true)).thenReturn(expectedResponse);

        // Act
        NotificationDetailResponse response = useCase.execute(tenantId, userId, notifId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isRead()).isTrue();

        ArgumentCaptor<UserNotificationJpaEntity> captor = ArgumentCaptor.forClass(UserNotificationJpaEntity.class);
        verify(userNotificationJpaRepository).save(captor.capture());
        UserNotificationJpaEntity savedRecord = captor.getValue();
        assertThat(savedRecord.getTenantId()).isEqualTo(tenantId);
        assertThat(savedRecord.getUserId()).isEqualTo(userId);
        assertThat(savedRecord.getNotificationId()).isEqualTo(notifId);
        assertThat(savedRecord.isRead()).isTrue();
    }

    @Test
    @DisplayName("Should update existing unread record to read")
    void should_update_existing_unread_record_to_read() {
        // Arrange
        SystemNotificationJpaEntity entity = SystemNotificationJpaEntity.builder().title("Title").build();
        entity.setId(notifId);

        UserNotificationJpaEntity existing = UserNotificationJpaEntity.builder()
                .userId(userId)
                .notificationId(notifId)
                .read(false)
                .build();
        existing.setTenantId(tenantId);

        when(systemNotificationJpaRepository.findByIdAndTenantId(notifId, tenantId))
                .thenReturn(Optional.of(entity));
        when(userNotificationJpaRepository.findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notifId))
                .thenReturn(Optional.of(existing));

        when(notificationDtoMapper.toDetailResponse(entity, true))
                .thenReturn(NotificationDetailResponse.builder().id(notifId).isRead(true).build());

        // Act
        useCase.execute(tenantId, userId, notifId);

        // Assert
        assertThat(existing.isRead()).isTrue();
        assertThat(existing.getReadAt()).isNotNull();
        verify(userNotificationJpaRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw NOTIFICATION_NOT_FOUND when notification not found")
    void should_throw_when_notification_not_found() {
        when(systemNotificationJpaRepository.findByIdAndTenantId(notifId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, notifId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_id_null() {
        assertThatThrownBy(() -> useCase.execute(null, userId, notifId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when userId is null")
    void should_throw_when_user_id_null() {
        assertThatThrownBy(() -> useCase.execute(tenantId, null, notifId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.UNAUTHORIZED);
    }
}

