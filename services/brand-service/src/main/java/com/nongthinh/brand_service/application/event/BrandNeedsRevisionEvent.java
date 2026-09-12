package com.nongthinh.brand_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandNeedsRevisionEvent(
        UUID eventId,
        Instant occurredAt,
        UUID brandProfileId,
        UUID userId,
        String brandName,
        String representativeName,
        String representativeEmail,
        String revisionReason,
        UUID actorUserId
) implements DomainEvent {
}
