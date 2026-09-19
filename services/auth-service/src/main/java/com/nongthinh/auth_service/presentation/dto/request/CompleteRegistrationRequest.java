package com.nongthinh.auth_service.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationRequest(
        @Size(max = 200, message = "REGISTRATION_PROFILE_INVALID") String firstName,
        @Size(max = 200, message = "REGISTRATION_PROFILE_INVALID") String lastName,
        @Pattern(regexp = "MALE|FEMALE|OTHER", message = "GENDER_INVALID") String gender,
        @NotBlank(message = "PHONE_REQUIRED") @Pattern(regexp = "[0-9]{10}", message = "PHONE_INVALID") String phone,
        @Size(max = 200, message = "REGISTRATION_PROFILE_INVALID") String brandName,
        @Size(max = 200, message = "REGISTRATION_PROFILE_INVALID") String representativeName,
        @Pattern(regexp = "[0-9]{10}", message = "PHONE_INVALID") String representativePhone,
        @Size(max = 255, message = "REGISTRATION_PROFILE_INVALID") @Email(message = "REPRESENTATIVE_EMAIL_INVALID") String representativeEmail) {
}
