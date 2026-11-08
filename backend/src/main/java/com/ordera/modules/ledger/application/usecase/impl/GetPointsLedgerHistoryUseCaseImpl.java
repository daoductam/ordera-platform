package com.ordera.modules.ledger.application.usecase.impl;

import com.ordera.common.api.PageResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.ledger.application.usecase.GetPointsLedgerHistoryUseCase;
import com.ordera.modules.ledger.infrastructure.mapper.LedgerMapper;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.ledger.infrastructure.persistence.repository.PointsLedgerJpaRepository;
import com.ordera.modules.ledger.presentation.dto.PointsLedgerEntryResponse;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPointsLedgerHistoryUseCaseImpl implements GetPointsLedgerHistoryUseCase {

    private final PointsLedgerJpaRepository pointsLedgerRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final LedgerMapper ledgerMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PointsLedgerEntryResponse> execute(Pageable pageable) {
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

        Page<PointsLedgerJpaEntity> entityPage = pointsLedgerRepository
                .findByTenantIdAndMemberIdOrderByCreatedAtDesc(tenantId, profile.getId(), pageable);

        List<PointsLedgerEntryResponse> mappedContent = ledgerMapper.toEntryResponseList(entityPage.getContent());

        return PageResponse.from(entityPage, mappedContent);
    }
}

