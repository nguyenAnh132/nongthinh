package com.nongthinh.profile_service.presentation.dto.request.me;

import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.nongthinh.profile_service.presentation.validator.BrandNameConstraint;
import com.nongthinh.profile_service.presentation.validator.PersonNameConstraint;

public record MyBrandProfileUpdateRequest(

    @NotBlank(message = "BRAND_NAME_REQUIRED")
    @BrandNameConstraint(min = 1, max = 200, type = "Brand name", message = "BRAND_NAME_LENGTH_INVALID")
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
    @PersonNameConstraint(min = 1, max = 255, type = "Representative name", message = "PERSON_NAME_INVALID")
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
