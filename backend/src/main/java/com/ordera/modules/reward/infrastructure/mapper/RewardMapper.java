package com.ordera.modules.reward.infrastructure.mapper;

import com.ordera.modules.reward.domain.model.LoyaltyGift;
import com.ordera.modules.reward.domain.model.UserGiftRedemption;
import com.ordera.modules.reward.domain.model.UserVoucher;
import com.ordera.modules.reward.domain.model.Voucher;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RewardMapper {

    Voucher toDomain(VoucherJpaEntity entity);
    VoucherJpaEntity toEntity(Voucher domain);
    List<Voucher> toVoucherDomainList(List<VoucherJpaEntity> entities);

    UserVoucher toDomain(UserVoucherJpaEntity entity);
    UserVoucherJpaEntity toEntity(UserVoucher domain);
    List<UserVoucher> toUserVoucherDomainList(List<UserVoucherJpaEntity> entities);

    LoyaltyGift toDomain(LoyaltyGiftJpaEntity entity);
    LoyaltyGiftJpaEntity toEntity(LoyaltyGift domain);
    List<LoyaltyGift> toGiftDomainList(List<LoyaltyGiftJpaEntity> entities);

    UserGiftRedemption toDomain(UserGiftRedemptionJpaEntity entity);
    UserGiftRedemptionJpaEntity toEntity(UserGiftRedemption domain);
    List<UserGiftRedemption> toRedemptionDomainList(List<UserGiftRedemptionJpaEntity> entities);
}
