package com.nongthinh.bo_portal_service.application.port.in.systemparam;

import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import java.util.List;

public interface ListSystemParamsUseCase {

    List<SystemParamView> execute();
}
