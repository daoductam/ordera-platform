package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.GetUnreadNotificationCountUseCase;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementation cho UseCase 8.1: Lấy số lượng thông báo chưa đọc.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và tối ưu hiệu năng đếm tức thì.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetUnreadNotificationCountUseCaseImpl implements GetUnreadNotificationCountUseCase {

    private final SystemNotificationJpaRepository systemNotificationJpaRepository;
    private final UserNotificationJpaRepository userNotificationJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse execute(UUID tenantId, UUID userId) {
        validateInputs(tenantId, userId);

        long unreadCount = calculateUnreadCount(tenantId, userId);
        log.debug("Unread notifications count for user {} in tenant {}: {}", userId, tenantId, unreadCount);

        return UnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    private void validateInputs(UUID tenantId, UUID userId) {
        if (tenantId == null) {
            throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
        }
        if (userId == null) {
            throw new BusinessException(NotificationErrorCode.UNAUTHORIZED);
        }
    }

    private long calculateUnreadCount(UUID tenantId, UUID userId) {
        Instant now = Instant.now();
        long activeCount = systemNotificationJpaRepository.countActiveNotifications(tenantId, now);
        long readCount = userNotificationJpaRepository.countReadNotifications(tenantId, userId);
        return Math.max(0, activeCount - readCount);
    }
}

