package com.ordera.modules.loyalty.infrastructure.mapper;

import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;
import com.ordera.modules.loyalty.presentation.dto.TierDefinitionResponse;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoyaltyDtoMapper {

    TierDefinitionResponse toTierResponse(TierDefinitionJpaEntity entity);

    List<TierDefinitionResponse> toTierResponseList(List<TierDefinitionJpaEntity> entities);

    @Mapping(target = "claimId", source = "id")
    SupportClaimResponse toClaimResponse(SupportClaimJpaEntity entity);

    UpdateProfileResponse toUpdateResponse(MemberProfileJpaEntity entity);
}

