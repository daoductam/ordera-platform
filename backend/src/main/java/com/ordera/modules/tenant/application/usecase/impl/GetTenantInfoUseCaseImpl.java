package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.GetTenantInfoUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
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
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }

        TenantJpaEntity tenantEntity = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND, "No active tenant found with ID '" + tenantId + "'."));

        return tenantDtoMapper.toTenantResponse(tenantEntity);
    }
}
