package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.SubmitSupportClaimRequest;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;

public interface SubmitSupportClaimUseCase {

    SupportClaimResponse execute(SubmitSupportClaimRequest request);
}

