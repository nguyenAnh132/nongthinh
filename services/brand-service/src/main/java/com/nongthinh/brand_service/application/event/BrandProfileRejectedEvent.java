package com.nongthinh.brand_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandProfileRejectedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID brandProfileId,
        UUID userId,
        String brandName,
        String representativeName,
        String representativeEmail,
        String rejectionReason,
        Instant canReRegisterAt,
        UUID actorUserId,
        boolean earlyReject
) implements DomainEvent {
}
