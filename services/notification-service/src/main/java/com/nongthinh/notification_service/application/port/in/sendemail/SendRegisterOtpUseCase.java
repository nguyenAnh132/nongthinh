package com.nongthinh.notification_service.application.port.in.sendemail;

import com.nongthinh.notification_service.application.command.SendRegisterOtpCommand;

public interface SendRegisterOtpUseCase {

    void execute(SendRegisterOtpCommand command);
}
