package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.RedeemVoucherResponse;

import java.util.UUID;

public interface RedeemVoucherUseCase {

    RedeemVoucherResponse execute(UUID voucherId);
}
