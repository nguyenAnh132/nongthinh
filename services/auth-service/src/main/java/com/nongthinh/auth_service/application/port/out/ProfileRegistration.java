package com.nongthinh.auth_service.application.port.out;

import java.util.UUID;
import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;

public interface ProfileRegistration {
    void complete(UUID userId, String role, CompleteRegistrationCommand command);
}
