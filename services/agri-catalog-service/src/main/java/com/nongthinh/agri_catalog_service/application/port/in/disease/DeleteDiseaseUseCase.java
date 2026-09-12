package com.nongthinh.agri_catalog_service.application.port.in.disease;

import java.util.UUID;

public interface DeleteDiseaseUseCase {

    void execute(UUID id);
}
