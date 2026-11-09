package com.ordera.modules.kiosk.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.kiosk.application.usecase.impl.KioskQuickEarnUseCaseImpl;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.adapter.ThermalPrinterAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskQuickEarnRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskQuickEarnResponse;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KioskQuickEarnUseCase Unit Tests")
class KioskQuickEarnUseCaseTest {

    @Mock
    private KioskDeviceJpaRepository kioskDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @Mock
    private AccruePointsUseCase accruePointsUseCase;

    @Mock
    private ThermalPrinterAdapter thermalPrinterAdapter;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private KioskQuickEarnUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";

    @Test
    @DisplayName("should_throw_INVALID_BILL_AMOUNT_when_bill_is_zero_or_negative")
    void should_throw_INVALID_BILL_AMOUNT_when_bill_is_zero_or_negative() {
        // Arrange
        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .billAmount(new BigDecimal("0.50"))
                .build();

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, null, request));
        assertEquals(KioskErrorCode.INVALID_BILL_AMOUNT, ex.getErrorCode());
        verifyNoInteractions(kioskDeviceRepository, userRepository, accruePointsUseCase);
    }

    @Test
    @DisplayName("should_throw_INVALID_PHONE_NUMBER_when_phone_is_invalid")
    void should_throw_INVALID_PHONE_NUMBER_when_phone_is_invalid() {
        // Arrange
        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber("invalid-phone")
                .billAmount(new BigDecimal("93.00"))
                .build();

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, null, request));
        assertEquals(KioskErrorCode.INVALID_PHONE_NUMBER, ex.getErrorCode());
        verifyNoInteractions(kioskDeviceRepository, accruePointsUseCase);
    }

    @Test
    @DisplayName("should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist")
    void should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist() {
        // Arrange
        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .billAmount(new BigDecimal("93.00"))
                .build();
        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(false);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, null, request));
        assertEquals(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND, ex.getErrorCode());
        verify(kioskDeviceRepository).existsByTenantIdAndDeviceCode(tenantId, deviceId);
        verifyNoInteractions(accruePointsUseCase);
    }

    @Test
    @DisplayName("should_throw_INVALID_STAFF_PIN_when_pin_is_wrong")
    void should_throw_INVALID_STAFF_PIN_when_pin_is_wrong() {
        // Arrange
        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .billAmount(new BigDecimal("93.00"))
                .build();
        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, "9999", request));
        assertEquals(KioskErrorCode.INVALID_STAFF_PIN, ex.getErrorCode());
        verifyNoInteractions(accruePointsUseCase);
    }

    @Test
    @DisplayName("should_accrue_points_and_print_receipt_for_registered_customer")
    void should_accrue_points_and_print_receipt_for_registered_customer() {
        // Arrange
        String phone = "+4791234567";
        BigDecimal billAmount = new BigDecimal("93.00");
        UUID userId = UUID.randomUUID();
        UUID tierId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber(phone)
                .billAmount(billAmount)
                .externalReceiptRef("POS-20261105-0891")
                .build();

        UserJpaEntity user = UserJpaEntity.builder()
                .phoneNumber(phone)
                .fullName("Đào Tâm Đức")
                .build();
        user.setId(userId);
        user.setTenantId(tenantId);

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentTierId(tierId)
                .build();
        profile.setId(userId);
        profile.setTenantId(tenantId);

        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .tierName("Gold Member")
                .earnPointsRate(new BigDecimal("1.5"))
                .build();
        tier.setId(tierId);

        PointsLedgerJpaEntity ledger = PointsLedgerJpaEntity.builder()
                .id(txId)
                .balanceAfter(2590)
                .pointsDelta(140)
                .build();

        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, phone)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(tierDefinitionRepository.findById(tierId)).thenReturn(Optional.of(tier));
        when(accruePointsUseCase.execute(any(AccruePointsCommand.class))).thenReturn(ledger);

        // Act
        KioskQuickEarnResponse response = useCase.execute(tenantId, deviceId, "1234", request);

        // Assert
        assertNotNull(response);
        assertEquals(txId.toString(), response.getTransactionId());
        assertEquals(phone, response.getPhoneNumber());
        assertEquals("Đào Tâm Đức", response.getMemberName());
        assertEquals(billAmount, response.getBillAmount());
        assertEquals(KioskConstants.DEFAULT_CURRENCY, response.getCurrency());
        assertEquals(140, response.getPointsEarned());
        assertEquals(2590, response.getNewTotalPoints());
        assertEquals("Gold Member", response.getCurrentTier());
        assertNotNull(response.getQueueNumber());
        assertTrue(response.getQueueNumber().startsWith("#A-"));

        verify(accruePointsUseCase).execute(argThat(cmd ->
                cmd.getPointsDelta() == 140 &&
                cmd.getReferenceId().equals("POS-20261105-0891")
        ));
        verify(thermalPrinterAdapter).printQuickEarnReceipt(eq(storeId), anyString(), eq("POS-20261105-0891"), eq(billAmount), eq(140));
    }
}
