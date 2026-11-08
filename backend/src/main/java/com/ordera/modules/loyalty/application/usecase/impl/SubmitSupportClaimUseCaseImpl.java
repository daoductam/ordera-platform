package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.application.usecase.SubmitSupportClaimUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.SupportClaimJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.SubmitSupportClaimRequest;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitSupportClaimUseCaseImpl implements SubmitSupportClaimUseCase {

    private static final int MAX_ALLOWED_RECEIPT_DAYS = 30;

    private final MemberProfileJpaRepository memberProfileRepository;
    private final SupportClaimJpaRepository supportClaimRepository;

    @Override
    @Transactional
    public SupportClaimResponse execute(SubmitSupportClaimRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }

        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        LocalDate receiptDate = request.getOrderDate();
        LocalDate now = LocalDate.now();
        if (receiptDate.isBefore(now.minusDays(MAX_ALLOWED_RECEIPT_DAYS))) {
            throw new BusinessException(
                    LoyaltyErrorCode.RECEIPT_EXPIRED,
                    LoyaltyErrorCode.RECEIPT_EXPIRED.getDefaultMessage(),
                    Map.of("orderDate", receiptDate.toString(), "maxAllowedDays", MAX_ALLOWED_RECEIPT_DAYS)
            );
        }

        String receiptCode = request.getReceiptCode().trim();
        if (supportClaimRepository.existsByTenantIdAndReceiptCode(tenantId, receiptCode)) {
            throw new BusinessException(LoyaltyErrorCode.CLAIM_ALREADY_EXISTS);
        }

        SupportClaimJpaEntity claim = SupportClaimJpaEntity.builder()
                .storeId(request.getStoreId())
                .memberId(profile.getId())
                .receiptCode(receiptCode)
                .orderDate(receiptDate)
                .billAmount(request.getBillAmount())
                .billPhotoUrl(request.getBillPhotoUrl())
                .status("PENDING")
                .reviewerNote(request.getNote())
                .createdAt(Instant.now())
                .build();
        claim.setTenantId(tenantId);

        SupportClaimJpaEntity saved = supportClaimRepository.save(claim);
        log.info("Support claim {} submitted for member {} with receiptCode {}", saved.getId(), profile.getId(), receiptCode);

        return SupportClaimResponse.builder()
                .claimId(saved.getId())
                .receiptCode(saved.getReceiptCode())
                .billAmount(saved.getBillAmount())
                .storeId(saved.getStoreId())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
