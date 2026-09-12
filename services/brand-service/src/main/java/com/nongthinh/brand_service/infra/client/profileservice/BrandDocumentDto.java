package com.nongthinh.brand_service.infra.client.profileservice;

import java.time.Instant;
import java.util.UUID;

public record BrandDocumentDto(
        UUID id,
        UUID brandProfileId,
        String businessLicenseUrl,
        String reviewStatus,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
