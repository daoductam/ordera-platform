package com.ordera.modules.kiosk.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.kiosk.application.usecase.impl.CreateKioskOrderUseCaseImpl;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.infrastructure.adapter.ThermalPrinterAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskCheckoutRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskOrderItemRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCheckoutResponse;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateKioskOrderUseCase Unit Tests")
class CreateKioskOrderUseCaseTest {

    @Mock
    private KioskDeviceJpaRepository kioskDeviceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @Mock
    private OrderJpaRepository orderRepository;

    @Mock
    private OrderItemJpaRepository orderItemRepository;

    @Mock
    private AccruePointsUseCase accruePointsUseCase;

    @Mock
    private ThermalPrinterAdapter thermalPrinterAdapter;

    @Mock
    private RedisKioskSessionAdapter redisKioskSessionAdapter;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private CreateKioskOrderUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";
    private final UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("should_throw_PRODUCT_NOT_FOUND_when_product_does_not_exist")
    void should_throw_PRODUCT_NOT_FOUND_when_product_does_not_exist() {
        // Arrange
        KioskCheckoutRequest request = KioskCheckoutRequest.builder()
                .storeId(storeId)
                .kioskDeviceId(deviceId)
                .customerPhone("+4791234567")
                .paymentMethod("CARD")
                .items(List.of(KioskOrderItemRequest.builder()
                        .productId(productId)
                        .quantity(1)
                        .build()))
                .build();

        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId, request));
        assertEquals(KioskErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
        verifyNoInteractions(orderRepository, thermalPrinterAdapter);
    }

    @Test
    @DisplayName("should_place_order_successfully_and_print_ticket")
    void should_place_order_successfully_and_print_ticket() {
        // Arrange
        KioskCheckoutRequest request = KioskCheckoutRequest.builder()
                .storeId(storeId)
                .kioskDeviceId(deviceId)
                .customerPhone("+4791234567")
                .customerName("Đào Tâm Đức")
                .tipAmount(BigDecimal.ZERO)
                .paymentMethod("CARD")
                .items(List.of(KioskOrderItemRequest.builder()
                        .productId(productId)
                        .quantity(2)
                        .build()))
                .build();

        ProductJpaEntity product = ProductJpaEntity.builder()
                .name("Sake Nigiri")
                .basePrice(new BigDecimal("46.50"))
                .available(true)
                .build();
        product.setId(productId);
        product.setTenantId(tenantId);

        when(kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(true);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, "+4791234567")).thenReturn(Optional.empty());

        when(orderRepository.save(any(OrderJpaEntity.class))).thenAnswer(inv -> {
            OrderJpaEntity order = inv.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        // Act
        KioskCheckoutResponse response = useCase.execute(tenantId, deviceId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertNotNull(response.getOrderCode());
        assertNotNull(response.getQueueNumber());
        assertEquals("PAID", response.getStatus());
        assertEquals(new BigDecimal("93.00"), response.getSubtotalAmount());
        assertEquals(new BigDecimal("93.00"), response.getTotalAmount());
        assertTrue(response.isPrintCommandDispatched());
        assertEquals(KioskConstants.POST_PAYMENT_REDIRECT_SECONDS, response.getResetTimeoutSeconds());

        verify(orderRepository).save(any(OrderJpaEntity.class));
        verify(orderItemRepository).save(any());
        verify(thermalPrinterAdapter).printQueueTicket(eq(storeId), anyString(), anyString());
        verify(redisKioskSessionAdapter).clearSession(tenantId, deviceId);
    }
}
