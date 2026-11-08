package com.ordera.modules.loyalty.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.loyalty.application.usecase.SubmitSupportClaimUseCase;
import com.ordera.modules.loyalty.presentation.dto.SubmitSupportClaimRequest;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loyalty/claims")
@RequiredArgsConstructor
@Tag(name = "Support Claims", description = "Missing points claim submissions for store receipts")
public class SupportClaimController {

    private final SubmitSupportClaimUseCase submitSupportClaimUseCase;

    @PostMapping
    @Operation(summary = "Submit a missing points claim for store receipt")
    public ResponseEntity<ApiResponse<SupportClaimResponse>> submitSupportClaim(@Valid @RequestBody SubmitSupportClaimRequest request) {
        SupportClaimResponse response = submitSupportClaimUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Yêu cầu khiếu nại đã được ghi nhận. Quản trị viên sẽ xử lý trong vòng 24 giờ.", response));
    }
}

