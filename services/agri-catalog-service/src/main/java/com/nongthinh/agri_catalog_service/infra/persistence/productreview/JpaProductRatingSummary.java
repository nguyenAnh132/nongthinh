package com.nongthinh.agri_catalog_service.infra.persistence.productreview;

import java.util.UUID;

public interface JpaProductRatingSummary {

    UUID getProductId();

    Double getAverageRating();

    long getReviewCount();
}
