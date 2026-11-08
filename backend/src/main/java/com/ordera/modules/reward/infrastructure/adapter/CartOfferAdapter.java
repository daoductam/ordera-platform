package com.ordera.modules.reward.infrastructure.adapter;

import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.order.infrastructure.persistence.entity.CartItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.CartJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.CartItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.CartJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter triển khai CartOfferPort sử dụng Spring Data JPA Repositories.
 * Tính toán subtotal đơn hàng dựa trên số lượng và giá sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartOfferAdapter implements CartOfferPort {

    private final CartJpaRepository cartRepository;
    private final CartItemJpaRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CartOfferDetails> findCartByIdAndTenantId(UUID cartId, UUID tenantId) {
        return cartRepository.findByIdAndTenantId(cartId, tenantId).map(cart -> {
            BigDecimal subtotal = calculateCartSubtotal(cart.getId());
            return new CartOfferDetails(
                    cart.getId(),
                    cart.getTenantId(),
                    cart.getStoreId(),
                    cart.getUserId(),
                    subtotal,
                    cart.getAppliedVoucherCode(),
                    cart.getAppliedGiftId()
            );
        });
    }

    @Override
    @Transactional
    public void updateAppliedVoucher(UUID cartId, UUID tenantId, String voucherCode) {
        cartRepository.findByIdAndTenantId(cartId, tenantId).ifPresent(cart -> {
            cart.setAppliedVoucherCode(voucherCode);
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void clearAppliedVoucher(UUID cartId, UUID tenantId) {
        cartRepository.findByIdAndTenantId(cartId, tenantId).ifPresent(cart -> {
            cart.setAppliedVoucherCode(null);
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void updateAppliedGift(UUID cartId, UUID tenantId, UUID giftId) {
        cartRepository.findByIdAndTenantId(cartId, tenantId).ifPresent(cart -> {
            cart.setAppliedGiftId(giftId);
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void clearAppliedGift(UUID cartId, UUID tenantId) {
        cartRepository.findByIdAndTenantId(cartId, tenantId).ifPresent(cart -> {
            cart.setAppliedGiftId(null);
            cartRepository.save(cart);
        });
    }

    private BigDecimal calculateCartSubtotal(UUID cartId) {
        List<CartItemJpaEntity> items = cartItemRepository.findByCartId(cartId);
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<UUID> productIds = items.stream().map(CartItemJpaEntity::getProductId).toList();
        Map<UUID, BigDecimal> priceMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductJpaEntity::getId, ProductJpaEntity::getBasePrice));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemJpaEntity item : items) {
            BigDecimal price = priceMap.getOrDefault(item.getProductId(), BigDecimal.ZERO);
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return subtotal;
    }
}
