package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.QrCodeUtils;
import com.ordera.modules.tenant.application.usecase.ResolveTableQrUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreTableRepository;
import com.ordera.modules.tenant.presentation.dto.ResolvedTableQrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResolveTableQrUseCaseImpl implements ResolveTableQrUseCase {

    private final StoreTableRepository storeTableRepository;
    private final StoreRepository storeRepository;

    @Override
    public ResolvedTableQrResponse execute(String qrToken) {
        UUID tenantId = validateAndGetTenantId();
        String rawToken = validateAndGetRawToken(qrToken);

        StoreTableJpaEntity table = resolveStoreTable(tenantId, rawToken);
        StoreJpaEntity store = findStoreOrThrow(table.getStoreId(), tenantId);

        return mapToResolvedResponse(table, store);
    }

    private UUID validateAndGetTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private String validateAndGetRawToken(String qrToken) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new BusinessException(TenantErrorCode.INVALID_QR_TOKEN);
        }
        return qrToken.trim();
    }

    private StoreTableJpaEntity resolveStoreTable(UUID tenantId, String rawToken) {
        // 1. Thử khớp chính xác (không phân biệt hoa/thường)
        Optional<StoreTableJpaEntity> tableOpt = storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, rawToken);

        // 2. Thử chuẩn hóa dấu gạch ngang sang gạch dưới
        if (tableOpt.isEmpty()) {
            String underscoreToken = QrCodeUtils.normalizeHyphenToUnderscore(rawToken);
            tableOpt = storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, underscoreToken);
        }

        // 3. Fallback: Phân tích số bàn từ token bằng QrCodeUtils
        if (tableOpt.isEmpty()) {
            tableOpt = findTableByExtractedNumber(tenantId, rawToken);
        }

        return tableOpt.orElseThrow(() -> new BusinessException(
                TenantErrorCode.INVALID_QR_TOKEN,
                "Mã QR bàn ăn '" + rawToken + "' không hợp lệ hoặc không thuộc chi nhánh này."
        ));
    }

    private Optional<StoreTableJpaEntity> findTableByExtractedNumber(UUID tenantId, String rawToken) {
        Optional<Integer> tableNumOpt = QrCodeUtils.extractTableNumber(rawToken);
        if (tableNumOpt.isEmpty()) {
            return Optional.empty();
        }

        int num = tableNumOpt.get();
        String formattedTable1 = QrCodeUtils.formatTableNumber(num, true);
        String formattedTable2 = QrCodeUtils.formatTableNumber(num, false);

        return storeTableRepository.findAll().stream()
                .filter(t -> tenantId.equals(t.getTenantId()))
                .filter(t -> formattedTable1.equalsIgnoreCase(t.getTableNumber()) || formattedTable2.equalsIgnoreCase(t.getTableNumber()))
                .findFirst();
    }

    private StoreJpaEntity findStoreOrThrow(UUID storeId, UUID tenantId) {
        return storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        TenantErrorCode.STORE_NOT_FOUND,
                        "Store with ID '" + storeId + "' does not exist in this tenant."
                ));
    }

    private ResolvedTableQrResponse mapToResolvedResponse(StoreTableJpaEntity table, StoreJpaEntity store) {
        return ResolvedTableQrResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .tableId(table.getId())
                .tableNumber(table.getTableNumber())
                .zone(table.getZone())
                .capacity(table.getSeatingCapacity())
                .isOccupied(table.isOccupied())
                .build();
    }
}
