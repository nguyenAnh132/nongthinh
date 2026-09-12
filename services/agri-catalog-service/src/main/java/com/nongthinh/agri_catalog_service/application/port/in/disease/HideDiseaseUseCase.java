package com.nongthinh.agri_catalog_service.application.port.in.disease;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;

public interface HideDiseaseUseCase {

    DiseaseView execute(UUID id);
}
