package com.nongthinh.profile_service.application.port.in.registration;

import com.nongthinh.profile_service.application.command.CompleteRegistrationProfileCommand;

public interface CompleteRegistrationProfileUseCase {
    void execute(CompleteRegistrationProfileCommand command);
}
