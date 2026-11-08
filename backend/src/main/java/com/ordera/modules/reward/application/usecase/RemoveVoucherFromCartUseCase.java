package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.CartVoucherCalculationResponse;
import com.ordera.modules.reward.presentation.dto.RemoveVoucherRequest;

public interface RemoveVoucherFromCartUseCase {

    CartVoucherCalculationResponse execute(RemoveVoucherRequest request);
}
