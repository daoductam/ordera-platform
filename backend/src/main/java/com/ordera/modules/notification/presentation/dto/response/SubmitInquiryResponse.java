package com.ordera.modules.notification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO phản hồi tiếp nhận phiếu hỗ trợ đối soát điểm thiếu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitInquiryResponse {

    private String ticketId;
    private String status;
    private String email;
    private String phoneNumber;
    private Instant createdAt;
}

