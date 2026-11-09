package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.MarkAllNotificationsReadUseCase;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.MarkAllReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation cho UseCase 8.5: Đánh dấu tất cả thông báo trong hộp thư của hội viên là đã đọc.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và Batch Persist để tối ưu hiệu năng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkAllNotificationsReadUseCaseImpl implements MarkAllNotificationsReadUseCase {

    private final SystemNotificationJpaRepository systemNotificationJpaRepository;
    private final UserNotificationJpaRepository userNotificationJpaRepository;

    @Override
    @Transactional
    public MarkAllReadResponse execute(UUID tenantId, UUID userId) {
        validateInputs(tenantId, userId);

        long markedCount = markUnreadNotifications(tenantId, userId);
        log.info("Marked {} notifications as read for user {} in tenant {}", markedCount, userId, tenantId);

        return MarkAllReadResponse.builder()
                .markedCount(markedCount)
                .unreadCount(0)
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

    private long markUnreadNotifications(UUID tenantId, UUID userId) {
        Instant now = Instant.now();
        List<UUID> activeIds = systemNotificationJpaRepository.findActiveNotificationIds(tenantId, now);
        if (activeIds.isEmpty()) {
            return 0;
        }

        List<UserNotificationJpaEntity> existingRecords =
                userNotificationJpaRepository.findByTenantIdAndUserId(tenantId, userId);

        Map<UUID, UserNotificationJpaEntity> recordMap = existingRecords.stream()
                .collect(Collectors.toMap(UserNotificationJpaEntity::getNotificationId, Function.identity(), (a, b) -> a));

        List<UserNotificationJpaEntity> toSave = new ArrayList<>();
        long count = 0;

        for (UUID notifId : activeIds) {
            UserNotificationJpaEntity record = recordMap.get(notifId);
            if (record != null) {
                if (!record.isRead()) {
                    record.setRead(true);
                    record.setReadAt(now);
                    toSave.add(record);
                    count++;
                }
            } else {
                UserNotificationJpaEntity newRecord = UserNotificationJpaEntity.builder()
                        .userId(userId)
                        .notificationId(notifId)
                        .read(true)
                        .readAt(now)
                        .createdAt(now)
                        .build();
                newRecord.setTenantId(tenantId);
                toSave.add(newRecord);
                count++;
            }
        }

        if (!toSave.isEmpty()) {
            userNotificationJpaRepository.saveAll(toSave);
        }

        return count;
    }
}

