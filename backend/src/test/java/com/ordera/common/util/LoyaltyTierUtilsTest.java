package com.ordera.common.util;

import com.ordera.modules.loyalty.domain.model.MemberTier;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyTierUtilsTest {

    private UUID tenantId;
    private List<TierDefinitionJpaEntity> tiers;
    private TierDefinitionJpaEntity newTier;
    private TierDefinitionJpaEntity silverTier;
    private TierDefinitionJpaEntity goldTier;
    private TierDefinitionJpaEntity platinumTier;
    private TierDefinitionJpaEntity diamondTier;
    private TierDefinitionJpaEntity rubyTier;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        newTier = createTier("New", 1, 0, "#1EB759", new BigDecimal("1.0000"));
        silverTier = createTier("Silver", 2, 200, "#00A3FF", new BigDecimal("1.2000"));
        goldTier = createTier("Gold", 3, 400, "#F5B800", new BigDecimal("1.5000"));
        platinumTier = createTier("Platinum", 4, 1000, "#FF5B4D", new BigDecimal("1.8000"));
        diamondTier = createTier("Diamond", 5, 2000, "#7C3AED", new BigDecimal("2.0000"));
        rubyTier = createTier("Ruby", 6, 4000, "#D9148E", new BigDecimal("2.5000"));

        tiers = new ArrayList<>(List.of(newTier, silverTier, goldTier, platinumTier, diamondTier, rubyTier));
    }

    private TierDefinitionJpaEntity createTier(String name, int order, int minPoints, String color, BigDecimal rate) {
        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .tierName(name)
                .tierOrder(order)
                .minPointsRequired(minPoints)
                .themeColorHex(color)
                .earnPointsRate(rate)
                .benefitsSummary(List.of("Đặc quyền " + name))
                .build();
        tier.setId(UUID.randomUUID());
        tier.setTenantId(tenantId);
        return tier;
    }

    @Test
    @DisplayName("should calculate progress correctly for new member with 0 points")
    void should_calculate_progress_for_new_member_with_zero_points() {
        LoyaltyTierUtils.TierProgressResult result = LoyaltyTierUtils.calculateTierProgress(
                newTier.getId(),
                0,
                0,
                tiers
        );

        assertThat(result.currentTierName()).isEqualTo("New");
        assertThat(result.currentTierOrder()).isEqualTo(1);
        assertThat(result.pointsToNextTier()).isEqualTo(200);
        assertThat(result.nextTierName()).isEqualTo("Silver");
        assertThat(result.nextTierMinPoints()).isEqualTo(200);
        assertThat(result.progressPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should calculate progress correctly for silver member with 300 points (50% progress to Gold)")
    void should_calculate_progress_for_silver_member_with_300_points() {
        LoyaltyTierUtils.TierProgressResult result = LoyaltyTierUtils.calculateTierProgress(
                silverTier.getId(),
                120,
                300,
                tiers
        );

        assertThat(result.currentTierName()).isEqualTo("Silver");
        assertThat(result.currentTierOrder()).isEqualTo(2);
        assertThat(result.currentPoints()).isEqualTo(120);
        assertThat(result.lifetimePoints()).isEqualTo(300);
        assertThat(result.pointsToNextTier()).isEqualTo(100);
        assertThat(result.nextTierName()).isEqualTo("Gold");
        assertThat(result.nextTierMinPoints()).isEqualTo(400);
        assertThat(result.progressPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("should calculate progress correctly when reaching Ruby (max tier)")
    void should_calculate_progress_for_ruby_max_tier() {
        LoyaltyTierUtils.TierProgressResult result = LoyaltyTierUtils.calculateTierProgress(
                rubyTier.getId(),
                4500,
                5200,
                tiers
        );

        assertThat(result.currentTierName()).isEqualTo("Ruby");
        assertThat(result.currentTierOrder()).isEqualTo(6);
        assertThat(result.pointsToNextTier()).isEqualTo(0);
        assertThat(result.nextTierName()).isEqualTo("Ruby");
        assertThat(result.progressPercentage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("should calculate fallback progress correctly when tier list is empty")
    void should_calculate_fallback_progress_when_tiers_empty() {
        LoyaltyTierUtils.TierProgressResult result = LoyaltyTierUtils.calculateTierProgress(
                null,
                150,
                300,
                List.of()
        );

        assertThat(result.currentTierName()).isEqualTo("Silver");
        assertThat(result.nextTierName()).isEqualTo("Gold");
        assertThat(result.pointsToNextTier()).isEqualTo(100);
        assertThat(result.progressPercentage()).isEqualTo(50.0);
    }
}

