package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.OtpUtils;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.port.SmsSenderPort;
import com.ordera.modules.auth.application.usecase.SendOtpUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.OtpChannel;
import com.ordera.modules.auth.domain.model.OtpPurpose;
import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.presentation.dto.SendOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Hiện thực hóa SendOtpUseCase theo Orchestrator Pattern.
 * Phương thức execute() điều phối các bước, phân rã chi tiết thành các private helper methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendOtpUseCaseImpl implements SendOtpUseCase {

    private final OtpStoragePort otpStoragePort;
    private final SmsSenderPort smsSenderPort;
    private final OtpAuditLogRepository otpAuditLogRepository;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_SECONDS = 120L;

    @Override
    @Transactional
    public SendOtpResponse execute(SendOtpRequest request) {
        UUID tenantId = validateAndResolveTenant();
        validateRequest(request);
        checkCooldown(tenantId, request.getPhoneNumber());

        String otpCode = generateOtpCode();
        saveOtpToCache(tenantId, request.getPhoneNumber(), otpCode);
        dispatchSms(request.getPhoneNumber(), otpCode);
        recordAuditLog(tenantId, request.getPhoneNumber(), otpCode);

        return buildResponse(request.getPhoneNumber());
    }

    private UUID validateAndResolveTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateRequest(SendOtpRequest request) {
        if (request == null || request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new BusinessException(AuthErrorCode.VALIDATION_FAILED, "Số điện thoại không được để trống.");
        }
    }

    private void checkCooldown(UUID tenantId, String phoneNumber) {
        if (otpStoragePort.isCooldownActive(tenantId, phoneNumber)) {
            long remainingSeconds = otpStoragePort.getCooldownRemainingSeconds(tenantId, phoneNumber);
            long retryAfter = remainingSeconds > 0 ? remainingSeconds : OTP_TTL_SECONDS;
            String message = String.format("Vui lòng đợi thêm %d giây trước khi yêu cầu gửi lại mã OTP mới.", retryAfter);
            throw new BusinessException(AuthErrorCode.OTP_COOLDOWN_ACTIVE, message, Map.of("retryAfterSeconds", retryAfter));
        }
    }

    private String generateOtpCode() {
        return OtpUtils.generateNumericOtp(OTP_LENGTH);
    }

    private void saveOtpToCache(UUID tenantId, String phoneNumber, String otpCode) {
        otpStoragePort.saveOtp(tenantId, phoneNumber, otpCode, OTP_TTL_SECONDS);
    }

    private void dispatchSms(String phoneNumber, String otpCode) {
        String message = String.format("Mã xác thực Ordera của bạn là %s. Mã có hiệu lực trong 120 giây.", otpCode);
        smsSenderPort.sendSms(phoneNumber, message);
    }

    private void recordAuditLog(UUID tenantId, String phoneNumber, String otpCode) {
        OtpAuditLogJpaEntity auditLog = OtpAuditLogJpaEntity.builder()
                .channel(OtpChannel.SMS.name())
                .recipient(phoneNumber)
                .otpHash(OtpUtils.sha256(otpCode))
                .purpose(OtpPurpose.LOGIN.name())
                .attemptCount(0)
                .verified(false)
                .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                .build();
        auditLog.setTenantId(tenantId);
        otpAuditLogRepository.save(auditLog);
    }

    private SendOtpResponse buildResponse(String phoneNumber) {
        return SendOtpResponse.builder()
                .phoneNumber(phoneNumber)
                .maskedPhoneNumber(OtpUtils.maskPhoneNumber(phoneNumber))
                .channel(OtpChannel.SMS.name())
                .cooldownSeconds(OTP_TTL_SECONDS)
                .resendAllowedAfterSeconds(OTP_TTL_SECONDS)
                .build();
    }
}
