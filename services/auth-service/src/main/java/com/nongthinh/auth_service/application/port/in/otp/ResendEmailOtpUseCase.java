package com.nongthinh.auth_service.application.port.in.otp;

import com.nongthinh.auth_service.application.command.ResendEmailOtpCommand;
import com.nongthinh.auth_service.application.view.OtpResendCooldownView;

public interface ResendEmailOtpUseCase {

    OtpResendCooldownView execute(ResendEmailOtpCommand command);
}
