package com.nongthinh.agri_catalog_service.application.port.in.disease;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.DiseaseRecommendationView;

public interface GetDiseaseRecommendationsUseCase {
    List<DiseaseRecommendationView> execute(UUID diseaseId, int limit);
}
