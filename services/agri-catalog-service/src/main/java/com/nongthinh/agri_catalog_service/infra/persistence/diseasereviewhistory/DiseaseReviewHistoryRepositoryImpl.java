package com.nongthinh.agri_catalog_service.infra.persistence.diseasereviewhistory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.domain.disease.DiseaseReviewHistory;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DiseaseReviewHistoryRepositoryImpl implements DiseaseReviewHistoryRepository {

    private final JpaDiseaseReviewHistoryRepository jpaRepository;
    private final DiseaseReviewHistoryPersistenceMapper mapper;
    private final EntityManager entityManager;

    @Override
    public DiseaseReviewHistory save(DiseaseReviewHistory history) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(history)));
    }

    @Override
    public List<DiseaseReviewHistory> findAllByDiseaseIdOrderByCreatedAtDesc(UUID diseaseId) {
        return jpaRepository.findAllByDiseaseIdOrderByCreatedAtDesc(diseaseId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PageView<DiseaseReviewHistoryListItemView> search(
            DiseaseReviewHistoryQuery query
    ) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> itemCriteria = builder.createTupleQuery();
        Root<JpaDiseaseReviewHistoryEntity> history = itemCriteria.from(
                JpaDiseaseReviewHistoryEntity.class
        );
        Join<JpaDiseaseReviewHistoryEntity, JpaDiseaseEntity> disease =
                history.join("disease");

        itemCriteria.multiselect(
                history.get("id").alias("id"),
                history.get("diseaseId").alias("diseaseId"),
                disease.get("name").alias("diseaseName"),
                disease.get("cropTypeId").alias("cropTypeId"),
                disease.get("createdSource").alias("createdSource"),
                disease.get("brandId").alias("brandId"),
                history.get("action").alias("action"),
                history.get("previousStatus").alias("previousStatus"),
                history.get("newStatus").alias("newStatus"),
                history.get("comment").alias("comment"),
                history.get("actorId").alias("actorId"),
                history.get("actorType").alias("actorType"),
                history.get("createdAt").alias("createdAt")
        );
        itemCriteria.where(predicates(builder, history, disease, query));
        itemCriteria.orderBy(
                builder.desc(history.get("createdAt")),
                builder.desc(history.get("id"))
        );

        TypedQuery<Tuple> itemQuery = entityManager.createQuery(itemCriteria);
        itemQuery.setFirstResult(query.page() * query.size());
        itemQuery.setMaxResults(query.size());
        List<DiseaseReviewHistoryListItemView> items = itemQuery.getResultList()
                .stream()
                .map(DiseaseReviewHistoryRepositoryImpl::toView)
                .toList();

        CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
        Root<JpaDiseaseReviewHistoryEntity> countHistory = countCriteria.from(
                JpaDiseaseReviewHistoryEntity.class
        );
        Join<JpaDiseaseReviewHistoryEntity, JpaDiseaseEntity> countDisease =
                countHistory.join("disease");
        countCriteria.select(builder.count(countHistory));
        countCriteria.where(
                predicates(builder, countHistory, countDisease, query)
        );
        long totalElements = entityManager.createQuery(countCriteria).getSingleResult();
        int totalPages = totalElements == 0
                ? 0
                : (int) ((totalElements + query.size() - 1) / query.size());

        return new PageView<>(
                items,
                query.page(),
                query.size(),
                totalElements,
                totalPages,
                query.page() + 1 < totalPages
        );
    }

    private static Predicate[] predicates(
            CriteriaBuilder builder,
            Root<JpaDiseaseReviewHistoryEntity> history,
            Join<JpaDiseaseReviewHistoryEntity, JpaDiseaseEntity> disease,
            DiseaseReviewHistoryQuery query
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
                builder.equal(
                        disease.get("createdSource"),
                        query.createdSource().getValue()
                )
        );
        if (query.diseaseId() != null) {
            predicates.add(builder.equal(history.get("diseaseId"), query.diseaseId()));
        }
        if (query.brandId() != null) {
            predicates.add(builder.equal(disease.get("brandId"), query.brandId()));
        }
        if (query.action() != null) {
            predicates.add(
                    builder.equal(history.get("action"), query.action().getValue())
            );
        }
        if (query.newStatus() != null) {
            predicates.add(
                    builder.equal(history.get("newStatus"), query.newStatus().getValue())
            );
        }
        if (query.actorType() != null) {
            predicates.add(
                    builder.equal(history.get("actorType"), query.actorType().getValue())
            );
        }
        if (query.actorId() != null) {
            predicates.add(builder.equal(history.get("actorId"), query.actorId()));
        }
        if (query.keyword() != null) {
            String pattern = "%" + query.keyword().toLowerCase(Locale.ROOT) + "%";
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(disease.get("name")), pattern),
                            builder.like(builder.lower(disease.get("slug")), pattern)
                    )
            );
        }
        if (query.from() != null) {
            predicates.add(
                    builder.greaterThanOrEqualTo(
                            history.get("createdAt"),
                            query.from()
                    )
            );
        }
        if (query.to() != null) {
            predicates.add(builder.lessThan(history.get("createdAt"), query.to()));
        }
        return predicates.toArray(Predicate[]::new);
    }

    private static DiseaseReviewHistoryListItemView toView(Tuple tuple) {
        String previousStatus = tuple.get("previousStatus", String.class);
        return new DiseaseReviewHistoryListItemView(
                tuple.get("id", UUID.class),
                tuple.get("diseaseId", UUID.class),
                tuple.get("diseaseName", String.class),
                tuple.get("cropTypeId", UUID.class),
                CreatedSource.fromString(tuple.get("createdSource", String.class)),
                tuple.get("brandId", UUID.class),
                DiseaseReviewAction.fromString(tuple.get("action", String.class)),
                previousStatus == null ? null : ReviewStatus.fromString(previousStatus),
                ReviewStatus.fromString(tuple.get("newStatus", String.class)),
                tuple.get("comment", String.class),
                tuple.get("actorId", UUID.class),
                ReviewActorType.fromString(tuple.get("actorType", String.class)),
                tuple.get("createdAt", Instant.class)
        );
    }
}
