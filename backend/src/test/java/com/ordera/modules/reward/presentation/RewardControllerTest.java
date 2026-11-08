package com.ordera.modules.reward.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.controller.RewardController;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RewardController MockMvc HTTP API Tests")
class RewardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetMyRewardsWalletUseCase getMyRewardsWalletUseCase;
    @Mock
    private GetRewardsCatalogUseCase getRewardsCatalogUseCase;
    @Mock
    private RedeemVoucherUseCase redeemVoucherUseCase;
    @Mock
    private RedeemPhysicalGiftUseCase redeemPhysicalGiftUseCase;
    @Mock
    private GetRewardsSummaryUseCase getRewardsSummaryUseCase;

    @InjectMocks
    private RewardController controller;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private final UUID voucherId = UUID.fromString("b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01");
    private final UUID giftId = UUID.fromString("c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("API 5.1: GET /api/v1/rewards/my-wallet should return 200 OK with wallet data")
    void should_getMyRewardsWallet_return_200() throws Exception {
        MyRewardsWalletResponse mockData = MyRewardsWalletResponse.builder()
                .totalCount(2)
                .vouchers(List.of(UserVoucherItemResponse.builder().code("SAVE100").build()))
                .gifts(List.of(UserGiftItemResponse.builder().title("Tumbler Pink").build()))
                .build();
        when(getMyRewardsWalletUseCase.execute("AVAILABLE", "ALL")).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/rewards/my-wallet")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_USER_VOUCHERS_RETRIEVED))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.vouchers[0].code").value("SAVE100"))
                .andExpect(jsonPath("$.data.gifts[0].title").value("Tumbler Pink"));

        verify(getMyRewardsWalletUseCase).execute("AVAILABLE", "ALL");
    }

    @Test
    @DisplayName("API 5.1: GET /api/v1/rewards/my-wallet with query filters")
    void should_getMyRewardsWallet_with_filters() throws Exception {
        MyRewardsWalletResponse mockData = MyRewardsWalletResponse.builder()
                .totalCount(1)
                .vouchers(List.of(UserVoucherItemResponse.builder().code("OFF30").build()))
                .gifts(List.of())
                .build();
        when(getMyRewardsWalletUseCase.execute("AVAILABLE", "VOUCHER")).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/rewards/my-wallet")
                        .header("X-Tenant-ID", tenantId)
                        .param("status", "AVAILABLE")
                        .param("type", "VOUCHER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }

    @Test
    @DisplayName("API 5.1: GET /api/v1/rewards/my-wallet returns 401 when unauthenticated")
    void should_getMyRewardsWallet_return_401_when_unauthorized() throws Exception {
        when(getMyRewardsWalletUseCase.execute(any(), any()))
                .thenThrow(new BusinessException(RewardErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/v1/rewards/my-wallet")
                        .header("X-Tenant-ID", tenantId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("API 5.2: GET /api/v1/rewards/catalog should return 200 OK with catalog items")
    void should_getRewardsCatalog_return_200() throws Exception {
        RewardsCatalogResponse mockData = RewardsCatalogResponse.builder()
                .memberAvailablePoints(500)
                .vouchers(List.of(CatalogVoucherItemResponse.builder().code("SAVE100").pointsCost(100).build()))
                .gifts(List.of(CatalogGiftItemResponse.builder().title("Tumbler Pink").pointsCost(1000).build()))
                .build();
        when(getRewardsCatalogUseCase.execute()).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/rewards/catalog")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_REWARD_CATALOG_RETRIEVED))
                .andExpect(jsonPath("$.data.memberAvailablePoints").value(500))
                .andExpect(jsonPath("$.data.vouchers[0].code").value("SAVE100"))
                .andExpect(jsonPath("$.data.gifts[0].title").value("Tumbler Pink"));
    }

    @Test
    @DisplayName("API 5.3: POST /api/v1/rewards/vouchers/{voucherId}/redeem should return 201 CREATED")
    void should_redeemVoucher_return_201() throws Exception {
        RedeemVoucherResponse mockData = RedeemVoucherResponse.builder()
                .code("SAVE100")
                .title("Voucher 100 NOK")
                .pointsSpent(100)
                .remainingPoints(400)
                .status("AVAILABLE")
                .build();
        when(redeemVoucherUseCase.execute(voucherId)).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/rewards/vouchers/{voucherId}/redeem", voucherId)
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_VOUCHER_REDEEMED))
                .andExpect(jsonPath("$.data.code").value("SAVE100"))
                .andExpect(jsonPath("$.data.pointsSpent").value(100))
                .andExpect(jsonPath("$.data.remainingPoints").value(400));
    }

    @Test
    @DisplayName("API 5.3: POST /api/v1/rewards/vouchers/{voucherId}/redeem returns 422 when insufficient points")
    void should_redeemVoucher_return_422_when_insufficient_points() throws Exception {
        when(redeemVoucherUseCase.execute(voucherId))
                .thenThrow(new BusinessException(RewardErrorCode.INSUFFICIENT_POINTS));

        mockMvc.perform(post("/api/v1/rewards/vouchers/{voucherId}/redeem", voucherId)
                        .header("X-Tenant-ID", tenantId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("POINTS_INSUFFICIENT"));
    }

    @Test
    @DisplayName("API 5.3: POST /api/v1/rewards/vouchers/{voucherId}/redeem returns 409 when concurrent transaction locked")
    void should_redeemVoucher_return_409_when_locked() throws Exception {
        when(redeemVoucherUseCase.execute(voucherId))
                .thenThrow(new BusinessException(RewardErrorCode.CONCURRENT_TRANSACTION_LOCK));

        mockMvc.perform(post("/api/v1/rewards/vouchers/{voucherId}/redeem", voucherId)
                        .header("X-Tenant-ID", tenantId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CONCURRENT_TRANSACTION_LOCK"));
    }

    @Test
    @DisplayName("API 5.4: POST /api/v1/rewards/gifts/{giftId}/redeem should return 201 CREATED")
    void should_redeemPhysicalGift_return_201() throws Exception {
        RedeemGiftResponse mockData = RedeemGiftResponse.builder()
                .giftTitle("Tumbler Pink")
                .pointsSpent(1000)
                .remainingPoints(200)
                .redemptionCode("GIFT8942")
                .status("PENDING")
                .build();
        when(redeemPhysicalGiftUseCase.execute(giftId)).thenReturn(mockData);

        mockMvc.perform(post("/api/v1/rewards/gifts/{giftId}/redeem", giftId)
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_GIFT_REDEEMED))
                .andExpect(jsonPath("$.data.giftTitle").value("Tumbler Pink"))
                .andExpect(jsonPath("$.data.redemptionCode").value("GIFT8942"));
    }

    @Test
    @DisplayName("API 5.4: POST /api/v1/rewards/gifts/{giftId}/redeem returns 409 when gift out of stock")
    void should_redeemPhysicalGift_return_409_when_out_of_stock() throws Exception {
        when(redeemPhysicalGiftUseCase.execute(giftId))
                .thenThrow(new BusinessException(RewardErrorCode.GIFT_OUT_OF_STOCK));

        mockMvc.perform(post("/api/v1/rewards/gifts/{giftId}/redeem", giftId)
                        .header("X-Tenant-ID", tenantId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("GIFT_OUT_OF_STOCK"));
    }

    @Test
    @DisplayName("API 5.5: GET /api/v1/rewards/summary should return 200 OK with counters")
    void should_getRewardsSummary_return_200() throws Exception {
        RewardsSummaryResponse mockData = RewardsSummaryResponse.builder()
                .totalAvailableRewards(3)
                .activeVouchersCount(2)
                .availableGiftsCount(1)
                .build();
        when(getRewardsSummaryUseCase.execute()).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/rewards/summary")
                        .header("X-Tenant-ID", tenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RewardMessages.MSG_OFFERS_SUMMARY_RETRIEVED))
                .andExpect(jsonPath("$.data.totalAvailableRewards").value(3))
                .andExpect(jsonPath("$.data.activeVouchersCount").value(2))
                .andExpect(jsonPath("$.data.availableGiftsCount").value(1));
    }
}
