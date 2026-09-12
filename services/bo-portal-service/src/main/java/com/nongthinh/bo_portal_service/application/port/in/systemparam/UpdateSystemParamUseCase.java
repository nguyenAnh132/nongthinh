package com.nongthinh.bo_portal_service.application.port.in.systemparam;

import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamCommand;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;

public interface UpdateSystemParamUseCase {
    SystemParamView execute(String name, UpdateSystemParamCommand command);
}
