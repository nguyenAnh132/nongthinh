package com.nongthinh.brand_service.application.port.in.ticket;

import com.nongthinh.brand_service.application.command.CompletePhoneVerificationCommand;

public interface CompletePhoneVerificationUseCase {

    void execute(String taskId, CompletePhoneVerificationCommand command);
}
