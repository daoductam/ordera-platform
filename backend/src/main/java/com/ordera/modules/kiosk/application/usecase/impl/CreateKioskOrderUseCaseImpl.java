package com.ordera.modules.kiosk.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.KioskQueueUtils;
import com.ordera.common.util.OrderCalculationUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.kiosk.application.usecase.CreateKioskOrderUseCase;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.infrastructure.adapter.ThermalPrinterAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskCheckoutRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskOrderItemRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCheckoutResponse;
import com.ordera.modules.ledger.domain.model.TransactionType;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Orchestrator UseCase: Khách hàng tự đặt món và thanh toán trực tiếp tại Kiosk.
 * Tính toán tài chính, sinh số thứ tự `#A-08`, phát lệnh in và kết thúc phiên.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateKioskOrderUseCaseImpl implements CreateKioskOrderUseCase {

    private final KioskDeviceJpaRepository kioskDeviceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final AccruePointsUseCase accruePointsUseCase;
    private final ThermalPrinterAdapter thermalPrinterAdapter;
    private final RedisKioskSessionAdapter redisKioskSessionAdapter;
    private final RedissonClient redissonClient;

    private static final Pattern PHONE_PATTERN = Pattern.compile(KioskConstants.PHONE_REGEX);

    @Override
    @Transactional
    public KioskCheckoutResponse execute(UUID tenantId, String deviceId, KioskCheckoutRequest request) {
        validateKioskCheckout(tenantId, deviceId, request);
        ProcessedOrderItems itemsData = processOrderItems(tenantId, request.getItems());
        OrderFinancials financials = calculateFinancials(itemsData.subtotal(), request.getTipAmount());

        UserJpaEntity customer = lookupCustomer(tenantId, request.getCustomerPhone());
        int pointsEarned = creditCustomerPoints(tenantId, customer, financials.subtotal());

        String queueNumber = KioskQueueUtils.generateQueueNumber(redissonClient, request.getStoreId());
        String orderCode = KioskQueueUtils.toOrderCode(queueNumber);

        OrderJpaEntity savedOrder = persistOrder(tenantId, request, customer, financials, pointsEarned, orderCode, queueNumber);
        persistOrderItems(savedOrder.getId(), itemsData.items());

        dispatchPrintAndClearSession(tenantId, request.getStoreId(), deviceId, queueNumber, orderCode);
        return buildResponse(savedOrder, queueNumber, financials, pointsEarned);
    }

    private void validateKioskCheckout(UUID tenantId, String deviceId, KioskCheckoutRequest request) {
        Objects.requireNonNull(tenantId, "TenantId must not be null");
        Objects.requireNonNull(deviceId, "DeviceId must not be null");
        Objects.requireNonNull(request, "Request must not be null");

        if (request.getCustomerPhone() == null || !PHONE_PATTERN.matcher(request.getCustomerPhone().trim()).matches()) {
            throw new BusinessException(KioskErrorCode.INVALID_PHONE_NUMBER);
        }
        if (!kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)) {
            throw new BusinessException(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND);
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(KioskErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private ProcessedOrderItems processOrderItems(UUID tenantId, List<KioskOrderItemRequest> itemRequests) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemJpaEntity> entities = new ArrayList<>();

        for (KioskOrderItemRequest itemReq : itemRequests) {
            ProductJpaEntity product = productRepository.findByIdAndTenantId(itemReq.getProductId(), tenantId)
                    .orElseThrow(() -> new BusinessException(KioskErrorCode.PRODUCT_NOT_FOUND));

            if (!product.isAvailable()) {
                throw new BusinessException(KioskErrorCode.PRODUCT_NOT_FOUND);
            }

            BigDecimal itemTotal = product.getBasePrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemTotal);

            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getBasePrice())
                    .quantity(itemReq.getQuantity())
                    .itemTotal(itemTotal)
                    .build();
            entities.add(itemEntity);
        }
        return new ProcessedOrderItems(subtotal, entities);
    }

    private OrderFinancials calculateFinancials(BigDecimal subtotal, BigDecimal tipInput) {
        BigDecimal tip = (tipInput != null && tipInput.compareTo(BigDecimal.ZERO) >= 0) ? tipInput : BigDecimal.ZERO;
        BigDecimal vat = OrderCalculationUtils.calculateVat(subtotal);
        BigDecimal total = subtotal.add(tip);
        return new OrderFinancials(subtotal, tip, vat, total);
    }

    private UserJpaEntity lookupCustomer(UUID tenantId, String phoneNumber) {
        return userRepository.findByTenantIdAndPhoneNumber(tenantId, phoneNumber.trim()).orElse(null);
    }

    private int creditCustomerPoints(UUID tenantId, UserJpaEntity customer, BigDecimal subtotal) {
        if (customer == null) {
            return KioskConstants.ZERO_POINTS;
        }

        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, customer.getId())
                .or(() -> memberProfileRepository.findByIdAndTenantId(customer.getId(), tenantId))
                .orElse(null);
        double earnRate = KioskConstants.DEFAULT_EARN_RATE_PERCENT;
        if (profile != null && profile.getCurrentTierId() != null) {
            Optional<TierDefinitionJpaEntity> tierOpt = tierDefinitionRepository.findById(profile.getCurrentTierId());
            if (tierOpt.isPresent()) {
                earnRate = tierOpt.get().getEarnPointsRate().doubleValue();
            }
        }

        int points = (int) Math.round(subtotal.doubleValue() * earnRate);
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(customer.getId())
                .transactionType(TransactionType.KIOSK_EARN)
                .pointsDelta(points)
                .referenceType(KioskConstants.REFERENCE_TYPE_KIOSK_ORDER)
                .referenceId(UUID.randomUUID().toString())
                .description("Tích điểm đặt món tại Kiosk")
                .build();
        accruePointsUseCase.execute(command);
        return points;
    }

    private OrderJpaEntity persistOrder(UUID tenantId, KioskCheckoutRequest request, UserJpaEntity customer,
                                        OrderFinancials financials, int pointsEarned, String orderCode, String queueNumber) {
        String customerName = (customer != null) ? customer.getFullName() :
                (request.getCustomerName() != null ? request.getCustomerName() : KioskConstants.DEFAULT_GUEST_NAME);

        OrderJpaEntity order = OrderJpaEntity.builder()
                .storeId(request.getStoreId())
                .userId(customer != null ? customer.getId() : null)
                .orderCode(orderCode)
                .orderNumber(queueNumber)
                .pickupMethod(KioskConstants.FULFILLMENT_TYPE_DINE_IN)
                .pickupPersonName(customerName)
                .pickupPersonPhone(request.getCustomerPhone())
                .subtotalAmount(financials.subtotal())
                .discountAmount(BigDecimal.ZERO)
                .tipAmount(financials.tip())
                .vatAmount(financials.vat())
                .totalAmount(financials.total())
                .pointsEarned(pointsEarned)
                .status(KioskConstants.ORDER_STATUS_PAID)
                .paymentStatus(KioskConstants.PAYMENT_STATUS_PAID)
                .paymentMethod(request.getPaymentMethod())
                .createdAt(Instant.now())
                .build();
        order.setTenantId(tenantId);
        return orderRepository.save(order);
    }

    private void persistOrderItems(UUID orderId, List<OrderItemJpaEntity> items) {
        for (OrderItemJpaEntity item : items) {
            item.setOrderId(orderId);
            orderItemRepository.save(item);
        }
    }

    private void dispatchPrintAndClearSession(UUID tenantId, UUID storeId, String deviceId, String queueNumber, String orderCode) {
        thermalPrinterAdapter.printQueueTicket(storeId, queueNumber, orderCode);
        redisKioskSessionAdapter.clearSession(tenantId, deviceId);
    }

    private KioskCheckoutResponse buildResponse(OrderJpaEntity order, String queueNumber, OrderFinancials financials, int pointsEarned) {
        return KioskCheckoutResponse.builder()
                .orderId(order.getId().toString())
                .orderCode(order.getOrderCode())
                .queueNumber(queueNumber)
                .status(order.getStatus())
                .subtotalAmount(financials.subtotal())
                .tipAmount(financials.tip())
                .vatAmount(financials.vat())
                .totalAmount(financials.total())
                .currency(KioskConstants.DEFAULT_CURRENCY)
                .pointsEarned(pointsEarned)
                .printCommandDispatched(true)
                .resetTimeoutSeconds(KioskConstants.POST_PAYMENT_REDIRECT_SECONDS)
                .build();
    }

    private record ProcessedOrderItems(BigDecimal subtotal, List<OrderItemJpaEntity> items) {}
    private record OrderFinancials(BigDecimal subtotal, BigDecimal tip, BigDecimal vat, BigDecimal total) {}
}
