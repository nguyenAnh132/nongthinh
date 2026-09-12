package com.nongthinh.bo_portal_service.application.port.in.systemparamtype;

import com.nongthinh.bo_portal_service.application.command.CreateSystemParamTypeCommand;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;

public interface CreateSystemParamTypeUseCase {

    SystemParamTypeView execute(CreateSystemParamTypeCommand command);
}
