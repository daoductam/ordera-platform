package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.AvailableOffersResponse;

import java.util.UUID;

public interface GetAvailableOffersForCartUseCase {

    AvailableOffersResponse execute(UUID cartId);
}
