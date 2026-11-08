package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.loyalty.application.usecase.GetTierDefinitionsUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.domain.model.MemberTier;
import com.ordera.modules.loyalty.infrastructure.mapper.LoyaltyDtoMapper;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.TierDefinitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetTierDefinitionsUseCaseImpl implements GetTierDefinitionsUseCase {

    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final LoyaltyDtoMapper loyaltyDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TierDefinitionResponse> execute() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        List<TierDefinitionJpaEntity> tiers = tierDefinitionRepository.findByTenantIdOrderByTierOrderAsc(tenantId);
        if (tiers != null && !tiers.isEmpty()) {
            return loyaltyDtoMapper.toTierResponseList(tiers);
        }

        // Fallback default 6 tiers from MemberTier enum
        return Arrays.stream(MemberTier.values())
                .map(tier -> TierDefinitionResponse.builder()
                        .tierName(tier.getDisplayName())
                        .tierOrder(tier.getOrder())
                        .minPointsRequired(tier.getMinPointsRequired())
                        .themeColorHex(tier.getThemeColorHex())
                        .earnPointsRate(tier.getEarnPointsRate())
                        .benefitsSummary(List.of("Hệ số tích điểm x" + tier.getEarnPointsRate()))
                        .build())
                .toList();
    }
}

