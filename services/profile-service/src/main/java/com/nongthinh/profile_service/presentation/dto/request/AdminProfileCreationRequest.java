package com.nongthinh.profile_service.presentation.dto.request;

import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record AdminProfileCreationRequest(

    @NotNull(message = "USER_ID_REQUIRED")
    UUID userId,

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "First name", message = "PERSON_NAME_INVALID")
    String firstName,

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "Last name", message = "PERSON_NAME_INVALID")
    String lastName,

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,

    @URL(message = "AVATAR_URL_INVALID")
    String avatarUrl
) {
}
