package com.nongthinh.auth_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record FarmerProfileCreationRequestedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    String gender,
    String phone,
    String provinceId,
    UUID communeId,
    String addressDetail,
    String avatarUrl
) implements DomainEvent {


}
