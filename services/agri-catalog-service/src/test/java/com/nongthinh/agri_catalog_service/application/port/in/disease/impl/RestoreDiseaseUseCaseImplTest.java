package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

class RestoreDiseaseUseCaseImplTest {

    private static final UUID DISEASE_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-07-26T00:00:00Z");
    private static final Instant RESTORED_AT =
            Instant.parse("2026-07-27T00:00:00Z");

    private final DiseaseUseCaseSupport support = mock(DiseaseUseCaseSupport.class);
    private final DiseaseRepository repository = mock(DiseaseRepository.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final CurrentUserProvider currentUserProvider =
            mock(CurrentUserProvider.class);
    private final RestoreDiseaseUseCaseImpl useCase =
            new RestoreDiseaseUseCaseImpl(
                    support,
                    repository,
                    clockProvider,
                    currentUserProvider
            );

    @Test
    void restoresHiddenDiseaseAndRecordsAuditHistory() {
        Disease disease = hiddenDisease();
        CurrentUser admin = new CurrentUser(
                ADMIN_ID,
                "admin-keycloak-id",
                "admin@nongthinh.vn",
                Set.of("ROLE_ADMIN"),
                null,
                Set.of()
        );
        when(support.requireDisease(DISEASE_ID)).thenReturn(disease);
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(clockProvider.now()).thenReturn(RESTORED_AT);
        when(repository.save(disease)).thenReturn(disease);

        var result = useCase.execute(DISEASE_ID);

        assertEquals(ReviewStatus.APPROVED, result.reviewStatus());
        assertEquals(RESTORED_AT, result.publishedAt());
        verify(support).requireStatus(disease, ReviewStatus.HIDDEN);
        verify(repository).save(disease);
        verify(support).recordHistory(
                disease,
                DiseaseReviewAction.RESTORED,
                ReviewStatus.HIDDEN,
                null,
                ADMIN_ID,
                ReviewActorType.ADMIN,
                RESTORED_AT
        );
    }

    private Disease hiddenDisease() {
        Disease disease = Disease.create(
                DISEASE_ID,
                CreatedSource.ADMIN,
                null,
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
                ADMIN_ID,
                CREATED_AT
        );
        disease.hide(ADMIN_ID, CREATED_AT.plusSeconds(60));
        return disease;
    }
}
