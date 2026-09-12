package com.nongthinh.bo_portal_service.application.port.in.systemparam;

import com.nongthinh.bo_portal_service.application.view.SystemParamValueView;

public interface GetSystemParamValueUseCase {
    SystemParamValueView execute(String name);
}
