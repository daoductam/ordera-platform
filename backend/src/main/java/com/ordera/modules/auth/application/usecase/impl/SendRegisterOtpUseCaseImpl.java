package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.OtpUtils;
import com.ordera.modules.auth.application.port.EmailSenderPort;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.port.SmsSenderPort;
import com.ordera.modules.auth.application.usecase.SendRegisterOtpUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.OtpChannel;
import com.ordera.modules.auth.domain.model.OtpPurpose;
import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.SendRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendRegisterOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Hiện thực hóa SendRegisterOtpUseCase theo Orchestrator Pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendRegisterOtpUseCaseImpl implements SendRegisterOtpUseCase {

    private final UserRepository userRepository;
    private final OtpStoragePort otpStoragePort;
    private final SmsSenderPort smsSenderPort;
    private final EmailSenderPort emailSenderPort;
    private final OtpAuditLogRepository otpAuditLogRepository;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_SECONDS = 120L;

    @Override
    @Transactional
    public SendRegisterOtpResponse execute(SendRegisterOtpRequest request) {
        UUID tenantId = validateAndResolveTenant();
        validateRequest(request);
        ensureUserDoesNotExist(tenantId, request);

        String recipient = resolveRecipient(request);
        checkCooldown(tenantId, recipient);

        String otpCode = generateOtpCode();
        saveOtpToCache(tenantId, recipient, otpCode);
        dispatchNotification(request, otpCode);
        recordAuditLog(tenantId, request, recipient, otpCode);

        return buildResponse(request, recipient);
    }

    private UUID validateAndResolveTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateRequest(SendRegisterOtpRequest request) {
        if (request == null) {
            throw new BusinessException(AuthErrorCode.VALIDATION_FAILED, "Dữ liệu yêu cầu không hợp lệ.");
        }
    }

    private void ensureUserDoesNotExist(UUID tenantId, SendRegisterOtpRequest request) {
        if (userRepository.existsByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber())
                || userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new BusinessException(AuthErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private String resolveRecipient(SendRegisterOtpRequest request) {
        return OtpChannel.EMAIL.name().equalsIgnoreCase(request.getChannel())
                ? request.getEmail()
                : request.getPhoneNumber();
    }

    private void checkCooldown(UUID tenantId, String recipient) {
        if (otpStoragePort.isCooldownActive(tenantId, recipient)) {
            long remainingSeconds = otpStoragePort.getCooldownRemainingSeconds(tenantId, recipient);
            long retryAfter = remainingSeconds > 0 ? remainingSeconds : OTP_TTL_SECONDS;
            String message = String.format("Vui lòng đợi thêm %d giây trước khi yêu cầu gửi lại mã OTP mới.", retryAfter);
            throw new BusinessException(AuthErrorCode.OTP_COOLDOWN_ACTIVE, message, Map.of("retryAfterSeconds", retryAfter));
        }
    }

    private String generateOtpCode() {
        return OtpUtils.generateNumericOtp(OTP_LENGTH);
    }

    private void saveOtpToCache(UUID tenantId, String recipient, String otpCode) {
        otpStoragePort.saveOtp(tenantId, recipient, otpCode, OTP_TTL_SECONDS);
    }

    private void dispatchNotification(SendRegisterOtpRequest request, String otpCode) {
        if (OtpChannel.EMAIL.name().equalsIgnoreCase(request.getChannel())) {
            String subject = "Mã xác thực đăng ký hội viên Ordera";
            String content = String.format("Xin chào %s,\n\nMã xác thực đăng ký hội viên của bạn là: %s. Mã có hiệu lực trong 120 giây.",
                    request.getFullName(), otpCode);
            emailSenderPort.sendEmail(request.getEmail(), subject, content);
        } else {
            String message = String.format("Mã xác thực đăng ký Ordera của bạn là %s. Mã có hiệu lực trong 120 giây.", otpCode);
            smsSenderPort.sendSms(request.getPhoneNumber(), message);
        }
    }

    private void recordAuditLog(UUID tenantId, SendRegisterOtpRequest request, String recipient, String otpCode) {
        OtpAuditLogJpaEntity auditLog = OtpAuditLogJpaEntity.builder()
                .channel(request.getChannel().toUpperCase())
                .recipient(recipient)
                .otpHash(OtpUtils.sha256(otpCode))
                .purpose(OtpPurpose.REGISTER.name())
                .attemptCount(0)
                .verified(false)
                .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                .build();
        auditLog.setTenantId(tenantId);
        otpAuditLogRepository.save(auditLog);
    }

    private SendRegisterOtpResponse buildResponse(SendRegisterOtpRequest request, String recipient) {
        String masked = OtpChannel.EMAIL.name().equalsIgnoreCase(request.getChannel())
                ? OtpUtils.maskEmail(recipient)
                : OtpUtils.maskPhoneNumber(recipient);

        return SendRegisterOtpResponse.builder()
                .channel(request.getChannel().toUpperCase())
                .recipientMasked(masked)
                .cooldownSeconds(OTP_TTL_SECONDS)
                .build();
    }
}

