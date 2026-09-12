package com.nongthinh.agri_catalog_service.infra.persistence.diseasereviewhistory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Transactional
class DiseaseReviewHistoryRepositoryImplTest {

    private static final Instant NOW = Instant.parse("2026-07-26T10:00:00Z");
    private static final UUID BRAND_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_DISEASE_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_DISEASE_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID CROP_TYPE_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DiseaseReviewHistoryRepository repository;

    @BeforeEach
    void setUp() {
        persistDisease(
                BRAND_DISEASE_ID,
                "BRAND",
                BRAND_ID,
                "Rice Blast",
                "rice-blast",
                NOW.minusSeconds(60)
        );
        persistDisease(
                ADMIN_DISEASE_ID,
                "ADMIN",
                null,
                "Rice Blast Admin",
                "rice-blast-admin",
                null
        );
        persistHistory(
                UUID.fromString("40000000-0000-0000-0000-000000000001"),
                BRAND_DISEASE_ID,
                "APPROVED",
                "PENDING_REVIEW",
                "APPROVED",
                "Approved",
                "ADMIN",
                NOW
        );
        persistHistory(
                UUID.fromString("40000000-0000-0000-0000-000000000002"),
                BRAND_DISEASE_ID,
                "REJECTED",
                "PENDING_REVIEW",
                "REJECTED",
                "Needs detail",
                "ADMIN",
                NOW
        );
        persistHistory(
                UUID.fromString("40000000-0000-0000-0000-000000000003"),
                ADMIN_DISEASE_ID,
                "APPROVED",
                "PENDING_REVIEW",
                "APPROVED",
                null,
                "ADMIN",
                NOW.plusSeconds(60)
        );
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void appliesAllFiltersAndIncludesSoftDeletedDisease() {
        DiseaseReviewHistoryQuery query = new DiseaseReviewHistoryQuery(
                CreatedSource.BRAND,
                BRAND_DISEASE_ID,
                BRAND_ID,
                DiseaseReviewAction.APPROVED,
                ReviewStatus.APPROVED,
                ReviewActorType.ADMIN,
                ACTOR_ID,
                "rIcE bLaSt",
                NOW,
                NOW.plusSeconds(1),
                0,
                20
        );

        PageView<DiseaseReviewHistoryListItemView> page = repository.search(query);

        assertEquals(1, page.totalElements());
        assertEquals(1, page.totalPages());
        assertFalse(page.hasNext());
        DiseaseReviewHistoryListItemView item = page.items().getFirst();
        assertEquals(BRAND_DISEASE_ID, item.diseaseId());
        assertEquals("Rice Blast", item.diseaseName());
        assertEquals(CROP_TYPE_ID, item.cropTypeId());
        assertEquals(CreatedSource.BRAND, item.createdSource());
        assertEquals(BRAND_ID, item.brandId());
    }

    @Test
    void usesStableSortPaginatesAndExecutesOnlyItemAndCountQueries() {
        Statistics statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.clear();

        PageView<DiseaseReviewHistoryListItemView> firstPage =
                repository.search(new DiseaseReviewHistoryQuery(
                        CreatedSource.BRAND,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        1
                ));

        assertEquals(2, firstPage.totalElements());
        assertEquals(2, firstPage.totalPages());
        assertTrue(firstPage.hasNext());
        assertEquals(
                UUID.fromString("40000000-0000-0000-0000-000000000002"),
                firstPage.items().getFirst().id()
        );
        assertEquals(2, statistics.getPrepareStatementCount());
    }

    private void persistDisease(
            UUID id,
            String source,
            UUID brandId,
            String name,
            String slug,
            Instant deletedAt
    ) {
        entityManager.persist(JpaDiseaseEntity.builder()
                .id(id)
                .createdSource(source)
                .brandId(brandId)
                .name(name)
                .slug(slug)
                .cropTypeId(CROP_TYPE_ID)
                .reviewStatus("APPROVED")
                .createdAt(NOW.minusSeconds(600))
                .createdBy(ACTOR_ID)
                .deletedAt(deletedAt)
                .build());
    }

    private void persistHistory(
            UUID id,
            UUID diseaseId,
            String action,
            String previousStatus,
            String newStatus,
            String comment,
            String actorType,
            Instant createdAt
    ) {
        entityManager.persist(JpaDiseaseReviewHistoryEntity.builder()
                .id(id)
                .diseaseId(diseaseId)
                .action(action)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .comment(comment)
                .actorId(ACTOR_ID)
                .actorType(actorType)
                .createdAt(createdAt)
                .build());
    }
}
