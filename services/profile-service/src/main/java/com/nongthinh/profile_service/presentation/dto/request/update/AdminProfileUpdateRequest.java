package com.nongthinh.profile_service.presentation.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record AdminProfileUpdateRequest(

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "First name", message = "PERSON_NAME_INVALID")
    String firstName,

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "Last name", message = "PERSON_NAME_INVALID")
    String lastName
) {
}
