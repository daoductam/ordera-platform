package com.ordera.modules.notification.infrastructure.mapper;

import com.ordera.modules.notification.domain.model.SupportInquiry;
import com.ordera.modules.notification.domain.model.SystemNotification;
import com.ordera.modules.notification.domain.model.UserNotification;
import com.ordera.modules.notification.infrastructure.persistence.entity.SupportInquiryJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.NotificationDetailResponse;
import com.ordera.modules.notification.presentation.dto.response.NotificationSummaryResponse;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MapStruct Mapper chuyển đổi giữa Entity, Domain Model và DTO cho Phân hệ Thông Báo & Hỗ Trợ.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationDtoMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("Europe/Oslo"));

    SystemNotification toDomain(SystemNotificationJpaEntity entity);

    SystemNotificationJpaEntity toEntity(SystemNotification domain);

    UserNotification toDomain(UserNotificationJpaEntity entity);

    UserNotificationJpaEntity toEntity(UserNotification domain);

    SupportInquiry toDomain(SupportInquiryJpaEntity entity);

    SupportInquiryJpaEntity toEntity(SupportInquiry domain);

    default NotificationSummaryResponse toSummaryResponse(SystemNotificationJpaEntity entity, boolean isRead) {
        if (entity == null) {
            return null;
        }

        String formattedDate = entity.getCreatedAt() != null
                ? DATE_TIME_FORMATTER.format(entity.getCreatedAt())
                : "";

        return NotificationSummaryResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .category(entity.getCategory())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .date(formattedDate)
                .createdAt(entity.getCreatedAt())
                .isRead(isRead)
                .bannerImageUrl(entity.getBannerImageUrl())
                .actionLabel(entity.getActionLabel())
                .actionUrl(entity.getActionUrl())
                .build();
    }

    default NotificationDetailResponse toDetailResponse(SystemNotificationJpaEntity entity, boolean isRead) {
        if (entity == null) {
            return null;
        }

        return NotificationDetailResponse.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .type(entity.getType())
                .title(entity.getTitle())
                .bannerImageUrl(entity.getBannerImageUrl())
                .content(entity.getContent())
                .isRead(isRead)
                .createdAt(entity.getCreatedAt())
                .hasSupportForm(entity.isHasSupportForm())
                .supportEmail(entity.getSupportEmail())
                .supportPhone(entity.getSupportPhone())
                .build();
    }

    default SupportInquiryJpaEntity toInquiryEntity(UUID tenantId, UUID userId, String ticketCode, SubmitInquiryRequest request) {
        if (request == null) {
            return null;
        }

        SupportInquiryJpaEntity entity = new SupportInquiryJpaEntity();
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setTicketCode(ticketCode);
        entity.setEmail(request.getEmail().trim());
        entity.setPhoneNumber(request.getPhoneNumber().trim());
        entity.setReceiptCode(request.getReceiptCode() != null ? request.getReceiptCode().trim() : null);
        entity.setOrderDate(request.getOrderDate());
        entity.setBillAmount(request.getBillAmount());
        entity.setNote(request.getNote().trim());
        entity.setReceiptPhotoUrl(request.getReceiptPhotoUrl());
        entity.setStatus("PENDING");
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    default SubmitInquiryResponse toInquiryResponse(SupportInquiryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubmitInquiryResponse.builder()
                .ticketId(entity.getTicketCode())
                .status(entity.getStatus())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

