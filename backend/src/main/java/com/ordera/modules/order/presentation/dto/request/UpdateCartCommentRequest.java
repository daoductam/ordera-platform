package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartCommentRequest {

    @Size(max = 200, message = "Ghi chú đơn hàng không được vượt quá 200 ký tự.")
    private String comment;
}

