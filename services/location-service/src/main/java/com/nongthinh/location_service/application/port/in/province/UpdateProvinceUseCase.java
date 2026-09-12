package com.nongthinh.location_service.application.port.in.province;

import com.nongthinh.location_service.application.command.ProvinceUpdateCommand;
import com.nongthinh.location_service.application.view.ProvinceView;

public interface UpdateProvinceUseCase {

    ProvinceView execute(String id, ProvinceUpdateCommand command);
}
