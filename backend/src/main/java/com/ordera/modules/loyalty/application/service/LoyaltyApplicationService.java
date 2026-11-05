package com.ordera.modules.loyalty.application.service;

import com.ordera.modules.loyalty.application.usecase.CalculateTierProgressUseCase;
import com.ordera.modules.loyalty.domain.model.MemberTier;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionRepository;
import com.ordera.modules.loyalty.presentation.dto.TierUpgradeProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyApplicationService implements CalculateTierProgressUseCase {

    private final MemberProfileRepository memberProfileRepository;
    private final TierDefinitionRepository tierDefinitionRepository;

    @Override
    @Transactional(readOnly = true)
    public TierUpgradeProgressResponse execute(UUID tenantId, UUID memberId) {
        MemberProfileJpaEntity profile = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member profile not found for id: " + memberId));

        TierDefinitionJpaEntity currentTier = tierDefinitionRepository.findById(profile.getCurrentTierId())
                .orElseThrow(() -> new IllegalArgumentException("Current tier not found"));

        int nextOrder = currentTier.getTierOrder() + 1;
        Optional<TierDefinitionJpaEntity> nextTierOpt = tierDefinitionRepository.findByTenantIdAndTierOrder(tenantId, nextOrder);

        if (nextTierOpt.isEmpty()) {
            return TierUpgradeProgressResponse.builder()
                    .nextTierName("MAX")
                    .nextTierRequiredPoints(currentTier.getMinPointsRequired())
                    .pointsNeeded(0)
                    .progressPercentage(100.0)
                    .build();
        }

        TierDefinitionJpaEntity nextTier = nextTierOpt.get();
        int pointsNeeded = Math.max(0, nextTier.getMinPointsRequired() - profile.getLifetimePoints());
        int pointsInTier = profile.getLifetimePoints() - currentTier.getMinPointsRequired();
        int tierRange = nextTier.getMinPointsRequired() - currentTier.getMinPointsRequired();
        double progressPercentage = tierRange > 0
                ? Math.min(100.0, Math.max(0.0, ((double) pointsInTier / tierRange) * 100.0))
                : 100.0;

        return TierUpgradeProgressResponse.builder()
                .nextTierName(nextTier.getTierName())
                .nextTierRequiredPoints(nextTier.getMinPointsRequired())
                .pointsNeeded(pointsNeeded)
                .progressPercentage(Math.round(progressPercentage * 10.0) / 10.0)
                .build();
    }
}
