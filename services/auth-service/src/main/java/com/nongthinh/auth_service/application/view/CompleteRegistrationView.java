package com.nongthinh.auth_service.application.view;

import java.util.UUID;

public record CompleteRegistrationView(UUID userId, boolean refreshRequired) {
}
