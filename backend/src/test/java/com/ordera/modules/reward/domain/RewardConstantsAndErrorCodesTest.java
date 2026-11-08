package com.ordera.modules.reward.domain;

import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.domain.model.LoyaltyGift;
import com.ordera.modules.reward.domain.model.UserGiftRedemption;
import com.ordera.modules.reward.domain.model.UserVoucher;
import com.ordera.modules.reward.domain.model.Voucher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RewardConstantsAndErrorCodesTest {

    @Test
    @DisplayName("should contain all specified reward domain constants without magic numbers")
    void should_containAllSpecifiedRewardDomainConstants() {
        assertThat(RewardConstants.MAX_VOUCHERS_PER_ORDER).isEqualTo(1);
        assertThat(RewardConstants.MAX_PHYSICAL_GIFTS_PER_ORDER).isEqualTo(1);
        assertThat(RewardConstants.GIFT_REDEMPTION_CODE_LENGTH).isEqualTo(8);
        assertThat(RewardConstants.LOCK_ACQUIRE_TIMEOUT_SEC).isEqualTo(3);
        assertThat(RewardConstants.LOCK_LEASE_TIMEOUT_SEC).isEqualTo(5);
        assertThat(RewardConstants.LOCK_KEY_REWARD_PATTERN).isEqualTo("lock:tenant:%s:reward:%s");
        assertThat(RewardConstants.PROMO_CODE_MIN_LENGTH).isEqualTo(3);
        assertThat(RewardConstants.PROMO_CODE_MAX_LENGTH).isEqualTo(20);
    }

    @Test
    @DisplayName("should implement ErrorCode with correct status codes in RewardErrorCode")
    void should_implementErrorCodeWithCorrectStatusCodes() {
        assertThat(RewardErrorCode.INSUFFICIENT_POINTS.getStatus()).isEqualTo(422);
        assertThat(RewardErrorCode.GIFT_OUT_OF_STOCK.getStatus()).isEqualTo(409);
        assertThat(RewardErrorCode.PROMO_CODE_NOT_FOUND.getStatus()).isEqualTo(404);
        assertThat(RewardErrorCode.MINIMUM_ORDER_NOT_MET.getStatus()).isEqualTo(422);
        assertThat(RewardErrorCode.VOUCHER_ALREADY_USED.getStatus()).isEqualTo(409);
        assertThat(RewardErrorCode.VOUCHER_EXPIRED.getStatus()).isEqualTo(422);
        assertThat(RewardErrorCode.INVALID_REWARD_COMBINATION.getStatus()).isEqualTo(422);
    }

    @Test
    @DisplayName("should correctly build domain models with all required attributes")
    void should_correctlyBuildDomainModels() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Voucher voucher = Voucher.builder()
                .id(id)
                .tenantId(tenantId)
                .code("SAVE100")
                .title("Voucher 100 NOK")
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.valueOf(250))
                .pointsCost(100)
                .validUntil(Instant.now().plusSeconds(86400))
                .active(true)
                .build();
        assertThat(voucher.getCode()).isEqualTo("SAVE100");

        UserVoucher userVoucher = UserVoucher.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .memberId(memberId)
                .voucherId(id)
                .status(RewardConstants.STATUS_AVAILABLE)
                .build();
        assertThat(userVoucher.getStatus()).isEqualTo(RewardConstants.STATUS_AVAILABLE);

        LoyaltyGift gift = LoyaltyGift.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .title("Tumbler Pink")
                .tag("Hot Reward")
                .stockQuantity(10)
                .pointsCost(100)
                .active(true)
                .build();
        assertThat(gift.getTag()).isEqualTo("Hot Reward");

        UserGiftRedemption redemption = UserGiftRedemption.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .memberId(memberId)
                .giftId(gift.getId())
                .redemptionCode("GIFT-8942")
                .status(RewardConstants.STATUS_PENDING)
                .build();
        assertThat(redemption.getRedemptionCode()).hasSize(9);
    }
}

