package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.GetNotificationDetailUseCase;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.NotificationDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation cho UseCase 8.3: Lấy chi tiết thông báo và tự động đánh dấu đã đọc.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và tối ưu cập nhật idempotency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetNotificationDetailUseCaseImpl implements GetNotificationDetailUseCase {

    private final SystemNotificationJpaRepository systemNotificationJpaRepository;
    private final UserNotificationJpaRepository userNotificationJpaRepository;
    private final NotificationDtoMapper notificationDtoMapper;

    @Override
    @Transactional
    public NotificationDetailResponse execute(UUID tenantId, UUID userId, UUID notificationId) {
        validateInputs(tenantId, userId, notificationId);

        SystemNotificationJpaEntity entity = findNotificationOrThrow(tenantId, notificationId);
        updateReadStatus(tenantId, userId, notificationId);

        log.debug("User {} read notification {} in tenant {}", userId, notificationId, tenantId);
        return notificationDtoMapper.toDetailResponse(entity, true);
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

    private SystemNotificationJpaEntity findNotificationOrThrow(UUID tenantId, UUID notificationId) {
        return systemNotificationJpaRepository.findByIdAndTenantId(notificationId, tenantId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private void updateReadStatus(UUID tenantId, UUID userId, UUID notificationId) {
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
            return;
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
    }
}

