package com.ordera.modules.tenant.infrastructure.mapper;

import com.ordera.modules.tenant.domain.model.CustomerFavoriteStore;
import com.ordera.modules.tenant.domain.model.KioskDevice;
import com.ordera.modules.tenant.domain.model.Store;
import com.ordera.modules.tenant.domain.model.StoreTable;
import com.ordera.modules.tenant.domain.model.Tenant;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.kiosk.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.ordera.modules.tenant.presentation.dto.StoreDetailResponse;
import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;
import com.ordera.modules.tenant.presentation.dto.StoreTableResponse;
import com.ordera.modules.tenant.presentation.dto.TenantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantDtoMapper {

    Tenant toDomain(TenantJpaEntity entity);
    TenantJpaEntity toEntity(Tenant domain);
    List<Tenant> toTenantDomainList(List<TenantJpaEntity> entities);

    Store toDomain(StoreJpaEntity entity);
    StoreJpaEntity toEntity(Store domain);
    List<Store> toStoreDomainList(List<StoreJpaEntity> entities);

    StoreTable toDomain(StoreTableJpaEntity entity);
    StoreTableJpaEntity toEntity(StoreTable domain);
    List<StoreTable> toStoreTableDomainList(List<StoreTableJpaEntity> entities);

    CustomerFavoriteStore toDomain(CustomerFavoriteStoreJpaEntity entity);
    CustomerFavoriteStoreJpaEntity toEntity(CustomerFavoriteStore domain);

    KioskDevice toDomain(KioskDeviceJpaEntity entity);
    KioskDeviceJpaEntity toEntity(KioskDevice domain);

    default TenantResponse toTenantResponse(TenantJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> metadata = entity.getMetadata();

        return TenantResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .slogan(extractMetadataString(metadata, "slogan", null))
                .logoUrl(entity.getLogoUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .status(entity.getStatus())
                .currency(extractMetadataString(metadata, "currency", "NOK"))
                .defaultVatRate(extractMetadataBigDecimal(metadata, "defaultVatRate", new BigDecimal("0.1500")))
                .supportPhone(extractMetadataString(metadata, "supportPhone", null))
                .supportEmail(extractMetadataString(metadata, "supportEmail", null))
                .build();
    }

    default StoreSummaryResponse toStoreSummaryResponse(StoreJpaEntity entity, Double distanceKm, boolean isFavorite) {
        if (entity == null) {
            return null;
        }

        return StoreSummaryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .coverImageUrl(entity.getCoverImageUrl())
                .logoUrl(entity.getLogoUrl())
                .phoneNumber(entity.getPhoneNumber())
                .openingHoursText(entity.getOpeningHours())
                .isOpen(entity.isActive())
                .distanceKm(distanceKm)
                .preparingTimeMinutes(entity.getPreparingTimeMinutes())
                .supportedFulfillmentTypes(resolveSupportedFulfillmentTypes(entity))
                .isFavorite(isFavorite)
                .build();
    }

    default StoreDetailResponse toStoreDetailResponse(StoreJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return StoreDetailResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .coverImageUrl(entity.getCoverImageUrl())
                .logoUrl(entity.getLogoUrl())
                .phoneNumber(entity.getPhoneNumber())
                .openingHoursText(entity.getOpeningHours())
                .isOpen(entity.isActive())
                .preparingTimeMinutes(entity.getPreparingTimeMinutes())
                .supportedFulfillmentTypes(resolveSupportedFulfillmentTypes(entity))
                .deliveryRadiusKm(entity.getDeliveryRadiusKm())
                .deliveryFee(entity.getDeliveryFee())
                .build();
    }

    default List<String> resolveSupportedFulfillmentTypes(StoreJpaEntity entity) {
        if (entity == null) {
            return Collections.emptyList();
        }
        List<String> types = new ArrayList<>();
        if (entity.isAcceptsPickup()) {
            types.add("TAKEAWAY");
        }
        if (entity.isAcceptsDinein()) {
            types.add("DINE_IN");
        }
        if (entity.isAcceptsDelivery()) {
            types.add("DELIVERY");
        }
        return types;
    }

    default StoreTableResponse toStoreTableResponse(StoreTableJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return StoreTableResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .tableNumber(entity.getTableNumber())
                .zone(entity.getZone())
                .seatingCapacity(entity.getSeatingCapacity())
                .qrCodeToken(entity.getQrCodeToken())
                .isOccupied(entity.isOccupied())
                .build();
    }

    private static String extractMetadataString(Map<String, Object> metadata, String key, String defaultValue) {
        if (metadata != null && metadata.get(key) != null) {
            return String.valueOf(metadata.get(key));
        }
        return defaultValue;
    }

    private static BigDecimal extractMetadataBigDecimal(Map<String, Object> metadata, String key, BigDecimal defaultValue) {
        if (metadata != null && metadata.get(key) != null) {
            try {
                return new BigDecimal(metadata.get(key).toString());
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }
}
