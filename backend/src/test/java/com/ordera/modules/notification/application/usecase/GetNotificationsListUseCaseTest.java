package com.ordera.modules.notification.application.usecase;

import com.ordera.common.api.PageResponse;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.GetNotificationsListUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.NotificationSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetNotificationsListUseCase Unit Tests")
class GetNotificationsListUseCaseTest {

    @Mock
    private SystemNotificationJpaRepository systemNotificationJpaRepository;

    @Mock
    private UserNotificationJpaRepository userNotificationJpaRepository;

    @Mock
    private NotificationDtoMapper notificationDtoMapper;

    @InjectMocks
    private GetNotificationsListUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID notifId1 = UUID.randomUUID();
    private final UUID notifId2 = UUID.randomUUID();

    @Test
    @DisplayName("Should return paginated notifications list with accurate read status")
    void should_return_notifications_list_with_read_status() {
        // Arrange
        SystemNotificationJpaEntity entity1 = SystemNotificationJpaEntity.builder()
                .title("Missing Points?")
                .category("Messages")
                .build();
        entity1.setId(notifId1);
        entity1.setTenantId(tenantId);

        SystemNotificationJpaEntity entity2 = SystemNotificationJpaEntity.builder()
                .title("System Update")
                .category("Messages")
                .build();
        entity2.setId(notifId2);
        entity2.setTenantId(tenantId);

        Page<SystemNotificationJpaEntity> page = new PageImpl<>(List.of(entity1, entity2));
        when(systemNotificationJpaRepository.findActiveByTenantIdAndCategory(eq(tenantId), eq("Messages"), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        when(userNotificationJpaRepository.findReadNotificationIds(tenantId, userId))
                .thenReturn(Set.of(notifId1));

        NotificationSummaryResponse summary1 = NotificationSummaryResponse.builder().id(notifId1).isRead(true).build();
        NotificationSummaryResponse summary2 = NotificationSummaryResponse.builder().id(notifId2).isRead(false).build();

        when(notificationDtoMapper.toSummaryResponse(entity1, true)).thenReturn(summary1);
        when(notificationDtoMapper.toSummaryResponse(entity2, false)).thenReturn(summary2);

        // Act
        PageResponse<NotificationSummaryResponse> result =
                useCase.execute(tenantId, userId, "MESSAGES", null, 0, 20);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).isRead()).isTrue();
        assertThat(result.getContent().get(1).isRead()).isFalse();
        verify(systemNotificationJpaRepository).findActiveByTenantIdAndCategory(eq(tenantId), eq("Messages"), any(Instant.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by isRead = false when requested")
    void should_filter_unread_notifications() {
        // Arrange
        SystemNotificationJpaEntity entity1 = SystemNotificationJpaEntity.builder().title("Msg 1").build();
        entity1.setId(notifId1);
        SystemNotificationJpaEntity entity2 = SystemNotificationJpaEntity.builder().title("Msg 2").build();
        entity2.setId(notifId2);

        Page<SystemNotificationJpaEntity> page = new PageImpl<>(List.of(entity1, entity2));
        when(systemNotificationJpaRepository.findActiveByTenantIdAndCategory(eq(tenantId), eq("News"), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        when(userNotificationJpaRepository.findReadNotificationIds(tenantId, userId))
                .thenReturn(Set.of(notifId1));

        when(notificationDtoMapper.toSummaryResponse(entity1, true))
                .thenReturn(NotificationSummaryResponse.builder().id(notifId1).isRead(true).build());
        when(notificationDtoMapper.toSummaryResponse(entity2, false))
                .thenReturn(NotificationSummaryResponse.builder().id(notifId2).isRead(false).build());

        // Act
        PageResponse<NotificationSummaryResponse> result =
                useCase.execute(tenantId, userId, "NEWS", false, 0, 20);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(notifId2);
        assertThat(result.getContent().getFirst().isRead()).isFalse();
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_null() {
        assertThatThrownBy(() -> useCase.execute(null, userId, "MESSAGES", null, 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when userId is null")
    void should_throw_when_user_null() {
        assertThatThrownBy(() -> useCase.execute(tenantId, null, "MESSAGES", null, 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.UNAUTHORIZED);
    }
}

