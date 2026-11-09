package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.api.PageResponse;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.GetNotificationsListUseCase;
import com.ordera.modules.notification.domain.constant.NotificationConstants;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SystemNotificationJpaRepository;
import com.ordera.modules.notification.infrastructure.persistence.repository.UserNotificationJpaRepository;
import com.ordera.modules.notification.presentation.dto.response.NotificationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation cho UseCase 8.2: Lấy danh sách thông báo phân trang theo tab Messages / News.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và cơ chế Fan-out on Read.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetNotificationsListUseCaseImpl implements GetNotificationsListUseCase {

    private final SystemNotificationJpaRepository systemNotificationJpaRepository;
    private final UserNotificationJpaRepository userNotificationJpaRepository;
    private final NotificationDtoMapper notificationDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationSummaryResponse> execute(
            UUID tenantId,
            UUID userId,
            String type,
            Boolean isRead,
            int page,
            int size
    ) {
        validateInputs(tenantId, userId);

        String category = normalizeCategory(type);
        Pageable pageable = buildPageable(page, size);
        Page<SystemNotificationJpaEntity> entityPage = fetchNotifications(tenantId, category, pageable);

        Set<UUID> readNotificationIds = userNotificationJpaRepository.findReadNotificationIds(tenantId, userId);
        List<NotificationSummaryResponse> mappedList = mapToSummaryResponses(entityPage.getContent(), readNotificationIds, isRead);

        return PageResponse.from(entityPage, mappedList);
    }

    private void validateInputs(UUID tenantId, UUID userId) {
        if (tenantId == null) {
            throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
        }
        if (userId == null) {
            throw new BusinessException(NotificationErrorCode.UNAUTHORIZED);
        }
    }

    private String normalizeCategory(String type) {
        if (type == null || type.isBlank()) {
            return NotificationConstants.DEFAULT_CATEGORY;
        }
        if (type.equalsIgnoreCase(NotificationConstants.CATEGORY_NEWS)) {
            return NotificationConstants.CATEGORY_NEWS;
        }
        if (type.equalsIgnoreCase(NotificationConstants.CATEGORY_MESSAGES)) {
            return NotificationConstants.CATEGORY_MESSAGES;
        }
        return type.trim();
    }

    private Pageable buildPageable(int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = size <= 0
                ? NotificationConstants.DEFAULT_PAGE_SIZE
                : Math.min(size, NotificationConstants.MAX_PAGE_SIZE);

        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Page<SystemNotificationJpaEntity> fetchNotifications(UUID tenantId, String category, Pageable pageable) {
        Instant now = Instant.now();
        return systemNotificationJpaRepository.findActiveByTenantIdAndCategory(tenantId, category, now, pageable);
    }

    private List<NotificationSummaryResponse> mapToSummaryResponses(
            List<SystemNotificationJpaEntity> entities,
            Set<UUID> readNotificationIds,
            Boolean isReadFilter
    ) {
        return entities.stream()
                .map(entity -> {
                    boolean read = readNotificationIds.contains(entity.getId());
                    return notificationDtoMapper.toSummaryResponse(entity, read);
                })
                .filter(item -> isReadFilter == null || item.isRead() == isReadFilter)
                .toList();
    }
}

