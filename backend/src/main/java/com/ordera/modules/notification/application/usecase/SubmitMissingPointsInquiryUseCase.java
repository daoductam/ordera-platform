package com.ordera.modules.notification.application.usecase;

import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;

import java.util.UUID;

/**
 * UseCase 8.6: Tiếp nhận phiếu yêu cầu tra cứu điểm cũ, sinh ticket_code (INQ-YYYYMMDD-XXX) cho Admin đối soát.
 */
public interface SubmitMissingPointsInquiryUseCase {

    SubmitInquiryResponse execute(UUID tenantId, UUID userId, SubmitInquiryRequest request);
}

