package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductReview;

public interface ProductReviewRepository {

    boolean existsByProductIdAndFarmerId(UUID productId, UUID farmerId);

    Optional<ProductReview> findById(UUID id);

    Optional<ProductReview> findByProductIdAndFarmerId(UUID productId, UUID farmerId);

    List<ProductReview> findAllByProductIdOrderByCreatedAtDesc(UUID productId);

    List<ProductReview> findAllVisibleByProductIdOrderByCreatedAtDesc(UUID productId);

    ProductReview save(ProductReview productReview);
}
