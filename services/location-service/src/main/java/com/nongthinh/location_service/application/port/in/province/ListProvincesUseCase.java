package com.nongthinh.location_service.application.port.in.province;

import java.util.List;
import com.nongthinh.location_service.application.view.ProvinceView;

public interface ListProvincesUseCase {

    List<ProvinceView> execute();
}
