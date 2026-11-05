package com.ordera.modules.tenant.infrastructure.mapper;

import com.ordera.modules.tenant.domain.model.Store;
import com.ordera.modules.tenant.domain.model.StoreTable;
import com.ordera.modules.tenant.domain.model.Tenant;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    Tenant toDomain(TenantJpaEntity entity);
    TenantJpaEntity toEntity(Tenant domain);
    List<Tenant> toTenantDomainList(List<TenantJpaEntity> entities);

    Store toDomain(StoreJpaEntity entity);
    StoreJpaEntity toEntity(Store domain);
    List<Store> toStoreDomainList(List<StoreJpaEntity> entities);

    StoreTable toDomain(StoreTableJpaEntity entity);
    StoreTableJpaEntity toEntity(StoreTable domain);
    List<StoreTable> toStoreTableDomainList(List<StoreTableJpaEntity> entities);
}
