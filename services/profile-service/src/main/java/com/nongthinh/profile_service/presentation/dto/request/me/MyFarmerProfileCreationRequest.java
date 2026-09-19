package com.nongthinh.profile_service.presentation.dto.request.me;

import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record MyFarmerProfileCreationRequest(

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "First name", message = "PERSON_NAME_INVALID")
    String firstName,

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @PersonNameConstraint(min = 1, max = 255, type = "Last name", message = "PERSON_NAME_INVALID")
    String lastName,

    @NotBlank(message = "GENDER_REQUIRED")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "GENDER_INVALID")
    String gender,

    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,

    @Size(max = 10, message = "PROVINCE_ID_INVALID")
    String provinceId,

    UUID communeId,

    @Size(max = 500, message = "ADDRESS_DETAIL_TOO_LONG")
    String addressDetail,

    @URL
    String avatarUrl
) {
}
