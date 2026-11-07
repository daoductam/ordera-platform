package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.GetFavoriteStoresUseCase;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.CustomerFavoriteStoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFavoriteStoresUseCaseImpl implements GetFavoriteStoresUseCase {

    private final StoreRepository storeRepository;
    private final CustomerFavoriteStoreRepository customerFavoriteStoreRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public List<StoreSummaryResponse> execute(UUID userId, Double userLat, Double userLng) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantHeaderMissingException();
        }

        List<CustomerFavoriteStoreJpaEntity> favorites = customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, userId);
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> storeIds = favorites.stream()
                .map(CustomerFavoriteStoreJpaEntity::getStoreId)
                .toList();

        List<StoreJpaEntity> stores = storeRepository.findAllById(storeIds).stream()
                .filter(s -> tenantId.equals(s.getTenantId()) && s.isActive())
                .toList();

        List<StoreSummaryResponse> result = new ArrayList<>();
        for (StoreJpaEntity store : stores) {
            Double distanceKm = null;
            if (userLat != null && userLng != null && store.getLatitude() != null && store.getLongitude() != null) {
                distanceKm = GetStoresListUseCaseImpl.calculateHaversineDistance(
                        userLat,
                        userLng,
                        store.getLatitude().doubleValue(),
                        store.getLongitude().doubleValue()
                );
            }
            result.add(tenantDtoMapper.toStoreSummaryResponse(store, distanceKm, true));
        }

        if (userLat != null && userLng != null) {
            result.sort(Comparator.comparing(
                    StoreSummaryResponse::getDistanceKm,
                    Comparator.nullsLast(Double::compareTo)
            ));
        }

        return result;
    }
}

