package com.nongthinh.auth_service.presentation.dto.request;

import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record
RegisterFarmerRequest(
    @Email(message = "EMAIL_REQUEST_FORMAT_INVALID")
    String email,
    @NotBlank(message = "PASSWORD_REQUIRED")
    String password,
    @NotNull(message = "TEMPORARY_REQUIRED")
    Boolean temporary,
    @NotNull(message = "ENABLED_REQUIRED")
    Boolean enabled,
    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @Size(min = 1, max = 255, message = "FIRST_NAME_LENGTH_INVALID")
    String firstName,
    @NotBlank(message = "LAST_NAME_REQUIRED")
    @Size(min = 3, max = 255, message = "LAST_NAME_LENGTH_INVALID")
    String lastName,
    @NotBlank(message = "GENDER_REQUIRED")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "GENDER_INVALID")
    String gender,
    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,
    @Size(max = 10, message = "PROVINCE_ID_INVALID")
    String provinceId,
    UUID communeId,
    @Size(max = 500, message = "ADDRESS_DETAIL_TOO_LONG")
    String addressDetail,
    @URL(message = "AVATAR_URL_INVALID")
    String avatarUrl
) {
}
