package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;

public interface GetProductDiseaseTreatmentUseCase {

    ProductDiseaseTreatmentView execute(UUID productId, UUID treatmentId);
}
