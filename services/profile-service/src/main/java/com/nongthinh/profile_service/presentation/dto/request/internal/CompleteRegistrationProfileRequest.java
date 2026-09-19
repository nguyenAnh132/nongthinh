package com.nongthinh.profile_service.presentation.dto.request.internal;

import java.util.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationProfileRequest(
        @NotNull UUID userId,
        @NotBlank @Pattern(regexp = "ROLE_ADMIN|ROLE_FARMER|ROLE_BRAND|ROLE_BRAND_PENDING") String role,
        @Size(max = 200) String firstName,
        @Size(max = 200) String lastName,
        @Pattern(regexp = "MALE|FEMALE|OTHER") String gender,
        @NotBlank @Pattern(regexp = "[0-9]{10}") String phone,
        @Size(max = 200) String brandName,
        @Size(max = 200) String representativeName,
        @Pattern(regexp = "[0-9]{10}") String representativePhone,
        @Size(max = 255) @Email String representativeEmail) {
}
