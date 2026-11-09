package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEReceiptEmailRequest {

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email format")
    private String email;
}
