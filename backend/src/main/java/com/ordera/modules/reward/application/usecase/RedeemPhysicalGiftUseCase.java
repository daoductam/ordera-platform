package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.RedeemGiftResponse;

import java.util.UUID;

public interface RedeemPhysicalGiftUseCase {

    RedeemGiftResponse execute(UUID giftId);
}
