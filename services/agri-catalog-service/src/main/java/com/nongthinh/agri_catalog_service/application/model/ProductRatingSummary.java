package com.nongthinh.agri_catalog_service.application.model;

import java.util.UUID;

public record ProductRatingSummary(
        UUID productId,
        double averageRating,
        long reviewCount
) {
    public static ProductRatingSummary unrated(UUID productId) {
        return new ProductRatingSummary(productId, 0.0d, 0L);
    }
}
