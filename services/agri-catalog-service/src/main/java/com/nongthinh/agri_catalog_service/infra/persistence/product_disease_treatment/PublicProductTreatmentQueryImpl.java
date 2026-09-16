package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.PublicProductTreatmentQuery;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.application.view.PublicProductTreatmentView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PublicProductTreatmentQueryImpl implements PublicProductTreatmentQuery {
    private final EntityManager entityManager;
    private final ProductDiseaseTreatmentPersistenceMapper mapper;

    @Override
    public List<PublicProductTreatmentView> findByProductId(UUID productId) {
        return entityManager.createQuery("""
                SELECT t FROM JpaProductDiseaseTreatmentEntity t
                JOIN FETCH t.disease d JOIN t.product p
                WHERE t.productId = :productId AND t.deletedAt IS NULL
                  AND d.deletedAt IS NULL AND d.reviewStatus = 'APPROVED'
                  AND p.deletedAt IS NULL AND p.publicationStatus = 'PUBLISHED' AND p.moderationStatus = 'NORMAL'
                ORDER BY t.priority ASC, t.id ASC
                """, JpaProductDiseaseTreatmentEntity.class)
                .setParameter("productId", productId).getResultList().stream()
                .map(item -> new PublicProductTreatmentView(item.getDisease().getName(),
                        ProductDiseaseTreatmentView.from(mapper.toDomain(item)))).toList();
    }
}
