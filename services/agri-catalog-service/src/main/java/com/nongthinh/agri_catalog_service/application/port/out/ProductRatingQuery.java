package com.nongthinh.agri_catalog_service.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;

public interface ProductRatingQuery {

    Map<UUID, ProductRatingSummary> summarizeVisibleReviews(Collection<UUID> productIds);
}
