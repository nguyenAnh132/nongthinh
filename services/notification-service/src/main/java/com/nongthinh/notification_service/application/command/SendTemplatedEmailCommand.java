package com.nongthinh.notification_service.application.command;

import java.util.Map;
import java.util.UUID;

public record SendTemplatedEmailCommand(
        String purposeCode,
        UUID userId,
        String email,
        Map<String, Object> variables
) {
}
