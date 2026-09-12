package com.nongthinh.bo_portal_service.application.port.in.systemparamtype;

import com.nongthinh.bo_portal_service.application.view.SystemParamTypeGroupView;
import java.util.List;

public interface ListSystemParamsGroupedByTypeUseCase {

    List<SystemParamTypeGroupView> execute();
}
