package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.GetMyRewardsWalletUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.mapper.RewardDtoMapper;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.MyRewardsWalletResponse;
import com.ordera.modules.reward.presentation.dto.UserGiftItemResponse;
import com.ordera.modules.reward.presentation.dto.UserVoucherItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UseCase lấy danh sách ưu đãi và quà tặng trong ví cá nhân của hội viên (Figma 07.03, 07.08).
 * Áp dụng Orchestrator Pattern, phân lập Multi-Tenant và xử lý lọc theo type/status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyRewardsWalletUseCaseImpl implements GetMyRewardsWalletUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final VoucherJpaRepository voucherRepository;
    private final UserGiftRedemptionJpaRepository userGiftRedemptionRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final RewardDtoMapper rewardDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public MyRewardsWalletResponse execute(String status, String type) {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity memberProfile = resolveMemberProfile(tenantId, userId);

        String effectiveType = (type == null || type.isBlank()) ? RewardConstants.TYPE_ALL : type.trim().toUpperCase();
        String effectiveStatus = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        List<UserVoucherItemResponse> voucherItems = shouldIncludeVouchers(effectiveType)
                ? fetchUserVouchers(tenantId, memberProfile.getId(), effectiveStatus)
                : List.of();

        List<UserGiftItemResponse> giftItems = shouldIncludeGifts(effectiveType)
                ? fetchUserGifts(tenantId, memberProfile.getId(), effectiveStatus)
                : List.of();

        int totalCount = voucherItems.size() + giftItems.size();
        return MyRewardsWalletResponse.builder()
                .totalCount(totalCount)
                .vouchers(voucherItems)
                .gifts(giftItems)
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

    private boolean shouldIncludeVouchers(String type) {
        return RewardConstants.TYPE_ALL.equalsIgnoreCase(type) || RewardConstants.TYPE_VOUCHER.equalsIgnoreCase(type);
    }

    private boolean shouldIncludeGifts(String type) {
        return RewardConstants.TYPE_ALL.equalsIgnoreCase(type) || RewardConstants.TYPE_GIFT.equalsIgnoreCase(type);
    }

    private List<UserVoucherItemResponse> fetchUserVouchers(UUID tenantId, UUID memberId, String status) {
        List<UserVoucherJpaEntity> userVouchers = (status != null)
                ? userVoucherRepository.findByTenantIdAndMemberIdAndStatus(tenantId, memberId, status)
                : userVoucherRepository.findByTenantIdAndMemberId(tenantId, memberId);

        if (userVouchers.isEmpty()) {
            return List.of();
        }

        List<UUID> voucherIds = userVouchers.stream().map(UserVoucherJpaEntity::getVoucherId).toList();
        Map<UUID, VoucherJpaEntity> voucherMap = voucherRepository.findAllById(voucherIds).stream()
                .collect(Collectors.toMap(VoucherJpaEntity::getId, v -> v));

        List<UserVoucherItemResponse> result = new ArrayList<>();
        for (UserVoucherJpaEntity uv : userVouchers) {
            VoucherJpaEntity v = voucherMap.get(uv.getVoucherId());
            if (v != null) {
                result.add(rewardDtoMapper.toUserVoucherItemResponse(uv, v));
            }
        }
        return result;
    }

    private List<UserGiftItemResponse> fetchUserGifts(UUID tenantId, UUID memberId, String status) {
        List<UserGiftRedemptionJpaEntity> redemptions = (status != null)
                ? userGiftRedemptionRepository.findByTenantIdAndMemberIdAndStatus(tenantId, memberId, status)
                : userGiftRedemptionRepository.findByTenantIdAndMemberIdOrderByCreatedAtDesc(tenantId, memberId);

        if (redemptions.isEmpty()) {
            return List.of();
        }

        List<UUID> giftIds = redemptions.stream().map(UserGiftRedemptionJpaEntity::getGiftId).toList();
        Map<UUID, LoyaltyGiftJpaEntity> giftMap = loyaltyGiftRepository.findAllById(giftIds).stream()
                .collect(Collectors.toMap(LoyaltyGiftJpaEntity::getId, g -> g));

        List<UserGiftItemResponse> result = new ArrayList<>();
        for (UserGiftRedemptionJpaEntity r : redemptions) {
            LoyaltyGiftJpaEntity g = giftMap.get(r.getGiftId());
            if (g != null) {
                result.add(rewardDtoMapper.toUserGiftItemResponse(r, g));
            }
        }
        return result;
    }
}

