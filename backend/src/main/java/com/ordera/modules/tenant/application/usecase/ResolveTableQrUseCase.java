package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.ResolvedTableQrResponse;

public interface ResolveTableQrUseCase {

    ResolvedTableQrResponse execute(String qrToken);
}

