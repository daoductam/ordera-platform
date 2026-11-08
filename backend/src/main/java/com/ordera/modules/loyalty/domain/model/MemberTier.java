package com.ordera.modules.loyalty.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum MemberTier {
    NEW(1, "New", 0, "#1EB759", new BigDecimal("1.0000")),
    SILVER(2, "Silver", 200, "#00A3FF", new BigDecimal("1.2000")),
    GOLD(3, "Gold", 400, "#F5B800", new BigDecimal("1.5000")),
    PLATINUM(4, "Platinum", 1000, "#FF5B4D", new BigDecimal("1.8000")),
    DIAMOND(5, "Diamond", 2000, "#7C3AED", new BigDecimal("2.0000")),
    RUBY(6, "Ruby", 4000, "#D9148E", new BigDecimal("2.5000"));

    private final int order;
    private final String displayName;
    private final int minPointsRequired;
    private final String themeColorHex;
    private final BigDecimal earnPointsRate;

    public static MemberTier fromOrder(int order) {
        for (MemberTier tier : values()) {
            if (tier.getOrder() == order) {
                return tier;
            }
        }
        return NEW;
    }

    public static MemberTier fromName(String name) {
        if (name == null) {
            return NEW;
        }
        for (MemberTier tier : values()) {
            if (tier.name().equalsIgnoreCase(name) || tier.getDisplayName().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return NEW;
    }

    public static MemberTier determineTierFromPoints(int lifetimePoints) {
        if (lifetimePoints >= RUBY.minPointsRequired) return RUBY;
        if (lifetimePoints >= DIAMOND.minPointsRequired) return DIAMOND;
        if (lifetimePoints >= PLATINUM.minPointsRequired) return PLATINUM;
        if (lifetimePoints >= GOLD.minPointsRequired) return GOLD;
        if (lifetimePoints >= SILVER.minPointsRequired) return SILVER;
        return NEW;
    }
}
