package com.nongthinh.profile_service.presentation.dto.request.me;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadBrandDocumentRequest(

    @NotBlank(message = "BUSINESS_LICENSE_URL_REQUIRED")
    @Size(max = 2048, message = "BUSINESS_LICENSE_URL_TOO_LONG")
    String businessLicenseUrl
) {
}
