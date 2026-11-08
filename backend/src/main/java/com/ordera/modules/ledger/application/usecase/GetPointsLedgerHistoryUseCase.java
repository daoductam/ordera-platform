package com.ordera.modules.ledger.application.usecase;

import com.ordera.common.api.PageResponse;
import com.ordera.modules.ledger.presentation.dto.PointsLedgerEntryResponse;
import org.springframework.data.domain.Pageable;

public interface GetPointsLedgerHistoryUseCase {

    PageResponse<PointsLedgerEntryResponse> execute(Pageable pageable);
}

