package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    private UUID id;
    private String code;
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String status;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}

