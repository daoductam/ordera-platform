package com.ordera.modules.loyalty.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.application.usecase.impl.SubmitSupportClaimUseCaseImpl;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.SupportClaimJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.SubmitSupportClaimRequest;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitSupportClaimUseCaseTest {

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private SupportClaimJpaRepository supportClaimRepository;

    @InjectMocks
    private SubmitSupportClaimUseCaseImpl submitSupportClaimUseCase;

    private UUID tenantId;
    private UUID userId;
    private UUID memberId;
    private MemberProfileJpaEntity profile;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

        profile = MemberProfileJpaEntity.builder().build();
        profile.setId(memberId);
        profile.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("should submit support claim successfully for recent receipt")
    void should_submit_claim_successfully() {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-8921-X")
                .orderDate(LocalDate.now().minusDays(5))
                .billAmount(new BigDecimal("350.00"))
                .storeId(UUID.randomUUID())
                .billPhotoUrl("https://storage.ordera.no/claims/rcp-8921.jpg")
                .note("Quên tích điểm tại quầy")
                .build();

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(supportClaimRepository.existsByTenantIdAndReceiptCode(tenantId, "RCP-8921-X")).thenReturn(false);
        when(supportClaimRepository.save(any(SupportClaimJpaEntity.class))).thenAnswer(invocation -> {
            SupportClaimJpaEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        SupportClaimResponse response = submitSupportClaimUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getReceiptCode()).isEqualTo("RCP-8921-X");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getBillAmount()).isEqualTo(new BigDecimal("350.00"));
        verify(supportClaimRepository).save(any(SupportClaimJpaEntity.class));
    }

    @Test
    @DisplayName("should throw RECEIPT_EXPIRED when receipt date is more than 30 days old")
    void should_throw_receipt_expired_when_older_than_30_days() {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-OLD-123")
                .orderDate(LocalDate.now().minusDays(35))
                .billAmount(new BigDecimal("200.00"))
                .build();

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> submitSupportClaimUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.RECEIPT_EXPIRED);

        verify(supportClaimRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw INVALID_RECEIPT_DATE when receipt date is in the future")
    void should_throw_invalid_receipt_date_when_in_future() {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-FUTURE-999")
                .orderDate(LocalDate.now().plusDays(2))
                .billAmount(new BigDecimal("150.00"))
                .build();

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> submitSupportClaimUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.INVALID_RECEIPT_DATE);

        verify(supportClaimRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw CLAIM_ALREADY_EXISTS when receipt code already submitted")
    void should_throw_claim_already_exists_when_duplicate_code() {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-DUPLICATE")
                .orderDate(LocalDate.now().minusDays(2))
                .billAmount(new BigDecimal("150.00"))
                .build();

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(supportClaimRepository.existsByTenantIdAndReceiptCode(tenantId, "RCP-DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> submitSupportClaimUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.CLAIM_ALREADY_EXISTS);

        verify(supportClaimRepository, never()).save(any());
    }
}

