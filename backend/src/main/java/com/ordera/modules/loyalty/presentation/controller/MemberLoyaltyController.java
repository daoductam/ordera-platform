package com.ordera.modules.loyalty.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.api.PageResponse;
import com.ordera.modules.ledger.application.usecase.GetPointsLedgerHistoryUseCase;
import com.ordera.modules.ledger.presentation.dto.PointsLedgerEntryResponse;
import com.ordera.modules.loyalty.application.usecase.*;
import com.ordera.modules.loyalty.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty & Member", description = "Member profiles, tier benefits, avatar upload and points ledger history")
public class MemberLoyaltyController {

    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final CompleteProfileUseCase completeProfileUseCase;
    private final UploadAvatarUseCase uploadAvatarUseCase;
    private final GetTierDefinitionsUseCase getTierDefinitionsUseCase;
    private final GetPointsLedgerHistoryUseCase getPointsLedgerHistoryUseCase;

    @GetMapping("/members/me")
    @Operation(summary = "Get current member profile and dashboard")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyMemberProfile() {
        MemberProfileResponse response = getMemberProfileUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok("Member profile retrieved successfully", response));
    }

    @PutMapping("/members/me")
    @Operation(summary = "Update member profile personal information")
    public ResponseEntity<ApiResponse<UpdateProfileResponse>> updateMemberProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UpdateProfileResponse response = updateProfileUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin cá nhân thành công", response));
    }

    @PostMapping("/members/complete-profile")
    @Operation(summary = "Complete profile after OTP verification")
    public ResponseEntity<ApiResponse<CompleteProfileResponse>> completeMemberProfile(@Valid @RequestBody CompleteProfileRequest request) {
        CompleteProfileResponse response = completeProfileUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok("Hoàn thiện hồ sơ thành công! Tài khoản hội viên đã sẵn sàng.", response));
    }

    @PostMapping(value = "/members/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload member avatar image")
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> uploadMemberAvatar(@RequestParam("file") MultipartFile file) {
        AvatarUploadResponse response = uploadAvatarUseCase.execute(file);
        return ResponseEntity.ok(ApiResponse.ok("Ảnh đại diện đã được cập nhật thành công.", response));
    }

    @GetMapping("/tiers")
    @Operation(summary = "Get list of all 6 tier definitions and benefits")
    public ResponseEntity<ApiResponse<List<TierDefinitionResponse>>> getTierDefinitions() {
        List<TierDefinitionResponse> response = getTierDefinitionsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok("Tier definitions retrieved successfully", response));
    }

    @GetMapping("/members/me/points")
    @Operation(summary = "Get points ledger history with pagination")
    public ResponseEntity<ApiResponse<PageResponse<PointsLedgerEntryResponse>>> getMyPointsLedgerHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int boundedSize = Math.max(1, Math.min(size, 50));
        PageResponse<PointsLedgerEntryResponse> response = getPointsLedgerHistoryUseCase.execute(PageRequest.of(page, boundedSize));
        return ResponseEntity.ok(ApiResponse.ok("Points ledger entries retrieved successfully", response));
    }
}

