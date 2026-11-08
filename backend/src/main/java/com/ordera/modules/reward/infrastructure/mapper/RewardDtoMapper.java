package com.ordera.modules.reward.infrastructure.mapper;

import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.presentation.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RewardDtoMapper {

    @Mapping(target = "userVoucherId", source = "userVoucher.id")
    @Mapping(target = "voucherId", source = "voucher.id")
    @Mapping(target = "code", source = "voucher.code")
    @Mapping(target = "title", source = "voucher.title")
    @Mapping(target = "description", source = "voucher.description")
    @Mapping(target = "discountType", source = "voucher.discountType")
    @Mapping(target = "discountValue", source = "voucher.discountValue")
    @Mapping(target = "discountValueText", source = "voucher", qualifiedByName = "formatDiscountText")
    @Mapping(target = "minOrderValue", source = "voucher.minOrderValue")
    @Mapping(target = "validFrom", source = "voucher.validFrom")
    @Mapping(target = "validUntil", source = "voucher.validUntil")
    @Mapping(target = "status", source = "userVoucher.status")
    UserVoucherItemResponse toUserVoucherItemResponse(UserVoucherJpaEntity userVoucher, VoucherJpaEntity voucher);

    @Mapping(target = "userGiftId", source = "redemption.id")
    @Mapping(target = "giftId", source = "gift.id")
    @Mapping(target = "title", source = "gift.title")
    @Mapping(target = "description", source = "gift.description")
    @Mapping(target = "imageUrl", source = "gift.imageUrl")
    @Mapping(target = "redemptionCode", source = "redemption.redemptionCode")
    @Mapping(target = "status", source = "redemption.status")
    @Mapping(target = "validUntil", expression = "java(redemption.getCreatedAt().plus(java.time.Duration.ofDays(30)))")
    UserGiftItemResponse toUserGiftItemResponse(UserGiftRedemptionJpaEntity redemption, LoyaltyGiftJpaEntity gift);

    @Mapping(target = "canAfford", ignore = true)
    @Mapping(target = "pointsNeeded", ignore = true)
    CatalogVoucherItemResponse toCatalogVoucherItem(VoucherJpaEntity voucher);

    @Mapping(target = "canAfford", ignore = true)
    @Mapping(target = "pointsNeeded", ignore = true)
    CatalogGiftItemResponse toCatalogGiftItem(LoyaltyGiftJpaEntity gift);

    @Mapping(target = "discountValueText", source = "voucher", qualifiedByName = "formatDiscountText")
    @Mapping(target = "isApplicable", ignore = true)
    @Mapping(target = "ineligibilityReason", ignore = true)
    @Mapping(target = "status", constant = RewardConstants.STATUS_AVAILABLE)
    CartOfferVoucherItemResponse toCartOfferVoucherItem(VoucherJpaEntity voucher);

    @Mapping(target = "giftId", source = "id")
    @Mapping(target = "isApplicable", expression = "java(gift.getStockQuantity() > 0)")
    @Mapping(target = "status", constant = RewardConstants.STATUS_AVAILABLE)
    CartOfferGiftItemResponse toCartOfferGiftItem(LoyaltyGiftJpaEntity gift);

    @Named("formatDiscountText")
    default String formatDiscountText(VoucherJpaEntity voucher) {
        if (voucher == null || voucher.getDiscountType() == null || voucher.getDiscountValue() == null) {
            return "";
        }
        if (RewardConstants.DISCOUNT_TYPE_PERCENT.equalsIgnoreCase(voucher.getDiscountType())) {
            return "-" + voucher.getDiscountValue().stripTrailingZeros().toPlainString() + " " + RewardConstants.PERCENT_SYMBOL;
        }
        return voucher.getDiscountValue().stripTrailingZeros().toPlainString() + " " + RewardConstants.CURRENCY_NOK;
    }
}

