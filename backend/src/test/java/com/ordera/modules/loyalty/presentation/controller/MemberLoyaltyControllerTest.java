package com.ordera.modules.loyalty.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordera.common.api.PageResponse;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.ledger.application.usecase.GetPointsLedgerHistoryUseCase;
import com.ordera.modules.ledger.presentation.dto.PointsLedgerEntryResponse;
import com.ordera.modules.loyalty.application.usecase.*;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.presentation.dto.*;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberLoyaltyController WebMvc Tests")
class MemberLoyaltyControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GetMemberProfileUseCase getMemberProfileUseCase;

    @Mock
    private UpdateProfileUseCase updateProfileUseCase;

    @Mock
    private CompleteProfileUseCase completeProfileUseCase;

    @Mock
    private UploadAvatarUseCase uploadAvatarUseCase;

    @Mock
    private GetTierDefinitionsUseCase getTierDefinitionsUseCase;

    @Mock
    private GetPointsLedgerHistoryUseCase getPointsLedgerHistoryUseCase;

    @InjectMocks
    private MemberLoyaltyController memberLoyaltyController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberLoyaltyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/v1/loyalty/members/me - should return 200 with member profile")
    void should_return_200_when_getting_my_profile() throws Exception {
        UUID memberId = UUID.randomUUID();
        MemberProfileResponse response = MemberProfileResponse.builder()
                .id(memberId)
                .fullName("David Nguyen")
                .phoneNumber("+4797362748")
                .email("info@ordera.no")
                .barcodeToken("ORD-MEM-97362748")
                .loyalty(LoyaltyTierInfoResponse.builder()
                        .currentTier("Silver")
                        .currentPoints(120)
                        .lifetimePoints(300)
                        .pointsToNextTier(100)
                        .nextTierName("Gold")
                        .progressPercentage(50.0)
                        .build())
                .hasMissingPointsNotice(true)
                .build();

        when(getMemberProfileUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/api/v1/loyalty/members/me")
                        .header("X-Tenant-ID", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("David Nguyen"))
                .andExpect(jsonPath("$.data.barcodeToken").value("ORD-MEM-97362748"))
                .andExpect(jsonPath("$.data.loyalty.currentTier").value("Silver"))
                .andExpect(jsonPath("$.data.loyalty.currentPoints").value(120))
                .andExpect(jsonPath("$.data.loyalty.pointsToNextTier").value(100))
                .andExpect(jsonPath("$.data.hasMissingPointsNotice").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/loyalty/members/me - should return 404 when profile not found")
    void should_return_404_when_profile_not_found() throws Exception {
        when(getMemberProfileUseCase.execute())
                .thenThrow(new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/loyalty/members/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_PROFILE_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/loyalty/members/me - should return 200 on profile update")
    void should_return_200_when_updating_profile() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("David Nguyen")
                .email("david.updated@ordera.no")
                .dateOfBirth(LocalDate.of(2000, 8, 23))
                .gender("MALE")
                .build();

        UpdateProfileResponse response = UpdateProfileResponse.builder()
                .id(UUID.randomUUID())
                .fullName("David Nguyen")
                .email("david.updated@ordera.no")
                .gender("MALE")
                .updatedAt(Instant.now())
                .build();

        when(updateProfileUseCase.execute(any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/loyalty/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("david.updated@ordera.no"));
    }

    @Test
    @DisplayName("POST /api/v1/loyalty/members/complete-profile - should return 200 on profile completion")
    void should_return_200_when_completing_profile() throws Exception {
        CompleteProfileRequest request = CompleteProfileRequest.builder()
                .fullName("ABY TRAN")
                .email("abytran@ordera.no")
                .dateOfBirth(LocalDate.of(2000, 8, 23))
                .gender("FEMALE")
                .build();

        CompleteProfileResponse response = CompleteProfileResponse.builder()
                .memberId(UUID.randomUUID())
                .fullName("ABY TRAN")
                .email("abytran@ordera.no")
                .isProfileComplete(true)
                .currentTier("New")
                .currentPoints(0)
                .build();

        when(completeProfileUseCase.execute(any(CompleteProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/loyalty/members/complete-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("ABY TRAN"))
                .andExpect(jsonPath("$.data.isProfileComplete").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/loyalty/tiers - should return 200 with list of 6 tier definitions")
    void should_return_200_when_getting_tiers() throws Exception {
        List<TierDefinitionResponse> tiers = List.of(
                TierDefinitionResponse.builder().tierName("New").tierOrder(1).minPointsRequired(0).themeColorHex("#1EB759").earnPointsRate(new BigDecimal("1.0000")).build(),
                TierDefinitionResponse.builder().tierName("Silver").tierOrder(2).minPointsRequired(200).themeColorHex("#00A3FF").earnPointsRate(new BigDecimal("1.2000")).build()
        );

        when(getTierDefinitionsUseCase.execute()).thenReturn(tiers);

        mockMvc.perform(get("/api/v1/loyalty/tiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tierName").value("New"))
                .andExpect(jsonPath("$.data[1].tierName").value("Silver"));
    }

    @Test
    @DisplayName("GET /api/v1/loyalty/members/me/points - should return 200 with points ledger pagination")
    void should_return_200_when_getting_points_history() throws Exception {
        PointsLedgerEntryResponse entry = PointsLedgerEntryResponse.builder()
                .id(UUID.randomUUID())
                .transactionType("ORDER_EARN")
                .pointsDelta(170)
                .balanceAfter(290)
                .referenceType("ORDERS")
                .referenceId("ORD-20261105-0891")
                .description("Tích điểm từ đơn hàng Linn Sushi")
                .createdAt(Instant.now())
                .build();

        PageResponse<PointsLedgerEntryResponse> pageResponse = PageResponse.<PointsLedgerEntryResponse>builder()
                .content(List.of(entry))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(getPointsLedgerHistoryUseCase.execute(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/loyalty/members/me/points?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("ORDER_EARN"))
                .andExpect(jsonPath("$.data.content[0].pointsDelta").value(170))
                .andExpect(jsonPath("$.data.content[0].balanceAfter").value(290));
    }
}

