package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.application.usecase.UploadAvatarUseCase;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadAvatarUseCaseImpl implements UploadAvatarUseCase {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final MemberProfileJpaRepository memberProfileRepository;

    @Override
    @Transactional
    public AvatarUploadResponse execute(MultipartFile file) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(LoyaltyErrorCode.UNSUPPORTED_IMAGE_FORMAT, "Tệp ảnh tải lên không được để trống.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(LoyaltyErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(LoyaltyErrorCode.UNSUPPORTED_IMAGE_FORMAT);
        }

        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        String extension = getExtensionFromContentType(contentType);
        String fileName = userId + "-" + System.currentTimeMillis() + "." + extension;
        String avatarUrl = "https://storage.ordera.no/avatars/" + fileName;

        profile.setAvatarUrl(avatarUrl);
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);

        return AvatarUploadResponse.builder()
                .avatarUrl(avatarUrl)
                .uploadedAt(profile.getUpdatedAt())
                .build();
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}

