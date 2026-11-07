package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.GetTenantInfoUseCase;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.domain.exception.TenantNotFoundException;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.TenantRepository;
import com.ordera.modules.tenant.presentation.dto.TenantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTenantInfoUseCaseImpl implements GetTenantInfoUseCase {

    private final TenantRepository tenantRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public TenantResponse execute() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantHeaderMissingException();
        }

        TenantJpaEntity tenantEntity = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        return tenantDtoMapper.toTenantResponse(tenantEntity);
    }
}

