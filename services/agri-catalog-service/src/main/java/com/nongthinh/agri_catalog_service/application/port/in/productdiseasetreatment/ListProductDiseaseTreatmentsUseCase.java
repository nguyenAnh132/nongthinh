package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;

public interface ListProductDiseaseTreatmentsUseCase {

    List<ProductDiseaseTreatmentView> execute(UUID productId, boolean publicOnly);
}
