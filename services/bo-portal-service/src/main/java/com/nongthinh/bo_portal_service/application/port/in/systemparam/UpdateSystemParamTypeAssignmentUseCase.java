package com.nongthinh.bo_portal_service.application.port.in.systemparam;

import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamTypeAssignmentCommand;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;

public interface UpdateSystemParamTypeAssignmentUseCase {

    SystemParamView execute(String name, UpdateSystemParamTypeAssignmentCommand command);
}
