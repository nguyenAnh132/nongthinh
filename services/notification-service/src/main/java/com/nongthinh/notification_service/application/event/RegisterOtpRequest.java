package com.nongthinh.notification_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record RegisterOtpRequest(
        UUID eventId,
        Instant occurredAt,
        UUID userId,
        String email,
        String otp,
        String userName,
        Integer expireMinutes
) {
}
