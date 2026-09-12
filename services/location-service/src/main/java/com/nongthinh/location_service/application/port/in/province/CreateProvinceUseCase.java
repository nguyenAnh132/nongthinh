package com.nongthinh.location_service.application.port.in.province;

import com.nongthinh.location_service.application.command.ProvinceCreationCommand;
import com.nongthinh.location_service.application.view.ProvinceView;

public interface CreateProvinceUseCase {

    ProvinceView execute(ProvinceCreationCommand command);
}
