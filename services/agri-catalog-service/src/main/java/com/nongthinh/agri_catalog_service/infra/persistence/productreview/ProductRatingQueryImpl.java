package com.nongthinh.agri_catalog_service.infra.persistence.productreview;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRatingQueryImpl implements ProductRatingQuery {

    private final JpaProductReviewRepository repository;

    @Override
    public Map<UUID, ProductRatingSummary> summarizeVisibleReviews(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return repository.summarizeVisibleReviews(productIds).stream()
                .map(summary -> new ProductRatingSummary(
                        summary.getProductId(),
                        summary.getAverageRating(),
                        summary.getReviewCount()
                ))
                .collect(Collectors.toUnmodifiableMap(
                        ProductRatingSummary::productId,
                        Function.identity()
                ));
    }
}
