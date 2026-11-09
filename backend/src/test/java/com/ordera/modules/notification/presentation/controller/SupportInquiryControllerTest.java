package com.ordera.modules.notification.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.modules.notification.application.usecase.SubmitMissingPointsInquiryUseCase;
import com.ordera.modules.notification.presentation.constant.NotificationMessages;
import com.ordera.modules.notification.presentation.dto.request.SubmitInquiryRequest;
import com.ordera.modules.notification.presentation.dto.response.SubmitInquiryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupportInquiryController Unit Tests")
class SupportInquiryControllerTest {

    @Mock
    private SubmitMissingPointsInquiryUseCase submitMissingPointsInquiryUseCase;

    @InjectMocks
    private SupportInquiryController controller;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("8.6: Should submit missing points inquiry successfully and return 201 Created")
    void should_submit_missing_points_inquiry() {
        SubmitInquiryRequest request = SubmitInquiryRequest.builder()
                .email("test@ordera.no")
                .phoneNumber("+4791234567")
                .note("Missing points for order 123")
                .orderDate(LocalDate.now())
                .billAmount(new BigDecimal("150.00"))
                .build();

        SubmitInquiryResponse responseData = SubmitInquiryResponse.builder()
                .ticketId("INQ-20261109-001")
                .status("PENDING")
                .email("test@ordera.no")
                .phoneNumber("+4791234567")
                .createdAt(Instant.now())
                .build();

        when(submitMissingPointsInquiryUseCase.execute(tenantId, userId, request)).thenReturn(responseData);

        ResponseEntity<ApiResponse<SubmitInquiryResponse>> response =
                controller.submitMissingPointsInquiry(tenantId, userId.toString(), request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(NotificationMessages.MSG_INQUIRY_SUBMITTED);
        assertThat(response.getBody().getData().getTicketId()).isEqualTo("INQ-20261109-001");
        verify(submitMissingPointsInquiryUseCase).execute(tenantId, userId, request);
    }
}

