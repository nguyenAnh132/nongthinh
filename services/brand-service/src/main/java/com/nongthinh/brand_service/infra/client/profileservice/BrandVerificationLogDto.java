package com.nongthinh.brand_service.infra.client.profileservice;

import java.time.Instant;
import java.util.UUID;

public record BrandVerificationLogDto(
        UUID id,
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        String result,
        String note,
        Instant verifiedAt,
        Instant createdAt
) {
}
