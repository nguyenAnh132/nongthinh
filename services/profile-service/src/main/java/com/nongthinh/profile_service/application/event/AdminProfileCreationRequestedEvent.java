package com.nongthinh.profile_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record AdminProfileCreationRequestedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    String phone,
    String avatarUrl
) {
}
