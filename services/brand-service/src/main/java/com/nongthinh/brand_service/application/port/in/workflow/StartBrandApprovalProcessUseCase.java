package com.nongthinh.brand_service.application.port.in.workflow;

import com.nongthinh.brand_service.application.command.StartBrandApprovalProcessCommand;

public interface StartBrandApprovalProcessUseCase {

    void execute(StartBrandApprovalProcessCommand command);
}
