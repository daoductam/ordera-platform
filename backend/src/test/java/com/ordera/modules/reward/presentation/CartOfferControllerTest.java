package com.ordera.modules.reward.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.controller.CartOfferController;
import com.ordera.modules.reward.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartOfferControllerTest {

    @Mock
    private GetAvailableOffersForCartUseCase getAvailableOffersForCartUseCase;
    @Mock
    private ValidatePromoCodeUseCase validatePromoCodeUseCase;
    @Mock
    private ApplyVoucherToCartUseCase applyVoucherToCartUseCase;
    @Mock
    private RemoveVoucherFromCartUseCase removeVoucherFromCartUseCase;
    @Mock
    private ApplyGiftToCartUseCase applyGiftToCartUseCase;
    @Mock
    private RemoveGiftFromCartUseCase removeGiftFromCartUseCase;

    private CartOfferController controller;

    private final UUID cartId = UUID.randomUUID();
    private final UUID giftId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new CartOfferController(
                getAvailableOffersForCartUseCase,
                validatePromoCodeUseCase,
                applyVoucherToCartUseCase,
                removeVoucherFromCartUseCase,
                applyGiftToCartUseCase,
                removeGiftFromCartUseCase
        );
    }

    @Test
    @DisplayName("5.6: getAvailableOffers should return 200 OK with MSG_ELIGIBLE_OFFERS_RETRIEVED")
    void should_getAvailableOffers_return_200() {
        AvailableOffersResponse mockData = AvailableOffersResponse.builder()
                .subtotalAmount(BigDecimal.valueOf(250))
                .vouchers(List.of())
                .gifts(List.of())
                .build();
        when(getAvailableOffersForCartUseCase.execute(cartId)).thenReturn(mockData);

        ResponseEntity<ApiResponse<AvailableOffersResponse>> response = controller.getAvailableOffers(cartId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_ELIGIBLE_OFFERS_RETRIEVED);
        verify(getAvailableOffersForCartUseCase).execute(cartId);
    }

    @Test
    @DisplayName("5.7: validatePromoCode should return 200 OK with MSG_PROMO_CODE_VALIDATED")
    void should_validatePromoCode_return_200() {
        ValidatePromoCodeRequest request = new ValidatePromoCodeRequest("DISCOUNT30", cartId);
        ValidatePromoCodeResponse mockData = ValidatePromoCodeResponse.builder()
                .code("DISCOUNT30")
                .discountAmount(BigDecimal.valueOf(75))
                .build();
        when(validatePromoCodeUseCase.execute(any(ValidatePromoCodeRequest.class))).thenReturn(mockData);

        ResponseEntity<ApiResponse<ValidatePromoCodeResponse>> response = controller.validatePromoCode(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_PROMO_CODE_VALIDATED);
        verify(validatePromoCodeUseCase).execute(request);
    }

    @Test
    @DisplayName("5.8: applyVoucher should return 200 OK with MSG_VOUCHER_APPLIED")
    void should_applyVoucher_return_200() {
        ApplyVoucherRequest request = new ApplyVoucherRequest(cartId, "DISCOUNT30");
        CartVoucherCalculationResponse mockData = CartVoucherCalculationResponse.builder()
                .cartId(cartId)
                .discountAmount(BigDecimal.valueOf(75))
                .finalTotalAmount(BigDecimal.valueOf(175))
                .build();
        when(applyVoucherToCartUseCase.execute(any(ApplyVoucherRequest.class))).thenReturn(mockData);

        ResponseEntity<ApiResponse<CartVoucherCalculationResponse>> response = controller.applyVoucher(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_VOUCHER_APPLIED);
        verify(applyVoucherToCartUseCase).execute(request);
    }

    @Test
    @DisplayName("5.9: removeVoucher should return 200 OK with MSG_VOUCHER_REMOVED")
    void should_removeVoucher_return_200() {
        RemoveVoucherRequest request = new RemoveVoucherRequest(cartId, "DISCOUNT30");
        CartVoucherCalculationResponse mockData = CartVoucherCalculationResponse.builder()
                .cartId(cartId)
                .discountAmount(BigDecimal.ZERO)
                .finalTotalAmount(BigDecimal.valueOf(250))
                .build();
        when(removeVoucherFromCartUseCase.execute(any(RemoveVoucherRequest.class))).thenReturn(mockData);

        ResponseEntity<ApiResponse<CartVoucherCalculationResponse>> response = controller.removeVoucher(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_VOUCHER_REMOVED);
        verify(removeVoucherFromCartUseCase).execute(request);
    }

    @Test
    @DisplayName("5.10: applyGift should return 200 OK with MSG_GIFT_APPLIED")
    void should_applyGift_return_200() {
        ApplyGiftRequest request = new ApplyGiftRequest(cartId, giftId);
        CartGiftCalculationResponse mockData = CartGiftCalculationResponse.builder()
                .cartId(cartId)
                .selectedGift(CartGiftCalculationResponse.SelectedGiftSummary.builder()
                        .giftId(giftId)
                        .title("Tumbler Pink")
                        .status(RewardConstants.STATUS_SELECTED)
                        .build())
                .build();
        when(applyGiftToCartUseCase.execute(any(ApplyGiftRequest.class))).thenReturn(mockData);

        ResponseEntity<ApiResponse<CartGiftCalculationResponse>> response = controller.applyGift(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_GIFT_APPLIED);
        verify(applyGiftToCartUseCase).execute(request);
    }

    @Test
    @DisplayName("5.11: removeGift should return 200 OK with MSG_GIFT_REMOVED")
    void should_removeGift_return_200() {
        RemoveGiftRequest request = new RemoveGiftRequest(cartId, giftId);
        CartGiftCalculationResponse mockData = CartGiftCalculationResponse.builder()
                .cartId(cartId)
                .selectedGift(null)
                .build();
        when(removeGiftFromCartUseCase.execute(any(RemoveGiftRequest.class))).thenReturn(mockData);

        ResponseEntity<ApiResponse<CartGiftCalculationResponse>> response = controller.removeGift(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_GIFT_REMOVED);
        verify(removeGiftFromCartUseCase).execute(request);
    }
}
