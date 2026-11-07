package com.ordera.modules.tenant.infrastructure.mapper;

import com.ordera.modules.tenant.domain.model.CustomerFavoriteStore;
import com.ordera.modules.tenant.domain.model.KioskDevice;
import com.ordera.modules.tenant.domain.model.Store;
import com.ordera.modules.tenant.domain.model.StoreTable;
import com.ordera.modules.tenant.domain.model.Tenant;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.KioskDeviceJpaEntity;
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
        String slogan = null;
        String currency = "NOK";
        BigDecimal defaultVatRate = new BigDecimal("0.1500");
        String supportPhone = null;
        String supportEmail = null;

        if (metadata != null) {
            if (metadata.get("slogan") != null) {
                slogan = String.valueOf(metadata.get("slogan"));
            }
            if (metadata.get("currency") != null) {
                currency = String.valueOf(metadata.get("currency"));
            }
            if (metadata.get("defaultVatRate") != null) {
                try {
                    defaultVatRate = new BigDecimal(metadata.get("defaultVatRate").toString());
                } catch (Exception ignored) {
                }
            }
            if (metadata.get("supportPhone") != null) {
                supportPhone = String.valueOf(metadata.get("supportPhone"));
            }
            if (metadata.get("supportEmail") != null) {
                supportEmail = String.valueOf(metadata.get("supportEmail"));
            }
        }

        return TenantResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .slogan(slogan)
                .logoUrl(entity.getLogoUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .status(entity.getStatus())
                .currency(currency)
                .defaultVatRate(defaultVatRate)
                .supportPhone(supportPhone)
                .supportEmail(supportEmail)
                .build();
    }

    default StoreSummaryResponse toStoreSummaryResponse(StoreJpaEntity entity, Double distanceKm, boolean isFavorite) {
        if (entity == null) {
            return null;
        }
        List<String> fulfillmentTypes = new ArrayList<>();
        if (entity.isAcceptsPickup()) {
            fulfillmentTypes.add("TAKEAWAY");
        }
        if (entity.isAcceptsDinein()) {
            fulfillmentTypes.add("DINE_IN");
        }
        if (entity.isAcceptsDelivery()) {
            fulfillmentTypes.add("DELIVERY");
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
                .supportedFulfillmentTypes(fulfillmentTypes)
                .isFavorite(isFavorite)
                .build();
    }

    default StoreDetailResponse toStoreDetailResponse(StoreJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<String> fulfillmentTypes = new ArrayList<>();
        if (entity.isAcceptsPickup()) {
            fulfillmentTypes.add("TAKEAWAY");
        }
        if (entity.isAcceptsDinein()) {
            fulfillmentTypes.add("DINE_IN");
        }
        if (entity.isAcceptsDelivery()) {
            fulfillmentTypes.add("DELIVERY");
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
                .supportedFulfillmentTypes(fulfillmentTypes)
                .deliveryRadiusKm(entity.getDeliveryRadiusKm())
                .deliveryFee(entity.getDeliveryFee())
                .build();
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
}

