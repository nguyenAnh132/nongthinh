package com.nongthinh.agri_catalog_service.application.port.out;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.PublicProductTreatmentView;

public interface PublicProductTreatmentQuery {
    List<PublicProductTreatmentView> findByProductId(UUID productId);
}
