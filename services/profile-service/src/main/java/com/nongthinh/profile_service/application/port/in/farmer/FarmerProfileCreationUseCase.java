package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;

public interface FarmerProfileCreationUseCase {

    void execute(FarmerProfileCreationCommand command);
}
