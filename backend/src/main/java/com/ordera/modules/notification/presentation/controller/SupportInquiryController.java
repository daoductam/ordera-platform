package com.ordera.modules.notification.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContextHolder;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.notification.application.usecase.SubmitMissingPointsInquiryUseCase;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.presentation.constant.NotificationMessages;
import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST Controller cho Phân hệ Tiếp Nhận Khiếu Nại & Hỗ Trợ Tra Cứu Điểm (API 8.6).
 * Tuyệt đối cấm hardcode string trong ApiResponse.
 */
@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@Tag(name = "08. Support & Inquiries", description = "Tiếp nhận khiếu nại, đối soát điểm cũ và hỗ trợ khách hàng (Figma 05.03)")
public class SupportInquiryController {

    private final SubmitMissingPointsInquiryUseCase submitMissingPointsInquiryUseCase;

    @PostMapping("/missing-points/submit")
    @Operation(summary = "8.6: Gửi biểu mẫu yêu cầu hỗ trợ đối soát điểm thiếu sau khi nâng cấp hệ thống")
    public ResponseEntity<ApiResponse<SubmitInquiryResponse>> submitMissingPointsInquiry(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            @Valid @RequestBody SubmitInquiryRequest request,
            Principal principal) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        SubmitInquiryResponse data = submitMissingPointsInquiryUseCase.execute(tenantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(NotificationMessages.MSG_INQUIRY_SUBMITTED, data));
    }

    private UUID resolveTenantId(UUID headerTenantId) {
        if (headerTenantId != null) {
            return headerTenantId;
        }
        UUID contextTenantId = TenantContextHolder.getTenantId();
        if (contextTenantId != null) {
            return contextTenantId;
        }
        throw new BusinessException(NotificationErrorCode.TENANT_HEADER_MISSING);
    }
}

