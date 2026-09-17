package com.nongthinh.auth_service.application.port.in.user;

import java.util.UUID;

public interface SynchronizeBrandRoleUseCase {
    void execute(UUID userId);
}
