package com.nongthinh.brand_service.infra.client.profileservice;

import java.time.Instant;
import java.util.UUID;

public record BrandProfileDto(
        UUID id,
        UUID userId,
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
        String websiteUrl,
        String status,
        String rejectionReason,
        Instant scheduledDeletionAt,
        Instant rejectedAt,
        Instant approvedAt,
        UUID approvedBy,
        UUID rejectedBy,
        Instant createdAt,
        Instant updatedAt
) {
}
