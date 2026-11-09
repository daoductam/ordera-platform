package com.ordera.modules.notification.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.notification.application.usecase.impl.SubmitMissingPointsInquiryUseCaseImpl;
import com.ordera.modules.notification.domain.exception.NotificationErrorCode;
import com.ordera.modules.notification.infrastructure.mapper.NotificationDtoMapper;
import com.ordera.modules.notification.infrastructure.persistence.entity.SupportInquiryJpaEntity;
import com.ordera.modules.notification.infrastructure.persistence.repository.SupportInquiryJpaRepository;
import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitMissingPointsInquiryUseCase Unit Tests")
class SubmitMissingPointsInquiryUseCaseTest {

    @Mock
    private SupportInquiryJpaRepository supportInquiryJpaRepository;

    @Mock
    private NotificationDtoMapper notificationDtoMapper;

    @InjectMocks
    private SubmitMissingPointsInquiryUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Should successfully submit inquiry and generate ticket code INQ-YYYYMMDD-XXX")
    void should_submit_inquiry_and_generate_ticket_code() {
        // Arrange
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("david.nguyen@example.com")
                .phoneNumber("+4797362748")
                .receiptCode("RCP-907")
                .orderDate(LocalDate.now())
                .billAmount(new BigDecimal("290.00"))
                .note("Tôi mua 290 NOK tại quầy Sentrum nhưng chưa thấy điểm vào ví.")
                .receiptPhotoUrl("https://storage.ordera.no/claims/rcp-907.jpg")
                .build();

        when(supportInquiryJpaRepository.countTodayInquiries(eq(tenantId), any(Instant.class), any(Instant.class)))
                .thenReturn(8L);

        SupportInquiryJpaEntity entity = new SupportInquiryJpaEntity();
        entity.setTicketCode("INQ-20261109-009");
        entity.setEmail(request.getEmail());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setStatus("PENDING");
        entity.setCreatedAt(Instant.now());

        when(notificationDtoMapper.toInquiryEntity(eq(tenantId), eq(userId), startsWith("INQ-"), eq(request)))
                .thenReturn(entity);
        when(supportInquiryJpaRepository.save(entity)).thenReturn(entity);
        when(notificationDtoMapper.toInquiryResponse(entity)).thenReturn(
                SubmitInquiryResponse.builder()
                        .ticketId("INQ-20261109-009")
                        .status("PENDING")
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .createdAt(entity.getCreatedAt())
                        .build()
        );

        // Act
        SubmitInquiryResponse response = useCase.execute(tenantId, userId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTicketId()).contains("INQ-");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getEmail()).isEqualTo("david.nguyen@example.com");

        verify(supportInquiryJpaRepository).save(entity);
    }

    @Test
    @DisplayName("Should allow anonymous guest user to submit inquiry (userId is null)")
    void should_allow_anonymous_guest_submission() {
        // Arrange
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("guest@example.no")
                .phoneNumber("97362748")
                .note("Khách vãng lai cần kiểm tra điểm thẻ cũ.")
                .build();

        when(supportInquiryJpaRepository.countTodayInquiries(eq(tenantId), any(Instant.class), any(Instant.class)))
                .thenReturn(0L);

        SupportInquiryJpaEntity entity = new SupportInquiryJpaEntity();
        entity.setTicketCode("INQ-20261109-001");
        entity.setEmail(request.getEmail());
        entity.setStatus("PENDING");

        when(notificationDtoMapper.toInquiryEntity(eq(tenantId), isNull(), startsWith("INQ-"), eq(request)))
                .thenReturn(entity);
        when(supportInquiryJpaRepository.save(entity)).thenReturn(entity);
        when(notificationDtoMapper.toInquiryResponse(entity)).thenReturn(
                SubmitInquiryResponse.builder().ticketId("INQ-20261109-001").status("PENDING").build()
        );

        // Act
        SubmitInquiryResponse response = useCase.execute(tenantId, null, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should throw INQUIRY_VALIDATION_FAILED when email is invalid")
    void should_throw_when_email_invalid() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("invalid-email")
                .phoneNumber("+4797362748")
                .note("Valid note text...")
                .build();

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("Should throw INQUIRY_VALIDATION_FAILED when phoneNumber is invalid")
    void should_throw_when_phone_invalid() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("valid@example.com")
                .phoneNumber("123") // Less than 8 digits
                .note("Valid note text...")
                .build();

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("Should throw INQUIRY_VALIDATION_FAILED when note is shorter than 5 characters")
    void should_throw_when_note_too_short() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("valid@example.com")
                .phoneNumber("+4797362748")
                .note("Hi")
                .build();

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("Should throw INQUIRY_VALIDATION_FAILED when billAmount is negative")
    void should_throw_when_bill_amount_negative() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("valid@example.com")
                .phoneNumber("+4797362748")
                .note("Valid note text...")
                .billAmount(new BigDecimal("-10.00"))
                .build();

        assertThatThrownBy(() -> useCase.execute(tenantId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.INQUIRY_VALIDATION_FAILED);
    }

    @Test
    @DisplayName("Should throw TENANT_HEADER_MISSING when tenantId is null")
    void should_throw_when_tenant_id_null() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("valid@example.com")
                .phoneNumber("+4797362748")
                .note("Valid note text...")
                .build();

        assertThatThrownBy(() -> useCase.execute(null, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.TENANT_HEADER_MISSING);
    }
}

