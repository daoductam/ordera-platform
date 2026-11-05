package com.ordera.modules.loyalty.presentation.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusResponse {

    private UUID memberId;
    private UUID userId;
    private String currentTierName;
    private int currentTierOrder;
    private String themeColorHex;
    private int currentPoints;
    private int lifetimePoints;
    private TierUpgradeProgressResponse upgradeProgress;
}
