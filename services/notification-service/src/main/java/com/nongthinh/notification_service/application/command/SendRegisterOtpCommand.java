package com.nongthinh.notification_service.application.command;

import java.util.UUID;

public record SendRegisterOtpCommand(
        UUID userId,
        String email,
        String otp,
        String userName,
        String expireMinutes
) {
}
