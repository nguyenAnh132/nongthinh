package com.nongthinh.agri_catalog_service.application.view;

import com.nongthinh.agri_catalog_service.application.model.PublicDiseaseProductRecommendation;

public record DiseaseRecommendationView(
        ProductSummaryView product,
        ProductDiseaseTreatmentView treatment
) {
    public static DiseaseRecommendationView from(PublicDiseaseProductRecommendation recommendation) {
        return new DiseaseRecommendationView(
                ProductSummaryView.from(recommendation.product()),
                ProductDiseaseTreatmentView.from(recommendation.treatment()));
    }
}
