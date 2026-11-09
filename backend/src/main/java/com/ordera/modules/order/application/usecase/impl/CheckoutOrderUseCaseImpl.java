package com.ordera.modules.order.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.OrderCalculationUtils;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.ledger.domain.model.TransactionType;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.CheckoutOrderUseCase;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.domain.model.OrderStatus;
import com.ordera.modules.order.domain.model.PaymentStatus;
import com.ordera.modules.order.infrastructure.adapter.JavaMailReceiptAdapter;
import com.ordera.modules.order.infrastructure.adapter.RedisCartStorageAdapter;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemModifierJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.request.CheckoutOrderRequest;
import com.ordera.modules.order.presentation.dto.response.CheckoutOrderResponse;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrator UseCase: Khởi tạo đơn hàng chính thức & xử lý thanh toán (API 6.6).
 * Áp dụng Orchestrator Pattern chống God Method (execute <= 35 dòng).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutOrderUseCaseImpl implements CheckoutOrderUseCase {

    private final RedissonClient redissonClient;
    private final RedisCartStorageAdapter redisCartStorage;
    private final CartService cartService;
    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final OrderItemModifierJpaRepository orderItemModifierRepository;
    private final ProductModifierRepository productModifierRepository;
    private final VoucherJpaRepository voucherRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final AccruePointsUseCase accruePointsUseCase;
    private final JavaMailReceiptAdapter javaMailReceiptAdapter;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public CheckoutOrderResponse execute(UUID tenantId, UUID userId, String idempotencyKey, CheckoutOrderRequest request) {
        checkIdempotency(tenantId, idempotencyKey);
        RLock lock = acquireCheckoutLock(userId, request.getCartId());
        try {
            CalculatedOrderData calcData = validateAndCalculateAmounts(tenantId, userId, request);
            OrderPersistenceResult result = persistOrderAndRewardsTransactionally(tenantId, userId, request, calcData);
            clearCartCache(tenantId, request.getCartId(), userId);
            dispatchReceiptEmail(result.order(), result.items());
            recordIdempotency(tenantId, idempotencyKey, result.order().getId());
            return buildCheckoutResponse(result.order());
        } finally {
            releaseLock(lock);
        }
    }

    private OrderPersistenceResult persistOrderAndRewardsTransactionally(
            UUID tenantId, UUID userId, CheckoutOrderRequest request, CalculatedOrderData calcData) {
        if (transactionManager != null) {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            return txTemplate.execute(status -> {
                OrderPersistenceResult res = createOrderEntities(tenantId, userId, request, calcData);
                applyRewardsAndPoints(tenantId, userId, res.order(), request);
                return res;
            });
        }
        OrderPersistenceResult res = createOrderEntities(tenantId, userId, request, calcData);
        applyRewardsAndPoints(tenantId, userId, res.order(), request);
        return res;
    }

    private void checkIdempotency(UUID tenantId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        Optional<String> existingOrderId = redisCartStorage.getIdempotentOrderId(tenantId, idempotencyKey);
        if (existingOrderId.isPresent()) {
            log.warn("Idempotency conflict detected for key: {}", idempotencyKey);
            throw new BusinessException(OrderErrorCode.IDEMPOTENT_OPERATION_CONFLICT);
        }
    }

    private RLock acquireCheckoutLock(UUID userId, UUID cartId) {
        String lockIdentifier = userId != null ? userId.toString() : cartId.toString();
        String lockKey = String.format(OrderConstants.LOCK_KEY_CHECKOUT_PATTERN, lockIdentifier);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(
                    OrderConstants.REDISSON_LOCK_ACQUIRE_TIMEOUT_SEC,
                    OrderConstants.REDISSON_LOCK_LEASE_TIMEOUT_SEC,
                    TimeUnit.SECONDS
            );
            if (!acquired) {
                log.warn("Failed to acquire Redisson checkout lock for key: {}", lockKey);
                throw new BusinessException(OrderErrorCode.CONCURRENT_CHECKOUT_LOCK);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(OrderErrorCode.CONCURRENT_CHECKOUT_LOCK, "Luồng checkout bị gián đoạn.");
        }
        return lock;
    }

    private void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    private CalculatedOrderData validateAndCalculateAmounts(UUID tenantId, UUID userId, CheckoutOrderRequest request) {
        Cart cart = cartService.getCartDomain(tenantId, request.getCartId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(OrderErrorCode.CART_EMPTY);
        }

        BigDecimal subtotal = cart.getSubtotalAmount() != null ? cart.getSubtotalAmount() : BigDecimal.ZERO;
        BigDecimal safeTip = request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        UUID appliedVoucherId = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherJpaEntity voucher = voucherRepository.findByTenantIdAndCode(tenantId, request.getVoucherCode().trim())
                    .orElse(null);
            if (voucher != null) {
                if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
                    throw new BusinessException(OrderErrorCode.MINIMUM_ORDER_VALUE_NOT_MET);
                }
                appliedVoucherId = voucher.getId();
                if (RewardConstants.DISCOUNT_TYPE_PERCENT.equalsIgnoreCase(voucher.getDiscountType())) {
                    BigDecimal percent = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
                    discount = subtotal.multiply(percent).divide(new BigDecimal("100"), OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
                } else {
                    BigDecimal fixed = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
                    discount = fixed.min(subtotal);
                }
            }
        }

        String giftTitle = null;
        if (request.getRewardGiftId() != null) {
            Optional<LoyaltyGiftJpaEntity> giftOpt = loyaltyGiftRepository.findByIdAndTenantId(request.getRewardGiftId(), tenantId);
            giftTitle = giftOpt.map(LoyaltyGiftJpaEntity::getTitle).orElse(null);
        }

        BigDecimal total = OrderCalculationUtils.calculateTotal(subtotal, discount, safeTip);
        BigDecimal vat = OrderCalculationUtils.calculateVat(total);

        BigDecimal tierRate = resolveTierRate(tenantId, userId);
        int pointsEarned = OrderCalculationUtils.calculatePointsToEarn(subtotal, discount, tierRate);

        return new CalculatedOrderData(cart, subtotal, discount, safeTip, vat, total, pointsEarned, appliedVoucherId, giftTitle);
    }

    protected OrderPersistenceResult createOrderEntities(
            UUID tenantId, UUID userId, CheckoutOrderRequest request, CalculatedOrderData data) {

        UUID storeId = data.cart().getStoreId() != null ? data.cart().getStoreId() : tenantId;
        String orderNumber = generateDailyOrderNumber(tenantId, storeId);
        String orderCode = generateUniqueOrderCode();

        OrderJpaEntity order = OrderJpaEntity.builder()
                .storeId(storeId)
                .userId(userId)
                .orderNumber(orderNumber)
                .orderCode(orderCode)
                .pickupMethod(request.getPickupMethod())
                .tableNumber(request.getTableNumber())
                .pickupPersonName(request.getPickupPersonName())
                .pickupPersonPhone(request.getPickupPersonPhone())
                .subtotalAmount(data.subtotal())
                .discountAmount(data.discount())
                .tipAmount(data.tip())
                .vatAmount(data.vat())
                .totalAmount(data.total())
                .pointsEarned(data.pointsEarned())
                .status(OrderStatus.PREPARING.name())
                .paymentStatus(PaymentStatus.PAID.name())
                .paymentMethod(request.getPaymentMethod())
                .orderNotes(data.cart().getComment())
                .appliedVoucherId(data.appliedVoucherId())
                .appliedGiftId(request.getRewardGiftId())
                .appliedGiftTitle(data.giftTitle())
                .eReceiptEmail(request.getEReceiptEmail())
                .createdAt(Instant.now())
                .build();
        order.setTenantId(tenantId);
        order = orderRepository.save(order);

        List<OrderItemJpaEntity> savedItems = new ArrayList<>();
        for (CartItem cartItem : data.cart().getItems()) {
            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.builder()
                    .orderId(order.getId())
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .itemTotal(cartItem.getSubtotalPrice())
                    .specialInstruction(cartItem.getSpecialInstruction())
                    .build();
            itemEntity = orderItemRepository.save(itemEntity);
            savedItems.add(itemEntity);

            if (cartItem.getSelectedModifierIds() != null && !cartItem.getSelectedModifierIds().isEmpty()) {
                List<ProductModifierJpaEntity> mods = productModifierRepository.findAllById(cartItem.getSelectedModifierIds());
                for (ProductModifierJpaEntity mod : mods) {
                    OrderItemModifierJpaEntity modifierEntity = OrderItemModifierJpaEntity.builder()
                            .orderItemId(itemEntity.getId())
                            .modifierGroupName("Option")
                            .modifierName(mod.getName())
                            .extraPrice(mod.getExtraPrice())
                            .build();
                    orderItemModifierRepository.save(modifierEntity);
                }
            }
        }

        return new OrderPersistenceResult(order, savedItems);
    }

    private void applyRewardsAndPoints(UUID tenantId, UUID userId, OrderJpaEntity order, CheckoutOrderRequest request) {
        if (userId != null && order.getAppliedVoucherId() != null) {
            Optional<MemberProfileJpaEntity> profileOpt = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId);
            if (profileOpt.isPresent()) {
                UUID memberId = profileOpt.get().getId();
                Optional<UserVoucherJpaEntity> userVoucherOpt = userVoucherRepository
                        .findByTenantIdAndMemberIdAndVoucherIdAndStatus(
                                tenantId, memberId, order.getAppliedVoucherId(), RewardConstants.STATUS_AVAILABLE);
                if (userVoucherOpt.isEmpty()) {
                    userVoucherOpt = userVoucherRepository.findByTenantIdAndMemberIdAndVoucherIdAndStatus(
                            tenantId, memberId, order.getAppliedVoucherId(), RewardConstants.STATUS_IN_USE);
                }
                userVoucherOpt.ifPresent(uv -> {
                    uv.setStatus(RewardConstants.STATUS_USED);
                    uv.setAppliedOrderId(order.getId());
                    userVoucherRepository.save(uv);
                    log.info("Marked user voucher {} as USED for order {}", uv.getId(), order.getOrderCode());
                });
            }
        }

        if (userId != null && order.getPointsEarned() > 0) {
            Optional<MemberProfileJpaEntity> profileOpt = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId);
            profileOpt.ifPresent(profile -> {
                AccruePointsCommand cmd = AccruePointsCommand.builder()
                        .tenantId(tenantId)
                        .memberId(profile.getId())
                        .transactionType(TransactionType.ORDER_EARN)
                        .pointsDelta(order.getPointsEarned())
                        .referenceType("ORDER")
                        .referenceId(order.getId().toString())
                        .description("Tích điểm từ đơn hàng " + order.getOrderCode())
                        .build();
                try {
                    accruePointsUseCase.execute(cmd);
                    log.info("Accrued {} points for order {}", order.getPointsEarned(), order.getOrderCode());
                } catch (Exception e) {
                    log.error("Failed to accrue points for order {}: {}", order.getOrderCode(), e.getMessage());
                }
            });
        }
    }

    private void clearCartCache(UUID tenantId, UUID cartId, UUID userId) {
        cartService.clearCart(tenantId, cartId, userId);
    }

    private void dispatchReceiptEmail(OrderJpaEntity order, List<OrderItemJpaEntity> items) {
        if (order.getEReceiptEmail() != null && !order.getEReceiptEmail().isBlank()) {
            try {
                javaMailReceiptAdapter.sendReceiptEmail(order, items, order.getEReceiptEmail());
            } catch (Exception e) {
                log.warn("E-Receipt email dispatch failed (non-blocking for checkout): {}", e.getMessage());
            }
        }
    }

    private void recordIdempotency(UUID tenantId, String idempotencyKey, UUID orderId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            redisCartStorage.saveIdempotencyRecord(tenantId, idempotencyKey, orderId.toString());
        }
    }

    private CheckoutOrderResponse buildCheckoutResponse(OrderJpaEntity order) {
        return CheckoutOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderCode(order.getOrderCode())
                .pickupMethod(order.getPickupMethod())
                .tableNumber(order.getTableNumber())
                .totalAmount(order.getTotalAmount())
                .pointsEarned(order.getPointsEarned())
                .status(order.getStatus())
                .estimatedReadyMinutes(OrderConstants.DEFAULT_ESTIMATED_READY_MINUTES)
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateDailyOrderNumber(UUID tenantId, UUID storeId) {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        long count = orderRepository.countByTenantIdAndStoreIdAndCreatedAtGreaterThanEqual(tenantId, storeId, startOfDay);
        return String.format("%s%03d", OrderConstants.QUEUE_NUMBER_PREFIX, count + 1);
    }

    private String generateUniqueOrderCode() {
        String dateStr = LocalDate.now(ZoneOffset.UTC).format(ORDER_DATE_FORMATTER);
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("%s%s-%04d", OrderConstants.ORDER_CODE_PREFIX, dateStr, rand);
    }

    private BigDecimal resolveTierRate(UUID tenantId, UUID userId) {
        if (userId == null) {
            return BigDecimal.ONE;
        }
        Optional<MemberProfileJpaEntity> profileOpt = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId);
        if (profileOpt.isEmpty() || profileOpt.get().getCurrentTierId() == null) {
            return BigDecimal.ONE;
        }
        Optional<TierDefinitionJpaEntity> tierOpt = tierDefinitionRepository.findById(profileOpt.get().getCurrentTierId());
        return tierOpt.map(TierDefinitionJpaEntity::getEarnPointsRate).orElse(BigDecimal.ONE);
    }

    private record CalculatedOrderData(
            Cart cart,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tip,
            BigDecimal vat,
            BigDecimal total,
            int pointsEarned,
            UUID appliedVoucherId,
            String giftTitle
    ) {}

    protected record OrderPersistenceResult(OrderJpaEntity order, List<OrderItemJpaEntity> items) {}
}

