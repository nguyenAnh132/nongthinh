package com.nongthinh.brand_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandProfileCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID brandProfileId,
        UUID userId,
        String brandName
) {
}
