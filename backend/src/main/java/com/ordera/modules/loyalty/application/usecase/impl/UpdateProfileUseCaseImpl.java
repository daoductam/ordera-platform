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

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UpdateProfileResponse execute(UpdateProfileRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
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
        userRepository.save(user);

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

