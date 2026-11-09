package com.ordera.modules.notification.infrastructure.persistence.repository;

import com.ordera.modules.notification.infrastructure.persistence.entity.SupportInquiryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository quản lý phiếu yêu cầu hỗ trợ đối soát điểm thiếu.
 * Phân lập theo tenant_id.
 */
@Repository
public interface SupportInquiryJpaRepository extends JpaRepository<SupportInquiryJpaEntity, UUID> {

    Optional<SupportInquiryJpaEntity> findByTenantIdAndTicketCode(UUID tenantId, String ticketCode);

    Page<SupportInquiryJpaEntity> findByTenantIdAndStatus(UUID tenantId, String status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SupportInquiryJpaEntity s " +
           "WHERE s.tenantId = :tenantId AND s.createdAt >= :startOfDay AND s.createdAt < :endOfDay")
    long countTodayInquiries(
            @Param("tenantId") UUID tenantId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );
}

