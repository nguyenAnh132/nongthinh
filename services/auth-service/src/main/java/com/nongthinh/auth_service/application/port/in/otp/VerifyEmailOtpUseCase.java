package com.nongthinh.auth_service.application.port.in.otp;

import com.nongthinh.auth_service.application.command.VerifyEmailOtpCommand;

public interface VerifyEmailOtpUseCase {

    void execute(VerifyEmailOtpCommand command);
}
