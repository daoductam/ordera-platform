package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.application.usecase.UploadAvatarUseCase;
import com.ordera.modules.loyalty.domain.constant.LoyaltyConstants;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.AvatarUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * Tải lên và cập nhật ảnh đại diện hội viên (Avatar Upload).
 * Áp dụng Orchestrator Pattern điều phối các bước xác thực file và lưu trữ URL.
 * Sử dụng hằng số tập trung từ LoyaltyConstants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadAvatarUseCaseImpl implements UploadAvatarUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;

    @Override
    @Transactional
    public AvatarUploadResponse execute(MultipartFile file) {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();

        validateFile(file);

        MemberProfileJpaEntity profile = fetchMemberProfile(tenantId, userId);

        String avatarUrl = generateAvatarUrl(userId, file.getContentType());
        updateAvatarProfile(profile, avatarUrl);

        return AvatarUploadResponse.builder()
                .avatarUrl(avatarUrl)
                .uploadedAt(profile.getUpdatedAt())
                .build();
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private UUID resolveUserId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(LoyaltyErrorCode.UNSUPPORTED_IMAGE_FORMAT, "Tệp ảnh tải lên không được để trống.");
        }

        if (file.getSize() > LoyaltyConstants.MAX_AVATAR_FILE_SIZE) {
            throw new BusinessException(LoyaltyErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !LoyaltyConstants.ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(LoyaltyErrorCode.UNSUPPORTED_IMAGE_FORMAT);
        }
    }

    private MemberProfileJpaEntity fetchMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private String generateAvatarUrl(UUID userId, String contentType) {
        String extension = getExtensionFromContentType(contentType);
        String fileName = userId + "-" + System.currentTimeMillis() + "." + extension;
        return LoyaltyConstants.DEFAULT_AVATAR_STORAGE_BASE_URL + "/" + fileName;
    }

    private void updateAvatarProfile(MemberProfileJpaEntity profile, String avatarUrl) {
        profile.setAvatarUrl(avatarUrl);
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
