package com.nongthinh.auth_service.application.command;

import java.util.UUID;

public record RegisterBrandCommand(
    String email,
    String password,
    boolean temporary,
    boolean enabled,
    String brandName,
    String taxCode,
    String description,
    String phone,
    String officeProvinceId,
    UUID officeCommuneId,
    String officeAddressDetail,
    String representativeName,
    String representativePhone,
    String representativeEmail,
    String logoUrl,
    String bannerUrl,
    String websiteUrl
) {
}
