package com.nongthinh.profile_service.application.port.in.admin;

import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;

public interface AdminProfileCreationUseCase {

    void execute(AdminProfileCreationCommand command);
}