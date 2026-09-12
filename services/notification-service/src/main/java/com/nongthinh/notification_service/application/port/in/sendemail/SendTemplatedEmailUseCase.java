package com.nongthinh.notification_service.application.port.in.sendemail;

import com.nongthinh.notification_service.application.command.SendTemplatedEmailCommand;

public interface SendTemplatedEmailUseCase {

    void execute(SendTemplatedEmailCommand command);
}
