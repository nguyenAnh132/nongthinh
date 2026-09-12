package com.nongthinh.auth_service.application.port.in.user;

import com.nongthinh.auth_service.application.command.RegisterAdminCommand;

public interface RegisterAdminUseCase {

    void execute(RegisterAdminCommand command);
}
