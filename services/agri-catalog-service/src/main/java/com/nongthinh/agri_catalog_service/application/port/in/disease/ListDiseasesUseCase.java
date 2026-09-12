package com.nongthinh.agri_catalog_service.application.port.in.disease;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public interface ListDiseasesUseCase {

    List<DiseaseView> execute(
            UUID brandId,
            ReviewStatus reviewStatus,
            UUID cropTypeId,
            boolean publicOnly
    );
}
