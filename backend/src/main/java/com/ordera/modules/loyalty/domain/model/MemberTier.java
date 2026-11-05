package com.ordera.modules.loyalty.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberTier {
    NEW(1, "New", 0, "#1EB759"),
    SILVER(2, "Silver", 200, "#00A3FF"),
    GOLD(3, "Gold", 400, "#F5B800"),
    PLATINUM(4, "Platinum", 1000, "#FF5B4D"),
    DIAMOND(5, "Diamond", 2000, "#7C3AED"),
    RUBY(6, "Ruby", 4000, "#D9148E");

    private final int order;
    private final String displayName;
    private final int minPointsRequired;
    private final String themeColorHex;

    public static MemberTier fromOrder(int order) {
        for (MemberTier tier : values()) {
            if (tier.getOrder() == order) {
                return tier;
            }
        }
        return NEW;
    }
}

