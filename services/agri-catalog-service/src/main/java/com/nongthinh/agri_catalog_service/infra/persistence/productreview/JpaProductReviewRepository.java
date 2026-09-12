package com.nongthinh.agri_catalog_service.infra.persistence.productreview;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductReviewRepository extends JpaRepository<JpaProductReviewEntity, UUID> {

    @Query("""
            select review.productId as productId,
                   avg(review.rating) as averageRating,
                   count(review.id) as reviewCount
            from JpaProductReviewEntity review
            where review.productId in :productIds
              and review.status = 'VISIBLE'
              and review.deletedAt is null
            group by review.productId
            """)
    List<JpaProductRatingSummary> summarizeVisibleReviews(
            @Param("productIds") Collection<UUID> productIds
    );
}
