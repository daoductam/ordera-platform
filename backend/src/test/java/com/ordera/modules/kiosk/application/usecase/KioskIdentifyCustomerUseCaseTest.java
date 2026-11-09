package com.ordera.modules.kiosk.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.kiosk.application.usecase.impl.KioskIdentifyCustomerUseCaseImpl;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskIdentifyRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCustomerInfoResponse;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KioskIdentifyCustomerUseCase Unit Tests")
class KioskIdentifyCustomerUseCaseTest {

    @Mock
    private KioskDeviceJpaRepository kioskDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @Mock
    private UserVoucherJpaRepository userVoucherRepository;

    @Mock
    private RedisKioskSessionAdapter redisKioskSessionAdapter;

    @InjectMocks
    private KioskIdentifyCustomerUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";

    @Test
    @DisplayName("should_throw_INVALID_PHONE_NUMBER_when_phone_format_is_invalid")
    void should_throw_INVALID_PHONE_NUMBER_when_phone_format_is_invalid() {
        // Arrange
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber("123")
                .build();

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, request));
        assertEquals(KioskErrorCode.INVALID_PHONE_NUMBER, ex.getErrorCode());
        verifyNoInteractions(kioskDeviceRepository, userRepository);
    }

    @Test
    @DisplayName("should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist")
    void should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist() {
        // Arrange
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .build();
        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(false);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, request));
        assertEquals(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND, ex.getErrorCode());
        verify(kioskDeviceRepository).existsByTenantIdAndDeviceCode(tenantId, deviceId);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("should_return_registered_customer_with_tier_and_vouchers_when_phone_exists")
    void should_return_registered_customer_with_tier_and_vouchers_when_phone_exists() {
        // Arrange
        String phone = "+4791234567";
        UUID userId = UUID.randomUUID();
        UUID tierId = UUID.randomUUID();

        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber(phone)
                .build();

        UserJpaEntity user = UserJpaEntity.builder()
                .phoneNumber(phone)
                .fullName("Đào Tâm Đức")
                .email("duc@example.com")
                .build();
        user.setId(userId);
        user.setTenantId(tenantId);

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(2450)
                .lifetimePoints(5200)
                .currentTierId(tierId)
                .build();
        profile.setId(userId);
        profile.setTenantId(tenantId);

        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .tierName("Gold Member")
                .earnPointsRate(new BigDecimal("2.0"))
                .build();
        tier.setId(tierId);

        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, phone)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(tierDefinitionRepository.findById(tierId)).thenReturn(Optional.of(tier));
        when(userVoucherRepository.countByTenantIdAndMemberIdAndStatus(tenantId, userId, "ACTIVE")).thenReturn(2L);

        // Act
        KioskCustomerInfoResponse response = useCase.execute(tenantId, deviceId, request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isRegistered());
        assertEquals(userId, response.getUserId());
        assertEquals("Đào Tâm Đức", response.getName());
        assertEquals("Gold Member", response.getCurrentTier());
        assertEquals(2450, response.getCurrentPoints());
        assertEquals(5200, response.getLifetimePoints());
        assertEquals(2.0, response.getEarnRatePercent());
        assertEquals(2, response.getAvailableVoucherCount());
        verify(redisKioskSessionAdapter).saveSession(eq(tenantId), eq(deviceId), any());
    }

    @Test
    @DisplayName("should_return_unregistered_guest_customer_when_phone_not_found")
    void should_return_unregistered_guest_customer_when_phone_not_found() {
        // Arrange
        String phone = "+4798888888";
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber(phone)
                .build();

        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, phone)).thenReturn(Optional.empty());

        // Act
        KioskCustomerInfoResponse response = useCase.execute(tenantId, deviceId, request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isRegistered());
        assertNull(response.getUserId());
        assertEquals(KioskConstants.DEFAULT_GUEST_NAME, response.getName());
        assertEquals(KioskConstants.DEFAULT_TIER_NAME, response.getCurrentTier());
        assertEquals(KioskConstants.ZERO_POINTS, response.getCurrentPoints());
        assertEquals(KioskConstants.DEFAULT_EARN_RATE_PERCENT, response.getEarnRatePercent());
        assertEquals(KioskConstants.DEFAULT_AVAILABLE_VOUCHERS, response.getAvailableVoucherCount());
        verify(redisKioskSessionAdapter).saveSession(eq(tenantId), eq(deviceId), any());
    }
}
