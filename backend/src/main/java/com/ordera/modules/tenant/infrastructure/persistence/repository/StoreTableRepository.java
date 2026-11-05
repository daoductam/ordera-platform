package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreTableRepository extends JpaRepository<StoreTableJpaEntity, UUID> {

    List<StoreTableJpaEntity> findByStoreId(UUID storeId);

    Optional<StoreTableJpaEntity> findByStoreIdAndTableNumber(UUID storeId, String tableNumber);

    Optional<StoreTableJpaEntity> findByQrCodeToken(String qrCodeToken);
}
