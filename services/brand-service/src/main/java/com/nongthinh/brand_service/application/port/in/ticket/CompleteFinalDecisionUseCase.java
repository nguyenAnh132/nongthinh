package com.nongthinh.brand_service.application.port.in.ticket;

import com.nongthinh.brand_service.application.command.CompleteFinalDecisionCommand;

public interface CompleteFinalDecisionUseCase {

    void execute(String taskId, CompleteFinalDecisionCommand command);
}
