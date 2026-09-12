package com.nongthinh.brand_service.infra.client.profileservice;

import java.time.Instant;
import java.util.UUID;

public record BrandLifecycleLogDto(
        UUID id,
        UUID brandProfileId,
        String action,
        UUID actorUserId,
        String fromStatus,
        String toStatus,
        String payloadJson,
        Instant createdAt
) {
}
