package com.ordera.modules.order.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineStepResponse {

    private String step;
    private String time;

    @JsonProperty("isDone")
    private boolean isDone;
}

