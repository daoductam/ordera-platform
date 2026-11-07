package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.GetStoreDetailUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.StoreDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStoreDetailUseCaseImpl implements GetStoreDetailUseCase {

    private final StoreRepository storeRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public StoreDetailResponse execute(UUID storeId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }

        StoreJpaEntity store = storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(TenantErrorCode.STORE_NOT_FOUND, "Store with ID '" + storeId + "' does not exist in this tenant."));

        return tenantDtoMapper.toStoreDetailResponse(store);
    }
}
