package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.event.AdminProfileCreationRequestedEvent;

public interface HandleAdminProfileCreationRequestedUseCase {

    void execute(AdminProfileCreationRequestedEvent event);
}
