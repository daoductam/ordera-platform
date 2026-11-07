package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.GetStoreTablesUseCase;
import com.ordera.modules.tenant.domain.exception.StoreNotFoundException;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreTableRepository;
import com.ordera.modules.tenant.presentation.dto.StoreTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStoreTablesUseCaseImpl implements GetStoreTablesUseCase {

    private final StoreRepository storeRepository;
    private final StoreTableRepository storeTableRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public List<StoreTableResponse> execute(UUID storeId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantHeaderMissingException();
        }

        // Đảm bảo store tồn tại và thuộc quyền sở hữu của tenant hiện tại
        storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        List<StoreTableJpaEntity> tables = storeTableRepository.findByTenantIdAndStoreId(tenantId, storeId);

        return tables.stream()
                .map(tenantDtoMapper::toStoreTableResponse)
                .toList();
    }
}

