package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.application.usecase.SubmitSupportClaimUseCase;
import com.ordera.modules.loyalty.domain.constant.LoyaltyConstants;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.domain.model.SupportClaim;
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
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Tiếp nhận khiếu nại thiếu điểm hóa đơn trong vòng 30 ngày (Support Claim Submission).
 * Áp dụng Orchestrator Pattern điều phối các bước xác thực và tạo phiếu khiếu nại.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitSupportClaimUseCaseImpl implements SubmitSupportClaimUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final SupportClaimJpaRepository supportClaimRepository;

    @Override
    @Transactional
    public SupportClaimResponse execute(SubmitSupportClaimRequest request) {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();

        MemberProfileJpaEntity profile = fetchMemberProfile(tenantId, userId);
        validateReceiptDate(request.getOrderDate());

        String receiptCode = request.getReceiptCode().trim();
        validateReceiptUniqueness(tenantId, receiptCode);

        SupportClaimJpaEntity claim = createSupportClaim(tenantId, profile.getId(), request, receiptCode);
        SupportClaimJpaEntity saved = supportClaimRepository.save(claim);
        log.info("Support claim {} submitted for member {} with receiptCode {}", saved.getId(), profile.getId(), receiptCode);

        return buildResponse(saved);
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

    private MemberProfileJpaEntity fetchMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private void validateReceiptDate(LocalDate receiptDate) {
        if (receiptDate == null) {
            throw new BusinessException(LoyaltyErrorCode.MISSING_REQUIRED_FIELDS, "Ngày lập hóa đơn không được để trống.");
        }

        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        if (receiptDate.isAfter(now)) {
            throw new BusinessException(LoyaltyErrorCode.INVALID_RECEIPT_DATE);
        }

        if (receiptDate.isBefore(now.minusDays(LoyaltyConstants.MAX_ALLOWED_RECEIPT_DAYS))) {
            throw new BusinessException(
                    LoyaltyErrorCode.RECEIPT_EXPIRED,
                    LoyaltyErrorCode.RECEIPT_EXPIRED.getDefaultMessage(),
                    Map.of("orderDate", receiptDate.toString(), "maxAllowedDays", LoyaltyConstants.MAX_ALLOWED_RECEIPT_DAYS)
            );
        }
    }

    private void validateReceiptUniqueness(UUID tenantId, String receiptCode) {
        if (supportClaimRepository.existsByTenantIdAndReceiptCode(tenantId, receiptCode)) {
            throw new BusinessException(LoyaltyErrorCode.CLAIM_ALREADY_EXISTS);
        }
    }

    private SupportClaimJpaEntity createSupportClaim(
            UUID tenantId,
            UUID memberId,
            SubmitSupportClaimRequest request,
            String receiptCode
    ) {
        SupportClaimJpaEntity claim = SupportClaimJpaEntity.builder()
                .storeId(request.getStoreId())
                .memberId(memberId)
                .receiptCode(receiptCode)
                .orderDate(request.getOrderDate())
                .billAmount(request.getBillAmount())
                .billPhotoUrl(request.getBillPhotoUrl())
                .status(LoyaltyConstants.CLAIM_STATUS_PENDING)
                .reviewerNote(request.getNote())
                .createdAt(Instant.now())
                .build();
        claim.setTenantId(tenantId);
        return claim;
    }

    private SupportClaimResponse buildResponse(SupportClaimJpaEntity saved) {
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
