package com.nongthinh.auth_service.application.port.in.user;

import com.nongthinh.auth_service.application.command.RegisterFarmerCommand;

public interface RegisterFarmerUseCase {

    void execute(RegisterFarmerCommand command);

}
