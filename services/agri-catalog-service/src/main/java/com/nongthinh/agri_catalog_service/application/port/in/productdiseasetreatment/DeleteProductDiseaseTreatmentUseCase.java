package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment;

import java.util.UUID;

public interface DeleteProductDiseaseTreatmentUseCase {

    void execute(UUID productId, UUID treatmentId);
}
