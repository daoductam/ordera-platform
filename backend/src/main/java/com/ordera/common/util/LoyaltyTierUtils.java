package com.ordera.common.util;

import com.ordera.modules.loyalty.domain.model.MemberTier;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Tiện ích tính toán cấp bậc thẻ thành viên và tiến trình thăng hạng (Loyalty Tier Calculator).
 */
public final class LoyaltyTierUtils {

    private LoyaltyTierUtils() {
    }

    public record TierProgressResult(
            String currentTierName,
            int currentTierOrder,
            String themeColorHex,
            BigDecimal earnPointsRate,
            int currentPoints,
            int lifetimePoints,
            int pointsToNextTier,
            String nextTierName,
            int nextTierMinPoints,
            double progressPercentage
    ) {
    }

    /**
     * Tính toán chi tiết tiến độ thăng hạng từ danh sách cấu hình TierDefinitions của thương hiệu.
     */
    public static TierProgressResult calculateTierProgress(
            UUID currentTierId,
            int currentPoints,
            int lifetimePoints,
            List<TierDefinitionJpaEntity> tierEntities
    ) {
        if (tierEntities == null || tierEntities.isEmpty()) {
            return calculateFallbackTierProgress(currentPoints, lifetimePoints);
        }

        List<TierDefinitionJpaEntity> sortedTiers = tierEntities.stream()
                .sorted(Comparator.comparingInt(TierDefinitionJpaEntity::getTierOrder))
                .toList();

        TierDefinitionJpaEntity currentTier = sortedTiers.stream()
                .filter(t -> t.getId() != null && t.getId().equals(currentTierId))
                .findFirst()
                .orElseGet(() -> sortedTiers.get(0));

        int currentIndex = sortedTiers.indexOf(currentTier);
        boolean isMaxTier = (currentIndex >= sortedTiers.size() - 1);

        if (isMaxTier) {
            return new TierProgressResult(
                    currentTier.getTierName(),
                    currentTier.getTierOrder(),
                    currentTier.getThemeColorHex(),
                    currentTier.getEarnPointsRate(),
                    currentPoints,
                    lifetimePoints,
                    0,
                    currentTier.getTierName(),
                    currentTier.getMinPointsRequired(),
                    100.0
            );
        }

        TierDefinitionJpaEntity nextTier = sortedTiers.get(currentIndex + 1);
        int pointsToNextTier = Math.max(0, nextTier.getMinPointsRequired() - lifetimePoints);

        int range = nextTier.getMinPointsRequired() - currentTier.getMinPointsRequired();
        int progressInRange = Math.max(0, lifetimePoints - currentTier.getMinPointsRequired());

        double percentage = 0.0;
        if (range > 0) {
            percentage = (double) progressInRange / range * 100.0;
            percentage = Math.min(100.0, Math.max(0.0, percentage));
            percentage = Math.round(percentage * 10.0) / 10.0;
        }

        return new TierProgressResult(
                currentTier.getTierName(),
                currentTier.getTierOrder(),
                currentTier.getThemeColorHex(),
                currentTier.getEarnPointsRate(),
                currentPoints,
                lifetimePoints,
                pointsToNextTier,
                nextTier.getTierName(),
                nextTier.getMinPointsRequired(),
                percentage
        );
    }

    /**
     * Dự phòng khi tenant chưa cấu hình tier_definitions trong CSDL.
     */
    public static TierProgressResult calculateFallbackTierProgress(int currentPoints, int lifetimePoints) {
        MemberTier current = MemberTier.determineTierFromPoints(lifetimePoints);
        boolean isRuby = (current == MemberTier.RUBY);

        if (isRuby) {
            return new TierProgressResult(
                    current.getDisplayName(),
                    current.getOrder(),
                    current.getThemeColorHex(),
                    current.getEarnPointsRate(),
                    currentPoints,
                    lifetimePoints,
                    0,
                    current.getDisplayName(),
                    current.getMinPointsRequired(),
                    100.0
            );
        }

        MemberTier next = MemberTier.fromOrder(current.getOrder() + 1);
        int pointsToNext = Math.max(0, next.getMinPointsRequired() - lifetimePoints);
        int range = next.getMinPointsRequired() - current.getMinPointsRequired();
        int progressInRange = Math.max(0, lifetimePoints - current.getMinPointsRequired());
        double percentage = range > 0 ? (double) progressInRange / range * 100.0 : 0.0;
        percentage = Math.min(100.0, Math.max(0.0, Math.round(percentage * 10.0) / 10.0));

        return new TierProgressResult(
                current.getDisplayName(),
                current.getOrder(),
                current.getThemeColorHex(),
                current.getEarnPointsRate(),
                currentPoints,
                lifetimePoints,
                pointsToNext,
                next.getDisplayName(),
                next.getMinPointsRequired(),
                percentage
        );
    }
}

