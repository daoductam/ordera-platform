package com.ordera.modules.catalog.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductJpaEntity extends BaseTenantAuditEntity {

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "subcategory_id")
    private UUID subcategoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "pieces_text", length = 50)
    private String piecesText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dietary_tags", nullable = false)
    @Builder.Default
    private String dietaryTags = "[]";

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "vat_rate", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("15.00");
}
