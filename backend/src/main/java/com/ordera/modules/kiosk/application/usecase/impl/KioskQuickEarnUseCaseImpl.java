package com.ordera.modules.kiosk.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.KioskQueueUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.kiosk.application.usecase.KioskQuickEarnUseCase;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.adapter.ThermalPrinterAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskQuickEarnRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskQuickEarnResponse;
import com.ordera.modules.ledger.domain.model.TransactionType;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Orchestrator UseCase: Tích điểm nhanh tại quầy khi khách thanh toán qua POS thu ngân ngoài.
 * Tuân thủ Orchestrator Pattern (execute <= 35 dòng) và ghi nhận sổ cái kép bất biến.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KioskQuickEarnUseCaseImpl implements KioskQuickEarnUseCase {

    private final KioskDeviceJpaRepository kioskDeviceRepository;
    private final UserRepository userRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final AccruePointsUseCase accruePointsUseCase;
    private final ThermalPrinterAdapter thermalPrinterAdapter;
    private final RedissonClient redissonClient;

    private static final Pattern PHONE_PATTERN = Pattern.compile(KioskConstants.PHONE_REGEX);
    private static final String DEFAULT_REQUIRED_STAFF_PIN = "1234";

    @Override
    public KioskQuickEarnResponse execute(UUID tenantId, String deviceId, String staffPin, KioskQuickEarnRequest request) {
        validateKioskDevice(tenantId, deviceId, staffPin, request);
        UserJpaEntity customer = lookupCustomerByPhone(tenantId, request.getPhoneNumber());
        CustomerTierData tierData = resolveCustomerTier(tenantId, customer.getId());

        int earnedPoints = calculateEarnPoints(tierData.earnRate(), request.getBillAmount());
        PointsLedgerJpaEntity ledger = persistKioskLedgerEntry(tenantId, customer.getId(), earnedPoints, request.getExternalReceiptRef());

        String queueNumber = formatQueueTicket(request.getStoreId());
        dispatchThermalPrint(request.getStoreId(), queueNumber, request.getExternalReceiptRef(), request.getBillAmount(), earnedPoints);

        return buildResponse(customer, tierData.tierName(), request.getBillAmount(), earnedPoints, ledger.getBalanceAfter(), queueNumber, ledger.getId());
    }

    private void validateKioskDevice(UUID tenantId, String deviceId, String staffPin, KioskQuickEarnRequest request) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (request.getBillAmount() == null || request.getBillAmount().compareTo(BigDecimal.valueOf(KioskConstants.MIN_BILL_AMOUNT)) < 0) {
            throw new BusinessException(KioskErrorCode.INVALID_BILL_AMOUNT);
        }
        if (request.getPhoneNumber() == null || !PHONE_PATTERN.matcher(request.getPhoneNumber().trim()).matches()) {
            throw new BusinessException(KioskErrorCode.INVALID_PHONE_NUMBER);
        }
        if (!kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId)) {
            throw new BusinessException(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND);
        }
        if (staffPin != null && !staffPin.isBlank() && !DEFAULT_REQUIRED_STAFF_PIN.equals(staffPin.trim())) {
            throw new BusinessException(KioskErrorCode.INVALID_STAFF_PIN);
        }
    }

    private UserJpaEntity lookupCustomerByPhone(UUID tenantId, String phoneNumber) {
        String cleanPhone = phoneNumber.trim();
        return userRepository.findByTenantIdAndPhoneNumber(tenantId, cleanPhone)
                .orElseGet(() -> createGuestCustomer(tenantId, cleanPhone));
    }

    private UserJpaEntity createGuestCustomer(UUID tenantId, String phoneNumber) {
        UserJpaEntity newUser = UserJpaEntity.builder()
                .phoneNumber(phoneNumber)
                .email(phoneNumber.replace("+", "") + "@guest.kiosk.local")
                .fullName(KioskConstants.DEFAULT_GUEST_NAME)
                .phoneVerified(true)
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();
        newUser.setTenantId(tenantId);
        UserJpaEntity savedUser = userRepository.save(newUser);

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(savedUser.getId())
                .currentPoints(KioskConstants.ZERO_POINTS)
                .lifetimePoints(KioskConstants.ZERO_POINTS)
                .build();
        profile.setId(savedUser.getId());
        profile.setTenantId(tenantId);
        profile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(profile);
        return savedUser;
    }

    private CustomerTierData resolveCustomerTier(UUID tenantId, UUID userId) {
        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .or(() -> memberProfileRepository.findByIdAndTenantId(userId, tenantId))
                .orElse(null);
        if (profile != null && profile.getCurrentTierId() != null) {
            Optional<TierDefinitionJpaEntity> tierOpt = tierDefinitionRepository.findById(profile.getCurrentTierId());
            if (tierOpt.isPresent()) {
                return new CustomerTierData(tierOpt.get().getTierName(), tierOpt.get().getEarnPointsRate().doubleValue());
            }
        }
        return new CustomerTierData(KioskConstants.DEFAULT_TIER_NAME, KioskConstants.DEFAULT_EARN_RATE_PERCENT);
    }

    private int calculateEarnPoints(double earnRate, BigDecimal billAmount) {
        double calculated = billAmount.doubleValue() * earnRate;
        return (int) Math.round(calculated);
    }

    private PointsLedgerJpaEntity persistKioskLedgerEntry(UUID tenantId, UUID memberId, int pointsDelta, String receiptRef) {
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.KIOSK_EARN)
                .pointsDelta(pointsDelta)
                .referenceType(KioskConstants.REFERENCE_TYPE_EXTERNAL_RECEIPT)
                .referenceId(receiptRef != null ? receiptRef : UUID.randomUUID().toString())
                .description("Tích điểm nhanh tại Kiosk thu ngân")
                .build();
        return accruePointsUseCase.execute(command);
    }

    private String formatQueueTicket(UUID storeId) {
        return KioskQueueUtils.generateQueueNumber(redissonClient, storeId);
    }

    private void dispatchThermalPrint(UUID storeId, String queueNumber, String receiptRef, BigDecimal amount, int points) {
        thermalPrinterAdapter.printQuickEarnReceipt(storeId, queueNumber, receiptRef, amount, points);
    }

    private KioskQuickEarnResponse buildResponse(UserJpaEntity user, String tierName, BigDecimal billAmount,
                                                 int earnedPoints, int balanceAfter, String queueNumber, UUID txId) {
        return KioskQuickEarnResponse.builder()
                .transactionId(txId.toString())
                .phoneNumber(user.getPhoneNumber())
                .memberName(user.getFullName())
                .billAmount(billAmount)
                .currency(KioskConstants.DEFAULT_CURRENCY)
                .pointsEarned(earnedPoints)
                .newTotalPoints(balanceAfter)
                .currentTier(tierName)
                .queueNumber(queueNumber)
                .build();
    }

    private record CustomerTierData(String tierName, double earnRate) {}
}
