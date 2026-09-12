package com.nongthinh.profile_service.presentation.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record FarmerProfileUpdateRequest(

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "First name", message = "PERSON_NAME_INVALID")
    String firstName,

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @PersonNameConstraint(min = 3, max = 255, type = "Last name", message = "PERSON_NAME_INVALID")
    String lastName,

    @NotBlank(message = "GENDER_REQUIRED")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "GENDER_INVALID")
    String gender
) {
}
