package com.nongthinh.agri_catalog_service.domain.disease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

class DiseaseTest {

    private static final Instant NOW = Instant.parse("2026-07-26T00:00:00Z");
    private static final UUID ACTOR_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID BRAND_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000002"
    );
    private static final UUID CROP_TYPE_ID = UUID.fromString(
            "30000000-0000-0000-0000-000000000003"
    );

    @Test
    void adminDiseaseIsApprovedAndPublishedOnCreation() {
        Disease disease = createDisease(CreatedSource.ADMIN, null);

        assertEquals(ReviewStatus.APPROVED, disease.getReviewStatus());
        assertEquals(NOW, disease.getPublishedAt());
        assertNull(disease.getBrandId());
    }

    @Test
    void brandDiseaseFollowsReviewLifecycle() {
        Disease disease = createDisease(CreatedSource.BRAND, BRAND_ID);
        Instant submittedAt = NOW.plusSeconds(60);
        Instant reviewedAt = NOW.plusSeconds(120);

        assertEquals(ReviewStatus.DRAFT, disease.getReviewStatus());
        assertNull(disease.getPublishedAt());

        disease.submitForReview(BRAND_ID, submittedAt);
        assertEquals(ReviewStatus.PENDING_REVIEW, disease.getReviewStatus());
        assertEquals(submittedAt, disease.getSubmittedAt());

        disease.approve(ACTOR_ID, reviewedAt);
        assertEquals(ReviewStatus.APPROVED, disease.getReviewStatus());
        assertEquals(reviewedAt, disease.getPublishedAt());
        assertEquals(ACTOR_ID, disease.getReviewedBy());
        assertNotNull(disease.getReviewedAt());
    }

    @Test
    void invalidReviewTransitionsAreRejectedByDomain() {
        Disease disease = createDisease(CreatedSource.BRAND, BRAND_ID);

        assertThrows(
                IllegalStateException.class,
                () -> disease.approve(ACTOR_ID, NOW.plusSeconds(10))
        );
        assertThrows(
                IllegalStateException.class,
                () -> disease.hide(ACTOR_ID, NOW.plusSeconds(10))
        );
    }

    @Test
    void rejectionReasonIsRequired() {
        Disease disease = createDisease(CreatedSource.BRAND, BRAND_ID);
        disease.submitForReview(BRAND_ID, NOW.plusSeconds(10));

        assertThrows(
                IllegalArgumentException.class,
                () -> disease.reject(" ", ACTOR_ID, NOW.plusSeconds(20))
        );
    }

    @Test
    void hiddenDiseaseCanBeRestoredToApprovedAndPublished() {
        Disease disease = createDisease(CreatedSource.ADMIN, null);
        Instant hiddenAt = NOW.plusSeconds(60);
        Instant restoredAt = NOW.plusSeconds(120);

        disease.hide(ACTOR_ID, hiddenAt);
        assertEquals(ReviewStatus.HIDDEN, disease.getReviewStatus());
        assertNull(disease.getPublishedAt());

        disease.restorePublication(ACTOR_ID, restoredAt);

        assertEquals(ReviewStatus.APPROVED, disease.getReviewStatus());
        assertEquals(restoredAt, disease.getPublishedAt());
        assertEquals(restoredAt, disease.getUpdatedAt());
        assertEquals(ACTOR_ID, disease.getUpdatedBy());
    }

    @Test
    void onlyHiddenDiseaseCanBeRestoredToPublication() {
        Disease disease = createDisease(CreatedSource.ADMIN, null);

        assertThrows(
                IllegalStateException.class,
                () -> disease.restorePublication(ACTOR_ID, NOW.plusSeconds(60))
        );
    }

    private Disease createDisease(CreatedSource source, UUID brandId) {
        return Disease.create(
                UUID.fromString("40000000-0000-0000-0000-000000000004"),
                source,
                brandId,
                "Rice blast",
                "rice-blast",
                "Magnaporthe oryzae",
                CROP_TYPE_ID,
                "LEAF",
                "FUNGUS",
                "Short description",
                "Description",
                "Symptoms",
                "Causes",
                "Favorable conditions",
                "Prevention",
                "Treatment",
                null,
                ACTOR_ID,
                NOW
        );
    }
}
