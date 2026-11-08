package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tùy biến chi tiết (Option) trong nhóm Modifier (ví dụ: Size L +15 Kr, Mango Popping +10 Kr).
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modifier {

    private UUID id;
    private UUID groupId;
    private String name;
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
    @Builder.Default
    private boolean isDefault = false;
}
