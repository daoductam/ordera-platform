package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.CompleteProfileUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.domain.model.MemberTier;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.CompleteProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.CompleteProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Điều phối hoàn thiện hồ sơ hội viên sau bước xác thực OTP (Onboarding).
 * Tuân thủ Orchestrator Pattern: execute() điều phối các bước, phân rã chi tiết thành helper methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteProfileUseCaseImpl implements CompleteProfileUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CompleteProfileResponse execute(CompleteProfileRequest request) {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        validateRequest(request);

        UserJpaEntity user = fetchUser(userId, tenantId);
        MemberProfileJpaEntity profile = fetchMemberProfile(userId, tenantId);

        updateUser(user, request, tenantId, userId);
        updateProfile(profile, request);

        String tierName = resolveTierName(profile.getCurrentTierId(), tenantId);

        return buildResponse(profile, user, tierName);
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

    private void validateRequest(CompleteProfileRequest request) {
        if (request == null
                || request.getFullName() == null || request.getFullName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(LoyaltyErrorCode.MISSING_REQUIRED_FIELDS);
        }
    }

    private UserJpaEntity fetchUser(UUID userId, UUID tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private MemberProfileJpaEntity fetchMemberProfile(UUID userId, UUID tenantId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private void updateUser(UserJpaEntity user, CompleteProfileRequest request, UUID tenantId, UUID userId) {
        String newEmail = request.getEmail().trim();
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(newEmail)) {
            boolean emailExists = userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, newEmail, userId);
            if (emailExists) {
                throw new BusinessException(LoyaltyErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(newEmail);
        }

        user.setFullName(request.getFullName().trim());
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    private void updateProfile(MemberProfileJpaEntity profile, CompleteProfileRequest request) {
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);
    }

    private String resolveTierName(UUID tierId, UUID tenantId) {
        if (tierId == null) {
            return MemberTier.NEW.getDisplayName();
        }
        return tierDefinitionRepository.findByIdAndTenantId(tierId, tenantId)
                .map(TierDefinitionJpaEntity::getTierName)
                .orElse(MemberTier.NEW.getDisplayName());
    }

    private CompleteProfileResponse buildResponse(MemberProfileJpaEntity profile, UserJpaEntity user, String tierName) {
        return CompleteProfileResponse.builder()
                .memberId(profile.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .isProfileComplete(true)
                .currentTier(tierName)
                .currentPoints(profile.getCurrentPoints())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
