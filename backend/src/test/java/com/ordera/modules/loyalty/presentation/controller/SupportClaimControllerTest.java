package com.ordera.modules.loyalty.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.loyalty.application.usecase.SubmitSupportClaimUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.presentation.dto.SubmitSupportClaimRequest;
import com.ordera.modules.loyalty.presentation.dto.SupportClaimResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupportClaimController WebMvc Tests")
class SupportClaimControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SubmitSupportClaimUseCase submitSupportClaimUseCase;

    @InjectMocks
    private SupportClaimController supportClaimController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supportClaimController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/loyalty/claims - should return 201 Created on valid claim submission")
    void should_return_201_when_submitting_claim() throws Exception {
        UUID claimId = UUID.randomUUID();
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-8921-X")
                .orderDate(LocalDate.now().minusDays(5))
                .billAmount(new BigDecimal("350.00"))
                .storeId(UUID.randomUUID())
                .billPhotoUrl("https://storage.ordera.no/claims/receipt-8921.jpg")
                .note("Quên tích điểm tại quầy")
                .build();

        SupportClaimResponse response = SupportClaimResponse.builder()
                .claimId(claimId)
                .receiptCode("RCP-8921-X")
                .billAmount(new BigDecimal("350.00"))
                .storeId(request.getStoreId())
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        when(submitSupportClaimUseCase.execute(any(SubmitSupportClaimRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/loyalty/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiptCode").value("RCP-8921-X"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/loyalty/claims - should return 409 when claim already exists")
    void should_return_409_when_claim_already_exists() throws Exception {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-EXISTING")
                .orderDate(LocalDate.now().minusDays(2))
                .billAmount(new BigDecimal("100.00"))
                .build();

        when(submitSupportClaimUseCase.execute(any(SubmitSupportClaimRequest.class)))
                .thenThrow(new BusinessException(LoyaltyErrorCode.CLAIM_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/loyalty/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CLAIM_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/v1/loyalty/claims - should return 422 when receipt expired")
    void should_return_422_when_receipt_expired() throws Exception {
        SubmitSupportClaimRequest request = SubmitSupportClaimRequest.builder()
                .receiptCode("RCP-EXPIRED")
                .orderDate(LocalDate.now().minusDays(40))
                .billAmount(new BigDecimal("100.00"))
                .build();

        when(submitSupportClaimUseCase.execute(any(SubmitSupportClaimRequest.class)))
                .thenThrow(new BusinessException(LoyaltyErrorCode.RECEIPT_EXPIRED));

        mockMvc.perform(post("/api/v1/loyalty/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RECEIPT_EXPIRED"));
    }
}

