package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.TierUpgradeProgressResponse;

import java.util.UUID;

public interface CalculateTierProgressUseCase {

    TierUpgradeProgressResponse execute(UUID tenantId, UUID memberId);
}

