package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.UpdateProfileUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Cập nhật thông tin cá nhân hội viên (họ tên, email, ngày sinh, giới tính, avatar).
 * Áp dụng Orchestrator Pattern điều phối các bước cập nhật.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UpdateProfileResponse execute(UpdateProfileRequest request) {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();

        UserJpaEntity user = fetchUser(userId, tenantId);
        MemberProfileJpaEntity profile = fetchMemberProfile(userId, tenantId);

        updateUser(user, request, tenantId, userId);
        updateProfile(profile, request);

        return buildResponse(profile, user);
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

    private UserJpaEntity fetchUser(UUID userId, UUID tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private MemberProfileJpaEntity fetchMemberProfile(UUID userId, UUID tenantId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private void updateUser(UserJpaEntity user, UpdateProfileRequest request, UUID tenantId, UUID userId) {
        String newEmail = request.getEmail().trim();
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(newEmail)) {
            boolean emailExists = userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, newEmail, userId);
            if (emailExists) {
                throw new BusinessException(LoyaltyErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(newEmail);
        }

        user.setFullName(request.getFullName().trim());
        userRepository.save(user);
    }

    private void updateProfile(MemberProfileJpaEntity profile, UpdateProfileRequest request) {
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(request.getAvatarUrl().trim());
        }
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);
    }

    private UpdateProfileResponse buildResponse(MemberProfileJpaEntity profile, UserJpaEntity user) {
        return UpdateProfileResponse.builder()
                .id(profile.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .avatarUrl(profile.getAvatarUrl())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
