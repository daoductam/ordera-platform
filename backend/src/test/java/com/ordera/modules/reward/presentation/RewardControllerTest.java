package com.ordera.modules.reward.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.reward.application.usecase.*;
import com.ordera.modules.reward.presentation.constant.RewardMessages;
import com.ordera.modules.reward.presentation.controller.RewardController;
import com.ordera.modules.reward.presentation.dto.MyRewardsWalletResponse;
import com.ordera.modules.reward.presentation.dto.RedeemGiftResponse;
import com.ordera.modules.reward.presentation.dto.RedeemVoucherResponse;
import com.ordera.modules.reward.presentation.dto.RewardsCatalogResponse;
import com.ordera.modules.reward.presentation.dto.RewardsSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

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

    private RewardController controller;

    @BeforeEach
    void setUp() {
        controller = new RewardController(
                getMyRewardsWalletUseCase,
                getRewardsCatalogUseCase,
                redeemVoucherUseCase,
                redeemPhysicalGiftUseCase,
                getRewardsSummaryUseCase
        );
    }

    @Test
    @DisplayName("5.1: getMyRewardsWallet should return 200 OK with MSG_USER_VOUCHERS_RETRIEVED")
    void should_getMyRewardsWallet_return_200() {
        MyRewardsWalletResponse mockData = MyRewardsWalletResponse.builder()
                .totalCount(0)
                .vouchers(List.of())
                .gifts(List.of())
                .build();
        when(getMyRewardsWalletUseCase.execute(anyString(), anyString())).thenReturn(mockData);

        ResponseEntity<ApiResponse<MyRewardsWalletResponse>> response = controller.getMyRewardsWallet("AVAILABLE", "ALL");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_USER_VOUCHERS_RETRIEVED);
        verify(getMyRewardsWalletUseCase).execute("AVAILABLE", "ALL");
    }

    @Test
    @DisplayName("5.2: getRewardsCatalog should return 200 OK with MSG_REWARD_CATALOG_RETRIEVED")
    void should_getRewardsCatalog_return_200() {
        RewardsCatalogResponse mockData = RewardsCatalogResponse.builder()
                .memberAvailablePoints(500)
                .vouchers(List.of())
                .gifts(List.of())
                .build();
        when(getRewardsCatalogUseCase.execute()).thenReturn(mockData);

        ResponseEntity<ApiResponse<RewardsCatalogResponse>> response = controller.getRewardsCatalog();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_REWARD_CATALOG_RETRIEVED);
        verify(getRewardsCatalogUseCase).execute();
    }

    @Test
    @DisplayName("5.3: redeemVoucher should return 201 CREATED with MSG_VOUCHER_REDEEMED")
    void should_redeemVoucher_return_201() {
        UUID voucherId = UUID.randomUUID();
        RedeemVoucherResponse mockData = RedeemVoucherResponse.builder()
                .code("SAVE100")
                .pointsSpent(100)
                .build();
        when(redeemVoucherUseCase.execute(voucherId)).thenReturn(mockData);

        ResponseEntity<ApiResponse<RedeemVoucherResponse>> response = controller.redeemVoucher(voucherId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_VOUCHER_REDEEMED);
        verify(redeemVoucherUseCase).execute(voucherId);
    }

    @Test
    @DisplayName("5.4: redeemPhysicalGift should return 201 CREATED with MSG_GIFT_REDEEMED")
    void should_redeemPhysicalGift_return_201() {
        UUID giftId = UUID.randomUUID();
        RedeemGiftResponse mockData = RedeemGiftResponse.builder()
                .giftTitle("Tumbler Pink")
                .pointsSpent(1000)
                .build();
        when(redeemPhysicalGiftUseCase.execute(giftId)).thenReturn(mockData);

        ResponseEntity<ApiResponse<RedeemGiftResponse>> response = controller.redeemPhysicalGift(giftId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_GIFT_REDEEMED);
        verify(redeemPhysicalGiftUseCase).execute(giftId);
    }

    @Test
    @DisplayName("5.5: getRewardsSummary should return 200 OK with MSG_OFFERS_SUMMARY_RETRIEVED")
    void should_getRewardsSummary_return_200() {
        RewardsSummaryResponse mockData = RewardsSummaryResponse.builder()
                .totalAvailableRewards(3)
                .activeVouchersCount(2)
                .availableGiftsCount(1)
                .build();
        when(getRewardsSummaryUseCase.execute()).thenReturn(mockData);

        ResponseEntity<ApiResponse<RewardsSummaryResponse>> response = controller.getRewardsSummary();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(RewardMessages.MSG_OFFERS_SUMMARY_RETRIEVED);
        verify(getRewardsSummaryUseCase).execute();
    }
}

