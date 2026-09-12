package com.nongthinh.profile_service.presentation.dto.request.update;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.NotBlank;

public record UrlUpdateRequest(

    @NotBlank(message = "URL_REQUIRED")
    @URL(message = "URL_INVALID")
    String url
) {
}
