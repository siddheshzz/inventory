package com.siddhesh.inventoryManagement.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequestDto {

    @NotBlank(message = "Phone number is required")
    // Senior Standard: Validates international E.164 phone formats (e.g., +1234567890)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format. Must match E.164 standard")
    private String phoneNumber;
}
