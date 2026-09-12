package com.nongthinh.location_service.application.port.in.province;

import com.nongthinh.location_service.application.view.ProvinceView;

public interface GetProvinceByIdUseCase {

    ProvinceView execute(String id);
}
