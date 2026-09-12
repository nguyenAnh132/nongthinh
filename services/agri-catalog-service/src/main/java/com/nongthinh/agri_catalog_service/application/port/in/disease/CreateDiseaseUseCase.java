package com.nongthinh.agri_catalog_service.application.port.in.disease;

import com.nongthinh.agri_catalog_service.application.command.DiseaseCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;

public interface CreateDiseaseUseCase {

    DiseaseView execute(DiseaseCreationCommand command);
}
