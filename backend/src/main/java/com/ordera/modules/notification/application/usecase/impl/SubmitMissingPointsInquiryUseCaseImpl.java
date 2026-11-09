package com.ordera.modules.notification.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.SubmitMissingPointsInquiryUseCase;
import com.ordera.modules.notification.domain.constant.NotificationConstants;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SupportInquiryJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SupportInquiryJpaRepository;
import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Implementation cho UseCase 8.6: Tiếp nhận phiếu yêu cầu tra cứu điểm cũ.
 * Áp dụng Orchestrator Pattern, Guard Clauses, và sinh mã đối soát an toàn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitMissingPointsInquiryUseCaseImpl implements SubmitMissingPointsInquiryUseCase {

    private static final Pattern PHONE_PATTERN = Pattern.compile(NotificationConstants.PHONE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(NotificationConstants.EMAIL_REGEX);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Oslo");

    private final SupportInquiryJpaRepository supportInquiryJpaRepository;
    private final NotificationDtoMapper notificationDtoMapper;

    @Override
    @Transactional
    public SubmitInquiryResponse execute(UUID tenantId, UUID userId, SubmitInquiryRequest request) {
        validateTenant(tenantId);
        validateInquiryInput(request);

        String ticketCode = generateTicketCode(tenantId);
        SupportInquiryJpaEntity savedEntity = saveInquiryRecord(tenantId, userId, ticketCode, request);

        log.info("Created support inquiry {} for tenant {} and contact {}", ticketCode, tenantId, request.getEmail());
        return notificationDtoMapper.toInquiryResponse(savedEntity);
    }

    private void validateTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
        }
    }

    private void validateInquiryInput(SubmitInquiryRequest request) {
        if (request == null) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }

        validateEmail(request.getEmail());
        validatePhone(request.getPhoneNumber());
        validateNote(request.getNote());
        validateBillAmount(request.getBillAmount());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank() || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }
    }

    private void validateNote(String note) {
        if (note == null) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }
        int length = note.trim().length();
        if (length < NotificationConstants.MIN_INQUIRY_NOTE_LENGTH ||
            length > NotificationConstants.MAX_INQUIRY_NOTE_LENGTH) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }
    }

    private void validateBillAmount(BigDecimal billAmount) {
        if (billAmount != null && billAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
        }
    }

    private String generateTicketCode(UUID tenantId) {
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Instant startOfDay = today.atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        long todayCount = supportInquiryJpaRepository.countTodayInquiries(tenantId, startOfDay, endOfDay);
        long sequenceNumber = todayCount + 1;

        String dateStr = today.format(DateTimeFormatter.ofPattern(NotificationConstants.TICKET_DATE_FORMAT));
        return String.format(NotificationConstants.TICKET_CODE_FORMAT,
                NotificationConstants.TICKET_CODE_PREFIX, dateStr, sequenceNumber);
    }

    private SupportInquiryJpaEntity saveInquiryRecord(
            UUID tenantId,
            UUID userId,
            String ticketCode,
            SubmitInquiryRequest request
    ) {
        SupportInquiryJpaEntity entity = notificationDtoMapper.toInquiryEntity(tenantId, userId, ticketCode, request);
        return supportInquiryJpaRepository.save(entity);
    }
}

