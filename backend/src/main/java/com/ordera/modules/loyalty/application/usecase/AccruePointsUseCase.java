package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;

public interface AccruePointsUseCase {

    PointsLedgerJpaEntity execute(AccruePointsCommand command);
}

