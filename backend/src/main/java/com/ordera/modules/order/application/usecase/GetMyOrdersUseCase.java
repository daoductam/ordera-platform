package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetMyOrdersUseCase {

    Page<OrderSummaryResponse> execute(UUID tenantId, UUID userId, String tab, Pageable pageable);
}

