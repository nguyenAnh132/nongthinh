package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.application.model.PublicDiseaseProductRecommendation;

public interface ProductDiseaseTreatmentRepository {

    boolean existsByProductIdAndDiseaseId(UUID productId, UUID diseaseId);

    Optional<ProductDiseaseTreatment> findById(UUID id);

    List<ProductDiseaseTreatment> findAllByProductIdOrderByPriority(UUID productId);

    List<ProductDiseaseTreatment> findAllByDiseaseIdOrderByPriority(UUID diseaseId);

    List<ProductDiseaseTreatment> findAllPublicByProductIdOrderByPriority(UUID productId);

    List<ProductDiseaseTreatment> findAllPublicByDiseaseIdOrderByPriority(UUID diseaseId);

    List<PublicDiseaseProductRecommendation> findPublicRecommendationsByDiseaseId(
            UUID diseaseId, int limit);

    ProductDiseaseTreatment save(ProductDiseaseTreatment productDiseaseTreatment);
}
