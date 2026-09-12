package com.nongthinh.bo_portal_service.application.port.in.systemparamtype;

import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;

public interface GetSystemParamTypeUseCase {

    SystemParamTypeView execute(Long id);
}
