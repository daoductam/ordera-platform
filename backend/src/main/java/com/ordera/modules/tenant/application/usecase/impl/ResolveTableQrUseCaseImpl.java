package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.ResolveTableQrUseCase;
import com.ordera.modules.tenant.domain.exception.InvalidQrTokenException;
import com.ordera.modules.tenant.domain.exception.StoreNotFoundException;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResolveTableQrUseCaseImpl implements ResolveTableQrUseCase {

    private static final Pattern TABLE_NUM_PATTERN = Pattern.compile("(?i)(?:t|tbl|table)[-_ ]*(\\d+)");

    private final StoreTableRepository storeTableRepository;
    private final StoreRepository storeRepository;

    @Override
    public ResolvedTableQrResponse execute(String qrToken) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantHeaderMissingException();
        }

        if (qrToken == null || qrToken.isBlank()) {
            throw new InvalidQrTokenException(qrToken);
        }

        String rawToken = qrToken.trim();

        // 1. Thử khớp chính xác (không phân biệt hoa/thường)
        Optional<StoreTableJpaEntity> tableOpt = storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, rawToken);

        // 2. Thử chuẩn hóa dấu gạch ngang sang gạch dưới
        if (tableOpt.isEmpty()) {
            String underscoreToken = rawToken.replace("-", "_");
            tableOpt = storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, underscoreToken);
        }

        // 3. Fallback: Phân tích số bàn từ token (ví dụ: QR-LINN-SENTRUM-T05 -> Table 05)
        if (tableOpt.isEmpty()) {
            Matcher matcher = TABLE_NUM_PATTERN.matcher(rawToken);
            if (matcher.find()) {
                String digits = matcher.group(1);
                int num = Integer.parseInt(digits);
                String formattedTable1 = String.format("Table %02d", num);
                String formattedTable2 = "Table " + num;

                tableOpt = storeTableRepository.findAll().stream()
                        .filter(t -> tenantId.equals(t.getTenantId()))
                        .filter(t -> formattedTable1.equalsIgnoreCase(t.getTableNumber()) || formattedTable2.equalsIgnoreCase(t.getTableNumber()))
                        .findFirst();
            }
        }

        StoreTableJpaEntity table = tableOpt.orElseThrow(() -> new InvalidQrTokenException(rawToken));

        // Lấy thông tin cửa hàng sở hữu bàn
        StoreJpaEntity store = storeRepository.findByIdAndTenantId(table.getStoreId(), tenantId)
                .orElseThrow(() -> new StoreNotFoundException(table.getStoreId()));

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

