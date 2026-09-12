package com.nongthinh.bo_portal_service.application.port.in.systemparam;

import com.nongthinh.bo_portal_service.application.view.SystemParamView;

public interface GetSystemParamUseCase {

    SystemParamView execute(String name);
}
