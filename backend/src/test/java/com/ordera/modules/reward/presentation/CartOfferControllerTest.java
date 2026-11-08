package com.ordera.modules.reward.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.controller.CartOfferController;
import com.ordera.modules.reward.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartOfferController MockMvc HTTP API Tests")
class CartOfferControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @InjectMocks
    private CartOfferController controller;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private final UUID cartId = UUID.fromString("c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c99");
    private final UUID giftId = UUID.fromString("d1eebc99-9c0b-4ef8-bb6d-6bb9bd380d01");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("API 5.6: GET /api/v1/cart/available-offers should return 200 OK")
    void should_getAvailableOffers_return_200() throws Exception {
        AvailableOffersResponse mockData = AvailableOffersResponse.builder()
                .subtotalAmount(BigDecimal.valueOf(250))
                .vouchers(List.of(CartOfferVoucherItemResponse.builder()
                        .code("DISCOUNT30")
                        .title("30% Off")
                        .isApplicable(true)
                        .build()))
                .gifts(List.of(CartOfferGiftItemResponse.builder()
                        .giftId(giftId)
                        .title("Tumbler")
                        .isApplicable(true)
                        .build()))
                .build();
        when(getAvailableOffersForCartUseCase.execute(cartId)).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/cart/available-offers")
                        .header("X-Tenant-ID", tenantId)
                        .param("cartId", cartId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_ELIGIBLE_OFFERS_RETRIEVED))
                .andExpect(jsonPath("$.data.subtotalAmount").value(250))
                .andExpect(jsonPath("$.data.vouchers[0].code").value("DISCOUNT30"))
                .andExpect(jsonPath("$.data.gifts[0].giftId").value(giftId.toString()));
    }

    @Test
    @DisplayName("API 5.6: GET /api/v1/cart/available-offers returns 404 when cart not found")
    void should_getAvailableOffers_return_404_when_cart_not_found() throws Exception {
        when(getAvailableOffersForCartUseCase.execute(cartId))
                .thenThrow(new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        mockMvc.perform(get("/api/v1/cart/available-offers")
                        .header("X-Tenant-ID", tenantId)
                        .param("cartId", cartId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CART_NOT_FOUND"));
    }

    @Test
    @DisplayName("API 5.7: POST /api/v1/cart/promo/validate should return 200 OK")
    void should_validatePromoCode_return_200() throws Exception {
        ValidatePromoCodeRequest request = new ValidatePromoCodeRequest("DISCOUNT30", cartId);
        ValidatePromoCodeResponse mockData = ValidatePromoCodeResponse.builder()
                .code("DISCOUNT30")
                .discountAmount(BigDecimal.valueOf(75))
                .minOrderValue(BigDecimal.valueOf(200))
                .build();
        when(validatePromoCodeUseCase.execute(any(ValidatePromoCodeRequest.class))).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/cart/promo/validate")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_PROMO_CODE_VALIDATED))
                .andExpect(jsonPath("$.data.code").value("DISCOUNT30"))
                .andExpect(jsonPath("$.data.discountAmount").value(75));
    }

    @Test
    @DisplayName("API 5.7: POST /api/v1/cart/promo/validate returns 400 when validation fails")
    void should_validatePromoCode_return_400_when_invalid() throws Exception {
        // Missing cartId and code too short (< 3)
        ValidatePromoCodeRequest invalidRequest = new ValidatePromoCodeRequest("AB", null);

        mockMvc.perform(post("/api/v1/cart/promo/validate")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("API 5.8: POST /api/v1/cart/voucher/apply should return 200 OK")
    void should_applyVoucher_return_200() throws Exception {
        ApplyVoucherRequest request = new ApplyVoucherRequest(cartId, "DISCOUNT30");
        CartVoucherCalculationResponse mockData = CartVoucherCalculationResponse.builder()
                .cartId(cartId)
                .appliedVoucher(CartVoucherCalculationResponse.AppliedVoucherSummary.builder()
                        .code("DISCOUNT30")
                        .discountAmount(BigDecimal.valueOf(75))
                        .build())
                .subtotalAmount(BigDecimal.valueOf(250))
                .discountAmount(BigDecimal.valueOf(75))
                .vatAmount(new BigDecimal("22.83"))
                .finalTotalAmount(BigDecimal.valueOf(175))
                .build();
        when(applyVoucherToCartUseCase.execute(any(ApplyVoucherRequest.class))).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/cart/voucher/apply")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_VOUCHER_APPLIED))
                .andExpect(jsonPath("$.data.appliedVoucher.code").value("DISCOUNT30"))
                .andExpect(jsonPath("$.data.vatAmount").value(22.83))
                .andExpect(jsonPath("$.data.finalTotalAmount").value(175));
    }

    @Test
    @DisplayName("API 5.8: POST /api/v1/cart/voucher/apply returns 422 when multiple vouchers attempted")
    void should_applyVoucher_return_422_when_combination_invalid() throws Exception {
        ApplyVoucherRequest request = new ApplyVoucherRequest(cartId, "SECOND_CODE");
        when(applyVoucherToCartUseCase.execute(any(ApplyVoucherRequest.class)))
                .thenThrow(new BusinessException(RewardErrorCode.INVALID_REWARD_COMBINATION));

        mockMvc.perform(post("/api/v1/cart/voucher/apply")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REWARD_COMBINATION"));
    }

    @Test
    @DisplayName("API 5.9: DELETE /api/v1/cart/voucher/remove should return 200 OK")
    void should_removeVoucher_return_200() throws Exception {
        RemoveVoucherRequest request = new RemoveVoucherRequest(cartId, "DISCOUNT30");
        CartVoucherCalculationResponse mockData = CartVoucherCalculationResponse.builder()
                .cartId(cartId)
                .appliedVoucher(null)
                .subtotalAmount(BigDecimal.valueOf(250))
                .discountAmount(BigDecimal.ZERO)
                .finalTotalAmount(BigDecimal.valueOf(250))
                .build();
        when(removeVoucherFromCartUseCase.execute(any(RemoveVoucherRequest.class))).thenReturn(mockData);

        mockMvc.perform(delete("/api/v1/cart/voucher/remove")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_VOUCHER_REMOVED))
                .andExpect(jsonPath("$.data.appliedVoucher").doesNotExist());
    }

    @Test
    @DisplayName("API 5.10: POST /api/v1/cart/gift/apply should return 200 OK")
    void should_applyGift_return_200() throws Exception {
        ApplyGiftRequest request = new ApplyGiftRequest(cartId, giftId);
        CartGiftCalculationResponse mockData = CartGiftCalculationResponse.builder()
                .cartId(cartId)
                .selectedGift(CartGiftCalculationResponse.SelectedGiftSummary.builder()
                        .giftId(giftId)
                        .title("Tumbler Pink")
                        .status("SELECTED")
                        .build())
                .build();
        when(applyGiftToCartUseCase.execute(any(ApplyGiftRequest.class))).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/cart/gift/apply")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_GIFT_APPLIED))
                .andExpect(jsonPath("$.data.selectedGift.giftId").value(giftId.toString()))
                .andExpect(jsonPath("$.data.selectedGift.status").value("SELECTED"));
    }

    @Test
    @DisplayName("API 5.11: DELETE /api/v1/cart/gift/remove should return 200 OK")
    void should_removeGift_return_200() throws Exception {
        RemoveGiftRequest request = new RemoveGiftRequest(cartId, giftId);
        CartGiftCalculationResponse mockData = CartGiftCalculationResponse.builder()
                .cartId(cartId)
                .selectedGift(null)
                .build();
        when(removeGiftFromCartUseCase.execute(any(RemoveGiftRequest.class))).thenReturn(mockData);

        mockMvc.perform(delete("/api/v1/cart/gift/remove")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_GIFT_REMOVED))
                .andExpect(jsonPath("$.data.selectedGift").doesNotExist());
    }
}
