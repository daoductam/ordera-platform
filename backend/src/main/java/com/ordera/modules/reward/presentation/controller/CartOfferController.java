package com.ordera.modules.reward.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Offers", description = "Evaluate, validate, apply and remove vouchers and loyalty gifts from cart")
public class CartOfferController {

    private final GetAvailableOffersForCartUseCase getAvailableOffersForCartUseCase;
    private final ValidatePromoCodeUseCase validatePromoCodeUseCase;
    private final ApplyVoucherToCartUseCase applyVoucherToCartUseCase;
    private final RemoveVoucherFromCartUseCase removeVoucherFromCartUseCase;
    private final ApplyGiftToCartUseCase applyGiftToCartUseCase;
    private final RemoveGiftFromCartUseCase removeGiftFromCartUseCase;

    @GetMapping("/available-offers")
    @Operation(summary = "5.6: Get Available Offers for Cart")
    public ResponseEntity<ApiResponse<AvailableOffersResponse>> getAvailableOffers(@RequestParam UUID cartId) {
        AvailableOffersResponse response = getAvailableOffersForCartUseCase.execute(cartId);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_ELIGIBLE_OFFERS_RETRIEVED, response));
    }

    @PostMapping("/promo/validate")
    @Operation(summary = "5.7: Validate Manual Promo Code")
    public ResponseEntity<ApiResponse<ValidatePromoCodeResponse>> validatePromoCode(@Valid @RequestBody ValidatePromoCodeRequest request) {
        ValidatePromoCodeResponse response = validatePromoCodeUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_PROMO_CODE_VALIDATED, response));
    }

    @PostMapping("/voucher/apply")
    @Operation(summary = "5.8: Apply Voucher to Cart")
    public ResponseEntity<ApiResponse<CartVoucherCalculationResponse>> applyVoucher(@Valid @RequestBody ApplyVoucherRequest request) {
        CartVoucherCalculationResponse response = applyVoucherToCartUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_VOUCHER_APPLIED, response));
    }

    @DeleteMapping("/voucher/remove")
    @Operation(summary = "5.9: Remove Voucher from Cart")
    public ResponseEntity<ApiResponse<CartVoucherCalculationResponse>> removeVoucher(@Valid @RequestBody RemoveVoucherRequest request) {
        CartVoucherCalculationResponse response = removeVoucherFromCartUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_VOUCHER_REMOVED, response));
    }

    @PostMapping("/gift/apply")
    @Operation(summary = "5.10: Apply Physical Gift to Cart")
    public ResponseEntity<ApiResponse<CartGiftCalculationResponse>> applyGift(@Valid @RequestBody ApplyGiftRequest request) {
        CartGiftCalculationResponse response = applyGiftToCartUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_GIFT_APPLIED, response));
    }

    @DeleteMapping("/gift/remove")
    @Operation(summary = "5.11: Remove Physical Gift from Cart")
    public ResponseEntity<ApiResponse<CartGiftCalculationResponse>> removeGift(@Valid @RequestBody RemoveGiftRequest request) {
        CartGiftCalculationResponse response = removeGiftFromCartUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok(RewardMessages.MSG_GIFT_REMOVED, response));
    }
}

