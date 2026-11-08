package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.TierDefinitionResponse;

import java.util.List;

public interface GetTierDefinitionsUseCase {

    List<TierDefinitionResponse> execute();
}

