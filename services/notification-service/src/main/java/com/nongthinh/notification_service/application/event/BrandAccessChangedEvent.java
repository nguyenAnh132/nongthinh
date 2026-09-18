package com.nongthinh.notification_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record BrandAccessChangedEvent(UUID eventId, UUID userId, String status, Instant occurredAt) { }
