package com.nongthinh.auth_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandProfileCreationRequestedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID userId,
    String email,
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
) implements DomainEvent {


}
