package com.ordera.modules.loyalty.infrastructure.mapper;

import com.ordera.modules.loyalty.domain.model.MemberProfile;
import com.ordera.modules.loyalty.domain.model.SupportClaim;
import com.ordera.modules.loyalty.domain.model.TierDefinition;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoyaltyMapper {

    TierDefinition toDomain(TierDefinitionJpaEntity entity);
    TierDefinitionJpaEntity toEntity(TierDefinition domain);
    List<TierDefinition> toTierDomainList(List<TierDefinitionJpaEntity> entities);

    MemberProfile toDomain(MemberProfileJpaEntity entity);
    MemberProfileJpaEntity toEntity(MemberProfile domain);

    SupportClaim toDomain(SupportClaimJpaEntity entity);
    SupportClaimJpaEntity toEntity(SupportClaim domain);
    List<SupportClaim> toClaimDomainList(List<SupportClaimJpaEntity> entities);
}
