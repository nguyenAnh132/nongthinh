package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import com.nongthinh.agri_catalog_service.application.model.PublicDiseaseProductRecommendation;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.infra.persistence.product.ProductPersistenceMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductDiseaseTreatmentRepositoryImpl
        implements ProductDiseaseTreatmentRepository {

    private final JpaProductDiseaseTreatmentRepository jpaRepository;
    private final ProductDiseaseTreatmentPersistenceMapper mapper;
    private final ProductPersistenceMapper productMapper;

    @Override
    public boolean existsByProductIdAndDiseaseId(UUID productId, UUID diseaseId) {
        return jpaRepository.existsByProductIdAndDiseaseIdAndDeletedAtIsNull(
                productId,
                diseaseId
        );
    }

    @Override
    public Optional<ProductDiseaseTreatment> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProductDiseaseTreatment> findAllByProductIdOrderByPriority(UUID productId) {
        return toDomains(
                jpaRepository
                        .findAllByProductIdAndDeletedAtIsNullOrderByPriorityAscCreatedAtAsc(
                                productId
                        )
        );
    }

    @Override
    public List<ProductDiseaseTreatment> findAllByDiseaseIdOrderByPriority(UUID diseaseId) {
        return toDomains(
                jpaRepository
                        .findAllByDiseaseIdAndDeletedAtIsNullOrderByPriorityAscCreatedAtAsc(
                                diseaseId
                        )
        );
    }

    @Override
    public List<ProductDiseaseTreatment> findAllPublicByProductIdOrderByPriority(UUID productId) {
        return toDomains(jpaRepository.findAllPublicByProductId(productId));
    }

    @Override
    public List<ProductDiseaseTreatment> findAllPublicByDiseaseIdOrderByPriority(UUID diseaseId) {
        return toDomains(jpaRepository.findAllPublicByDiseaseId(diseaseId));
    }

    @Override
    public List<PublicDiseaseProductRecommendation> findPublicRecommendationsByDiseaseId(
            UUID diseaseId, int limit) {
        return jpaRepository.findPublicRecommendationsByDiseaseId(diseaseId, PageRequest.of(0, limit)).stream()
                .map(item -> new PublicDiseaseProductRecommendation(
                        productMapper.toDomain(item.getProduct()), mapper.toDomain(item)))
                .toList();
    }

    @Override
    public ProductDiseaseTreatment save(ProductDiseaseTreatment treatment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(treatment)));
    }

    private List<ProductDiseaseTreatment> toDomains(
            List<JpaProductDiseaseTreatmentEntity> entities
    ) {
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }
}
