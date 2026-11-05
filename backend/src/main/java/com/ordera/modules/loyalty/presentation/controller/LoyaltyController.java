package com.ordera.modules.loyalty.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.modules.loyalty.application.service.LoyaltyApplicationService;
import com.ordera.modules.loyalty.presentation.dto.TierUpgradeProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Member tier status, progression, and rewards")
public class LoyaltyController {

    private final LoyaltyApplicationService loyaltyApplicationService;

    @GetMapping("/members/{memberId}/progress")
    @Operation(summary = "Get tier progression details for a member")
    public ResponseEntity<ApiResponse<TierUpgradeProgressResponse>> getTierProgress(@PathVariable UUID memberId) {
        UUID tenantId = TenantContext.getTenantId();
        TierUpgradeProgressResponse response = loyaltyApplicationService.execute(tenantId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

