package com.nongthinh.auth_service.application.port.in.user;

import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;
import com.nongthinh.auth_service.application.view.CompleteRegistrationView;
import com.nongthinh.auth_service.application.view.RegistrationPrincipal;

public interface CompleteRegistrationUseCase {
    CompleteRegistrationView execute(RegistrationPrincipal principal, CompleteRegistrationCommand command);
}
