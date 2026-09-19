package com.nongthinh.auth_service.presentation.dto.request;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAdminRequest(
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
    @Size(min = 1, max = 255, message = "LAST_NAME_LENGTH_INVALID")
    String lastName,
    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,
    @NotBlank(message = "ADMIN_GROUP_REQUIRED")
    @Pattern(regexp = "SUPER_ADMIN|OPERATION", message = "ADMIN_GROUP_INVALID")
    String adminGroup,
    @URL(message = "AVATAR_URL_INVALID")
    String avatarUrl
) {
}
