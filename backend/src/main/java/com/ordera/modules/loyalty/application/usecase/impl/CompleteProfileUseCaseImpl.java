package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.CompleteProfileUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
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
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }

        if (request.getFullName() == null || request.getFullName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(LoyaltyErrorCode.MISSING_REQUIRED_FIELDS);
        }

        UserJpaEntity user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            boolean emailExists = userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, request.getEmail().trim(), userId);
            if (emailExists) {
                throw new BusinessException(LoyaltyErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail().trim());
        }

        user.setFullName(request.getFullName().trim());
        user.setEmailVerified(true);
        userRepository.save(user);

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);

        String tierName = "New";
        if (profile.getCurrentTierId() != null) {
            tierName = tierDefinitionRepository.findByIdAndTenantId(profile.getCurrentTierId(), tenantId)
                    .map(TierDefinitionJpaEntity::getTierName)
                    .orElse("New");
        }

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

