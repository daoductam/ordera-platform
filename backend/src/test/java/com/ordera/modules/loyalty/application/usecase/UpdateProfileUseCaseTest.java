package com.ordera.modules.loyalty.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.impl.UpdateProfileUseCaseImpl;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateProfileUseCaseImpl updateProfileUseCase;

    private UUID tenantId;
    private UUID userId;
    private UUID memberId;
    private UserJpaEntity user;
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

        user = UserJpaEntity.builder()
                .phoneNumber("+4798765432")
                .fullName("Current Name")
                .email("current@example.com")
                .build();
        user.setId(userId);
        user.setTenantId(tenantId);

        profile = MemberProfileJpaEntity.builder()
                .userId(userId)
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
    @DisplayName("should update profile successfully when user email was initially null (Null-Safety check)")
    void should_update_profile_successfully_when_user_email_null() {
        user.setEmail(null);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("New Name")
                .email("new@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("FEMALE")
                .avatarUrl("https://storage.ordera.no/avatars/new.png")
                .build();

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, "new@example.com", userId)).thenReturn(false);

        UpdateProfileResponse response = updateProfileUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("New Name");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getAvatarUrl()).isEqualTo("https://storage.ordera.no/avatars/new.png");

        verify(userRepository).save(user);
        verify(memberProfileRepository).save(profile);
    }

    @Test
    @DisplayName("should throw EMAIL_ALREADY_EXISTS when updating to an email already in use")
    void should_throw_email_already_exists_when_email_taken() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("New Name")
                .email("taken@example.com")
                .build();

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByTenantIdAndEmailAndIdNot(tenantId, "taken@example.com", userId)).thenReturn(true);

        assertThatThrownBy(() -> updateProfileUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
        verify(memberProfileRepository, never()).save(any());
    }
}

