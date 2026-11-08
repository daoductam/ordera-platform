package com.ordera.modules.reward.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.dto.MyRewardsWalletResponse;
import com.ordera.modules.reward.presentation.dto.RedeemGiftResponse;
import com.ordera.modules.reward.presentation.dto.RedeemVoucherResponse;
import com.ordera.modules.reward.presentation.dto.RewardsCatalogResponse;
import com.ordera.modules.reward.presentation.dto.RewardsSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Tag(name = "Reward & Voucher", description = "Member rewards wallet, points redemption catalog, voucher & gift management")
public class RewardController {

    private final GetMyRewardsWalletUseCase getMyRewardsWalletUseCase;
    private final GetRewardsCatalogUseCase getRewardsCatalogUseCase;
    private final RedeemVoucherUseCase redeemVoucherUseCase;
    private final RedeemPhysicalGiftUseCase redeemPhysicalGiftUseCase;
    private final GetRewardsSummaryUseCase getRewardsSummaryUseCase;

    @GetMapping("/my-wallet")
    @Operation(summary = "5.1: Get My Rewards Wallet (Vouchers & Physical Gifts)")
    public ResponseEntity<ApiResponse<MyRewardsWalletResponse>> getMyRewardsWallet(
            @RequestParam(required = false, defaultValue = "AVAILABLE") String status,
            @RequestParam(required = false, defaultValue = "ALL") String type) {
        MyRewardsWalletResponse response = getMyRewardsWalletUseCase.execute(status, type);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_USER_VOUCHERS_RETRIEVED, response));
    }

    @GetMapping("/catalog")
    @Operation(summary = "5.2: Get Rewards Catalog for Points Redemption")
    public ResponseEntity<ApiResponse<RewardsCatalogResponse>> getRewardsCatalog() {
        RewardsCatalogResponse response = getRewardsCatalogUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_REWARD_CATALOG_RETRIEVED, response));
    }

    @PostMapping("/vouchers/{voucherId}/redeem")
    @Operation(summary = "5.3: Redeem Points for Voucher Reward")
    public ResponseEntity<ApiResponse<RedeemVoucherResponse>> redeemVoucher(@PathVariable UUID voucherId) {
        RedeemVoucherResponse response = redeemVoucherUseCase.execute(voucherId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(RewardMessages.MSG_VOUCHER_REDEEMED, response));
    }

    @PostMapping("/gifts/{giftId}/redeem")
    @Operation(summary = "5.4: Redeem Points for Physical Gift (Tumbler)")
    public ResponseEntity<ApiResponse<RedeemGiftResponse>> redeemPhysicalGift(@PathVariable UUID giftId) {
        RedeemGiftResponse response = redeemPhysicalGiftUseCase.execute(giftId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(RewardMessages.MSG_GIFT_REDEEMED, response));
    }

    @GetMapping("/summary")
    @Operation(summary = "5.5: Get Rewards Summary Counters for Red Badge")
    public ResponseEntity<ApiResponse<RewardsSummaryResponse>> getRewardsSummary() {
        RewardsSummaryResponse response = getRewardsSummaryUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_OFFERS_SUMMARY_RETRIEVED, response));
    }
}

