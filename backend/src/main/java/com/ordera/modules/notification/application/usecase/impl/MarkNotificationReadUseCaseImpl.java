package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.MarkNotificationReadUseCase;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.MarkReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation cho UseCase 8.4: Đánh dấu một thông báo cụ thể là đã đọc.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và tính Idempotent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkNotificationReadUseCaseImpl implements MarkNotificationReadUseCase {

    private final SystemNotificationJpaRepository systemNotificationJpaRepository;
    private final UserNotificationJpaRepository userNotificationJpaRepository;

    @Override
    @Transactional
    public MarkReadResponse execute(UUID tenantId, UUID userId, UUID notificationId) {
        validateInputs(tenantId, userId, notificationId);
        verifyNotificationExists(tenantId, notificationId);

        Instant readAt = saveOrUpdateReadRecord(tenantId, userId, notificationId);
        log.info("Marked notification {} as read for user {} in tenant {}", notificationId, userId, tenantId);

        return MarkReadResponse.builder()
                .id(notificationId)
                .isRead(true)
                .readAt(readAt)
                .build();
    }

    private void validateInputs(UUID tenantId, UUID userId, UUID notificationId) {
        if (tenantId == null) {
            throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
        }
        if (userId == null) {
            throw new BusinessException(NotificationErrorCode.UNAUTHORIZED);
        }
        if (notificationId == null) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    private void verifyNotificationExists(UUID tenantId, UUID notificationId) {
        if (!systemNotificationJpaRepository.existsByIdAndTenantId(notificationId, tenantId)) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    private Instant saveOrUpdateReadRecord(UUID tenantId, UUID userId, UUID notificationId) {
        Optional<UserNotificationJpaEntity> existingOpt =
                userNotificationJpaRepository.findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notificationId);

        Instant now = Instant.now();
        if (existingOpt.isPresent()) {
            UserNotificationJpaEntity existing = existingOpt.get();
            if (!existing.isRead()) {
                existing.setRead(true);
                existing.setReadAt(now);
                userNotificationJpaRepository.save(existing);
            }
            return existing.getReadAt() != null ? existing.getReadAt() : now;
        }

        UserNotificationJpaEntity newRecord = UserNotificationJpaEntity.builder()
                .userId(userId)
                .notificationId(notificationId)
                .read(true)
                .readAt(now)
                .createdAt(now)
                .build();
        newRecord.setTenantId(tenantId);
        userNotificationJpaRepository.save(newRecord);
        return now;
    }
}

