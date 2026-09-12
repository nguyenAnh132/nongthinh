package com.nongthinh.brand_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandDocumentsSubmittedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID brandProfileId,
        UUID userId,
        String brandName
) {
}
