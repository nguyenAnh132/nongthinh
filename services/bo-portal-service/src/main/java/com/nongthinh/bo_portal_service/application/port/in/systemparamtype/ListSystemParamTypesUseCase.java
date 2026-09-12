package com.nongthinh.bo_portal_service.application.port.in.systemparamtype;

import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;
import java.util.List;

public interface ListSystemParamTypesUseCase {

    List<SystemParamTypeView> execute();
}
