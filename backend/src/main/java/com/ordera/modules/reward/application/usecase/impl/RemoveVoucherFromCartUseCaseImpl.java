package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.RemoveVoucherFromCartUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.CartVoucherCalculationResponse;
import com.ordera.modules.reward.presentation.dto.RemoveVoucherRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Gỡ bỏ mã voucher khỏi giỏ hàng (Figma 08.06, 08.08).
 * Hoàn trả voucher về trạng thái AVAILABLE trong ví và giải phóng liên kết giỏ hàng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveVoucherFromCartUseCaseImpl implements RemoveVoucherFromCartUseCase {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    private final CartOfferPort cartOfferPort;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserVoucherJpaRepository userVoucherRepository;

    @Override
    @Transactional
    public CartVoucherCalculationResponse execute(RemoveVoucherRequest request) {
        Objects.requireNonNull(request, "RemoveVoucherRequest must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(request.getCartId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        if (cart.appliedVoucherCode() == null || !cart.appliedVoucherCode().equalsIgnoreCase(request.getVoucherCode().trim())) {
            throw new BusinessException(RewardErrorCode.CART_OR_VOUCHER_NOT_FOUND);
        }

        // 1. Hoàn trả voucher về trạng thái AVAILABLE
        Optional<UserVoucherJpaEntity> userVoucherOpt = userVoucherRepository.findByAppliedOrderId(request.getCartId());
        userVoucherOpt.ifPresent(uv -> {
            uv.setStatus(RewardConstants.STATUS_AVAILABLE);
            uv.setAppliedOrderId(null);
            userVoucherRepository.save(uv);
        });

        // 2. Gỡ bỏ voucher khỏi giỏ hàng
        cartOfferPort.clearAppliedVoucher(request.getCartId(), tenantId);

        BigDecimal subtotal = (cart.subtotalAmount() != null) ? cart.subtotalAmount() : BigDecimal.ZERO;
        BigDecimal vatAmount = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        return CartVoucherCalculationResponse.builder()
                .cartId(request.getCartId())
                .appliedVoucher(null)
                .subtotalAmount(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .vatAmount(vatAmount)
                .finalTotalAmount(subtotal)
                .build();
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(RewardErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private UUID resolveUserId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(RewardErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private MemberProfileJpaEntity resolveMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }
}
