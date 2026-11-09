package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.CartItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity, UUID> {

    List<CartItemJpaEntity> findByCartId(UUID cartId);

    Optional<CartItemJpaEntity> findByIdAndCartId(UUID id, UUID cartId);

    void deleteByCartId(UUID cartId);
}
