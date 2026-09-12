package com.nongthinh.auth_service.presentation.dto.request;

import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterBrandRequest(
    @Email(message = "EMAIL_REQUEST_FORMAT_INVALID")
    String email,
    @NotBlank(message = "PASSWORD_REQUIRED")
    String password,
    @NotNull(message = "TEMPORARY_REQUIRED")
    Boolean temporary,
    @NotNull(message = "ENABLED_REQUIRED")
    Boolean enabled,
    @NotBlank(message = "BRAND_NAME_REQUIRED")
    @Size(min = 1, max = 200, message = "BRAND_NAME_LENGTH_INVALID")
    String brandName,
    @Size(max = 20, message = "TAX_CODE_TOO_LONG")
    String taxCode,
    @Size(max = 1000, message = "DESCRIPTION_TOO_LONG")
    String description,
    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String phone,
    @Size(max = 10, message = "PROVINCE_ID_INVALID")
    String officeProvinceId,
    UUID officeCommuneId,
    @Size(max = 500, message = "ADDRESS_DETAIL_TOO_LONG")
    String officeAddressDetail,
    @NotBlank(message = "REPRESENTATIVE_NAME_REQUIRED")
    @Size(min = 1, max = 255, message = "FIRST_NAME_LENGTH_INVALID")
    String representativeName,
    @NotBlank(message = "REPRESENTATIVE_PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10}$", message = "PHONE_INVALID")
    String representativePhone,
    @NotBlank(message = "REPRESENTATIVE_EMAIL_REQUIRED")
    @Email(message = "REPRESENTATIVE_EMAIL_INVALID")
    String representativeEmail,
    @URL(message = "LOGO_URL_INVALID")
    String logoUrl,
    @URL(message = "BANNER_URL_INVALID")
    String bannerUrl,
    @URL(message = "WEBSITE_URL_INVALID")
    String websiteUrl
) {
}
