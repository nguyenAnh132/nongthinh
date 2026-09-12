package com.nongthinh.location_service.application.port.in.commune;

import java.util.List;
import com.nongthinh.location_service.application.view.CommuneView;

public interface ListCommunesByProvinceIdUseCase {

    List<CommuneView> execute(String provinceId);
}
