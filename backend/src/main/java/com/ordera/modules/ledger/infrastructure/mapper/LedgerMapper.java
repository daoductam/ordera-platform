package com.ordera.modules.ledger.infrastructure.mapper;

import com.ordera.modules.ledger.domain.model.PointsLedger;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.ledger.presentation.dto.PointsLedgerEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMapper {

    PointsLedger toDomain(PointsLedgerJpaEntity entity);

    PointsLedgerJpaEntity toEntity(PointsLedger domain);

    List<PointsLedger> toDomainList(List<PointsLedgerJpaEntity> entities);

    PointsLedgerEntryResponse toEntryResponse(PointsLedgerJpaEntity entity);

    List<PointsLedgerEntryResponse> toEntryResponseList(List<PointsLedgerJpaEntity> entities);
}
