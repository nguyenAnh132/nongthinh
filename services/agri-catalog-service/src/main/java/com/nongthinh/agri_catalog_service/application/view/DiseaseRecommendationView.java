package com.nongthinh.agri_catalog_service.application.view;

import com.nongthinh.agri_catalog_service.application.model.PublicDiseaseProductRecommendation;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;

public record DiseaseRecommendationView(
        ProductSummaryView product,
        ProductDiseaseTreatmentView treatment,
        double averageRating,
        long reviewCount
) {
    public static DiseaseRecommendationView from(PublicDiseaseProductRecommendation recommendation) {
        return from(recommendation, ProductRatingSummary.unrated(recommendation.product().getId()));
    }

    public static DiseaseRecommendationView from(PublicDiseaseProductRecommendation recommendation,
            ProductRatingSummary rating) {
        return new DiseaseRecommendationView(
                ProductSummaryView.from(recommendation.product()),
                ProductDiseaseTreatmentView.from(recommendation.treatment()),
                rating.averageRating(), rating.reviewCount());
    }
}
