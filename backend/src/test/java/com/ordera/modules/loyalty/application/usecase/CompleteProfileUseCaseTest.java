package com.ordera.modules.loyalty.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.impl.CompleteProfileUseCaseImpl;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.CompleteProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.CompleteProfileResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteProfileUseCaseTest {

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompleteProfileUseCaseImpl completeProfileUseCase;

    private UUID tenantId;
    private UUID userId;
    private UUID memberId;
    private UUID tierId;
    private UserJpaEntity user;
    private MemberProfileJpaEntity profile;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        tierId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

        user = UserJpaEntity.builder()
                .phoneNumber("+4798765432")
                .fullName("Pending Member")
                .email(null) // Initially null for OTP phone users
                .build();
        user.setId(userId);
        user.setTenantId(tenantId);

        profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentTierId(tierId)
                .currentPoints(100)
                .build();
        profile.setId(memberId);
        profile.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("should complete profile successfully when user email was initially null (Null-Safety check)")
    void should_complete_profile_successfully_when_user_email_null() {
        CompleteProfileRequest request = CompleteProfileRequest.builder()
                .fullName("David Nguyen")
                .email("david.nguyen@example.com")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .gender("MALE")
                .build();

        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .tierName("Silver")
                .build();

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, "david.nguyen@example.com", userId)).thenReturn(false);
        when(tierDefinitionRepository.findByIdAndTenantId(tierId, tenantId)).thenReturn(Optional.of(tier));

        CompleteProfileResponse response = completeProfileUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getFullName()).isEqualTo("David Nguyen");
        assertThat(response.getEmail()).isEqualTo("david.nguyen@example.com");
        assertThat(response.isProfileComplete()).isTrue();
        assertThat(response.getCurrentTier()).isEqualTo("Silver");

        verify(userRepository).save(user);
        verify(memberProfileRepository).save(profile);
    }

    @Test
    @DisplayName("should throw EMAIL_ALREADY_EXISTS when new email is taken by another member")
    void should_throw_email_already_exists_when_email_taken() {
        CompleteProfileRequest request = CompleteProfileRequest.builder()
                .fullName("David Nguyen")
                .email("taken@example.com")
                .build();

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, "taken@example.com", userId)).thenReturn(true);

        assertThatThrownBy(() -> completeProfileUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
        verify(memberProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw MISSING_REQUIRED_FIELDS when fullName or email is blank")
    void should_throw_missing_required_fields_when_blank() {
        CompleteProfileRequest request = CompleteProfileRequest.builder()
                .fullName("")
                .email("valid@example.com")
                .build();

        assertThatThrownBy(() -> completeProfileUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.MISSING_REQUIRED_FIELDS);
    }
}

