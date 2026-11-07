package com.ordera.modules.tenant.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentOptionsResponse {

    private List<SupportedTypeDto> supportedTypes;
    private TimeOptionsDto timeOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportedTypeDto {
        private String type;
        private String label;
        private String icon;
        private boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeOptionsDto {
        private AsapOptionDto asap;
        private ScheduleOptionDto schedule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsapOptionDto {
        private String label;
        private Integer estimatedPreparingMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleOptionDto {
        private String label;
        private String sublabel;
        private boolean allowSameDayOnly;
        private List<String> availableSlots;
    }
}

