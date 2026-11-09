package com.ordera.modules.order.application.service;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.OrderCalculationUtils;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.infrastructure.adapter.RedisCartStorageAdapter;
import com.ordera.modules.order.infrastructure.mapper.OrderDtoMapper;
import com.ordera.modules.order.infrastructure.persistence.entity.CartItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.CartJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.CartItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.CartJpaRepository;
import com.ordera.modules.order.presentation.dto.request.AddCartItemRequest;
import com.ordera.modules.order.presentation.dto.response.CartItemDetailResponse;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dịch vụ quản lý giỏ hàng khách hàng.
 * Phối hợp lưu trữ phân tán Redis (TTL 7 ngày) và cơ sở dữ liệu Postgres.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisCartStorageAdapter redisCartStorage;
    private final CartJpaRepository cartJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;
    private final ProductRepository productRepository;
    private final ProductModifierRepository productModifierRepository;
    private final StoreRepository storeRepository;
    private final OrderDtoMapper orderDtoMapper;

    /**
     * Lấy chi tiết giỏ hàng hiện tại (API 6.1).
     */
    @Transactional
    public CartResponse getMyCart(UUID tenantId, UUID userId, UUID storeId) {
        Cart cart = getOrCreateCartDomain(tenantId, userId, storeId);
        return buildCartResponse(tenantId, cart);
    }

    /**
     * Thêm món ăn vào giỏ kèm modifiers (API 6.2).
     */
    @Transactional
    public CartResponse addItem(UUID tenantId, UUID userId, AddCartItemRequest request) {
        if (request.getQuantity() < 1 || request.getQuantity() > 99) {
            throw new BusinessException(OrderErrorCode.INVALID_QUANTITY);
        }

        ProductJpaEntity product = productRepository.findByIdAndTenantId(request.getProductId(), tenantId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isAvailable()) {
            throw new BusinessException(OrderErrorCode.STOCK_UNAVAILABLE, "Món ăn hiện đang tạm ngưng phục vụ.");
        }

        UUID storeId = request.getStoreId() != null ? request.getStoreId() : product.getTenantId();
        Cart cart = getOrCreateCartDomain(tenantId, userId, storeId);

        List<ProductModifierJpaEntity> modifiers = Collections.emptyList();
        if (request.getSelectedModifierIds() != null && !request.getSelectedModifierIds().isEmpty()) {
            modifiers = productModifierRepository.findAllById(request.getSelectedModifierIds());
        }

        List<BigDecimal> modifierPrices = modifiers.stream()
                .map(ProductModifierJpaEntity::getExtraPrice)
                .toList();

        List<String> modifierNames = modifiers.stream()
                .map(ProductModifierJpaEntity::getName)
                .toList();

        List<UUID> selectedIds = request.getSelectedModifierIds() != null
                ? new ArrayList<>(request.getSelectedModifierIds())
                : new ArrayList<>();
        Collections.sort(selectedIds);

        // Tìm xem đã có món tương tự cùng bộ modifiers trong giỏ chưa
        Optional<CartItem> matchingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId())
                        && areModifiersEqual(item.getSelectedModifierIds(), selectedIds))
                .findFirst();

        if (matchingItem.isPresent()) {
            CartItem existing = matchingItem.get();
            int newQty = existing.getQuantity() + request.getQuantity();
            if (newQty > 99) {
                newQty = 99;
            }
            existing.setQuantity(newQty);
            existing.setSubtotalPrice(OrderCalculationUtils.calculateItemTotal(product.getBasePrice(), modifierPrices, newQty));
        } else {
            BigDecimal itemTotal = OrderCalculationUtils.calculateItemTotal(
                    product.getBasePrice(), modifierPrices, request.getQuantity());

            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID())
                    .cartId(cart.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getBasePrice())
                    .quantity(request.getQuantity())
                    .subtotalPrice(itemTotal)
                    .selectedModifierIds(selectedIds)
                    .modifierNames(modifierNames)
                    .specialInstruction(request.getSpecialInstructions())
                    .createdAt(Instant.now())
                    .build();
            cart.getItems().add(newItem);
        }

        recalculateAndPersist(tenantId, cart);
        return buildCartResponse(tenantId, cart);
    }

    /**
     * Cập nhật số lượng (+/-) hoặc xóa món khỏi giỏ (API 6.3).
     */
    @Transactional
    public CartResponse updateItemQuantity(UUID tenantId, UUID userId, UUID itemId, int quantity) {
        if (quantity < 0 || quantity > 99) {
            throw new BusinessException(OrderErrorCode.INVALID_QUANTITY);
        }

        Cart cart = getOrCreateCartDomain(tenantId, userId, null);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(OrderErrorCode.CART_ITEM_NOT_FOUND));

        if (quantity == 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
            List<BigDecimal> modifierPrices = Collections.emptyList();
            if (item.getSelectedModifierIds() != null && !item.getSelectedModifierIds().isEmpty()) {
                modifierPrices = productModifierRepository.findAllById(item.getSelectedModifierIds())
                        .stream().map(ProductModifierJpaEntity::getExtraPrice).toList();
            }
            item.setSubtotalPrice(OrderCalculationUtils.calculateItemTotal(item.getUnitPrice(), modifierPrices, quantity));
        }

        recalculateAndPersist(tenantId, cart);
        return buildCartResponse(tenantId, cart);
    }

    @Transactional
    public CartResponse deleteItem(UUID tenantId, UUID userId, UUID itemId) {
        return updateItemQuantity(tenantId, userId, itemId, 0);
    }

    /**
     * Cập nhật ghi chú dị ứng / lưu ý đặc biệt (API 6.4).
     */
    @Transactional
    public CartResponse updateComment(UUID tenantId, UUID userId, String comment) {
        if (comment != null && comment.length() > OrderConstants.MAX_ALLERGY_NOTE_LENGTH) {
            throw new BusinessException(OrderErrorCode.COMMENT_TOO_LONG);
        }

        Cart cart = getOrCreateCartDomain(tenantId, userId, null);
        cart.setComment(comment);
        recalculateAndPersist(tenantId, cart);
        return buildCartResponse(tenantId, cart);
    }

    /**
     * Lấy Cart Domain Object phục vụ UseCase Checkout & Preview.
     */
    public Cart getCartDomain(UUID tenantId, UUID cartId) {
        return redisCartStorage.getCartById(tenantId, cartId)
                .orElseGet(() -> loadCartFromDatabase(tenantId, cartId)
                        .orElseThrow(() -> new BusinessException(OrderErrorCode.CART_NOT_FOUND)));
    }

    /**
     * Dọn dẹp giỏ hàng sau khi thanh toán thành công.
     */
    @Transactional
    public void clearCart(UUID tenantId, UUID cartId, UUID userId) {
        redisCartStorage.deleteCart(tenantId, cartId, userId);
        cartItemJpaRepository.deleteByCartId(cartId);
        cartJpaRepository.deleteByIdAndTenantId(cartId, tenantId);
        log.info("Cleared cart {} for user {} in tenant {}", cartId, userId, tenantId);
    }

    private Cart getOrCreateCartDomain(UUID tenantId, UUID userId, UUID storeId) {
        if (userId != null) {
            Optional<Cart> cached = redisCartStorage.getCartByUserId(tenantId, userId);
            if (cached.isPresent()) {
                return cached.get();
            }
            Optional<CartJpaEntity> dbCart = cartJpaRepository.findByTenantIdAndUserId(tenantId, userId);
            if (dbCart.isPresent()) {
                Cart loaded = buildCartFromEntity(dbCart.get());
                redisCartStorage.saveCart(loaded);
                return loaded;
            }
        }

        UUID finalStoreId = storeId != null ? storeId : UUID.randomUUID();
        CartJpaEntity entity = CartJpaEntity.builder()
                .storeId(finalStoreId)
                .userId(userId)
                .build();
        entity.setTenantId(tenantId);
        entity = cartJpaRepository.save(entity);

        Cart cart = Cart.builder()
                .id(entity.getId())
                .tenantId(tenantId)
                .storeId(finalStoreId)
                .userId(userId)
                .items(new ArrayList<>())
                .subtotalAmount(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        redisCartStorage.saveCart(cart);
        return cart;
    }

    private Optional<Cart> loadCartFromDatabase(UUID tenantId, UUID cartId) {
        return cartJpaRepository.findByIdAndTenantId(cartId, tenantId).map(this::buildCartFromEntity);
    }

    private Cart buildCartFromEntity(CartJpaEntity entity) {
        List<CartItemJpaEntity> itemEntities = cartItemJpaRepository.findByCartId(entity.getId());
        List<UUID> productIds = itemEntities.stream().map(CartItemJpaEntity::getProductId).distinct().toList();
        Map<UUID, ProductJpaEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductJpaEntity::getId, p -> p, (a, b) -> a));

        List<CartItem> items = itemEntities.stream().map(ie -> {
            ProductJpaEntity product = productMap.get(ie.getProductId());
            String name = product != null ? product.getName() : "Item";
            BigDecimal unitPrice = product != null ? product.getBasePrice() : BigDecimal.ZERO;
            return CartItem.builder()
                    .id(ie.getId())
                    .cartId(entity.getId())
                    .productId(ie.getProductId())
                    .productName(name)
                    .unitPrice(unitPrice)
                    .quantity(ie.getQuantity())
                    .subtotalPrice(unitPrice.multiply(BigDecimal.valueOf(ie.getQuantity())))
                    .specialInstruction(ie.getSpecialInstruction())
                    .createdAt(ie.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        List<BigDecimal> itemTotals = items.stream().map(CartItem::getSubtotalPrice).toList();
        BigDecimal subtotal = OrderCalculationUtils.calculateSubtotal(itemTotals);

        return Cart.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .storeId(entity.getStoreId())
                .userId(entity.getUserId())
                .comment(entity.getComment())
                .appliedVoucherCode(entity.getAppliedVoucherCode())
                .appliedGiftId(entity.getAppliedGiftId())
                .items(items)
                .subtotalAmount(subtotal)
                .build();
    }

    private void recalculateAndPersist(UUID tenantId, Cart cart) {
        List<BigDecimal> totals = cart.getItems().stream()
                .map(CartItem::getSubtotalPrice)
                .toList();
        cart.setSubtotalAmount(OrderCalculationUtils.calculateSubtotal(totals));
        cart.setUpdatedAt(Instant.now());

        redisCartStorage.saveCart(cart);

        // Đồng bộ JPA entity
        cartJpaRepository.findByIdAndTenantId(cart.getId(), tenantId).ifPresent(entity -> {
            entity.setComment(cart.getComment());
            entity.setAppliedVoucherCode(cart.getAppliedVoucherCode());
            entity.setAppliedGiftId(cart.getAppliedGiftId());
            cartJpaRepository.save(entity);

            cartItemJpaRepository.deleteByCartId(cart.getId());
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                List<CartItemJpaEntity> entities = cart.getItems().stream().map(item ->
                        CartItemJpaEntity.builder()
                                .cartId(cart.getId())
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .specialInstruction(item.getSpecialInstruction())
                                .build()
                ).toList();
                cartItemJpaRepository.saveAll(entities);
            }
        });
    }

    private CartResponse buildCartResponse(UUID tenantId, Cart cart) {
        String storeName = "Linn Sushi Sentrum";
        if (cart.getStoreId() != null) {
            Optional<StoreJpaEntity> store = storeRepository.findByIdAndTenantId(cart.getStoreId(), tenantId);
            if (store.isPresent()) {
                storeName = store.get().getName();
            }
        }

        List<CartItemDetailResponse> itemResponses = cart.getItems().stream().map(item ->
                CartItemDetailResponse.builder()
                        .itemId(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .itemPrice(item.getUnitPrice())
                        .subtotalPrice(item.getSubtotalPrice())
                        .selectedModifierIds(item.getSelectedModifierIds())
                        .modifierNames(item.getModifierNames())
                        .specialInstructions(item.getSpecialInstruction())
                        .build()
        ).toList();

        int totalCount = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .storeId(cart.getStoreId())
                .storeName(storeName)
                .totalItemCount(totalCount)
                .items(itemResponses)
                .comment(cart.getComment())
                .appliedVoucherCode(cart.getAppliedVoucherCode())
                .appliedGiftId(cart.getAppliedGiftId())
                .subtotalAmount(cart.getSubtotalAmount())
                .currency(OrderConstants.CURRENCY_NOK)
                .build();
    }

    private boolean areModifiersEqual(List<UUID> list1, List<UUID> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        List<UUID> s1 = new ArrayList<>(list1);
        List<UUID> s2 = new ArrayList<>(list2);
        Collections.sort(s1);
        Collections.sort(s2);
        return s1.equals(s2);
    }
}
