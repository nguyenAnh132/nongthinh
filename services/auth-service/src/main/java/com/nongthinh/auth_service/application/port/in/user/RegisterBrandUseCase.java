package com.nongthinh.auth_service.application.port.in.user;

import com.nongthinh.auth_service.application.command.RegisterBrandCommand;

public interface RegisterBrandUseCase {

    void execute(RegisterBrandCommand command);
}
