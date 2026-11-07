package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.ToggleFavoriteStoreUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.CustomerFavoriteStoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.ToggleFavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToggleFavoriteStoreUseCaseImpl implements ToggleFavoriteStoreUseCase {

    private final StoreRepository storeRepository;
    private final CustomerFavoriteStoreRepository customerFavoriteStoreRepository;

    @Override
    @Transactional
    public ToggleFavoriteResponse execute(UUID storeId, UUID userId, boolean favorite) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }

        // Kiểm tra cửa hàng tồn tại trong tenant
        storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(TenantErrorCode.STORE_NOT_FOUND, "Store with ID '" + storeId + "' does not exist in this tenant."));

        if (favorite) {
            boolean alreadyFavorite = customerFavoriteStoreRepository.existsByTenantIdAndUserIdAndStoreId(tenantId, userId, storeId);
            if (!alreadyFavorite) {
                CustomerFavoriteStoreJpaEntity entity = CustomerFavoriteStoreJpaEntity.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .build();
                entity.setTenantId(tenantId);
                customerFavoriteStoreRepository.save(entity);
            }
        } else {
            customerFavoriteStoreRepository.deleteByTenantIdAndUserIdAndStoreId(tenantId, userId, storeId);
        }

        return ToggleFavoriteResponse.builder()
                .storeId(storeId)
                .isFavorite(favorite)
                .build();
    }
}
