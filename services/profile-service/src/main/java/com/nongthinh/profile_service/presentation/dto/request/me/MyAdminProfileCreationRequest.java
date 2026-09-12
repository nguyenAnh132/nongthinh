package com.nongthinh.profile_service.presentation.dto.request.me;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record MyAdminProfileCreationRequest(

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "First name", message = "PERSON_NAME_INVALID")
    String firstName,

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @PersonNameConstraint(min = 3, max = 255, type = "Last name", message = "PERSON_NAME_INVALID")
    String lastName,

    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,

    @URL(message = "AVATAR_URL_INVALID")
    String avatarUrl
) {
}
