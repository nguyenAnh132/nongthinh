package com.nongthinh.agri_catalog_service.application.port.in.disease;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.DiseaseRecommendationView;
import com.nongthinh.agri_catalog_service.application.view.PageView;

public interface ListDiseaseRecommendationsUseCase {
    PageView<DiseaseRecommendationView> execute(UUID diseaseId, int page);
}
