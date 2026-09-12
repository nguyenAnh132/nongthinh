package com.nongthinh.notification_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandProfileApprovedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID brandProfileId,
        UUID userId,
        String brandName,
        String representativeName,
        String representativeEmail,
        UUID actorUserId
) {
}
