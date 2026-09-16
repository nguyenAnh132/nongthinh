package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;

class DeleteDiseaseUseCaseImplTest {

    private static final UUID DISEASE_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-14T03:00:00Z");

    private final DiseaseUseCaseSupport support = mock(DiseaseUseCaseSupport.class);
    private final DiseaseRepository repository = mock(DiseaseRepository.class);
    private final ProductDiseaseTreatmentRepository treatmentRepository =
            mock(ProductDiseaseTreatmentRepository.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final DeleteDiseaseUseCaseImpl useCase = new DeleteDiseaseUseCaseImpl(
            support,
            repository,
            treatmentRepository,
            clockProvider,
            currentUserProvider
    );

    @Test
    void brandCanDeleteApprovedDisease() {
        Disease disease = approvedBrandDisease();
        CurrentUser brand = new CurrentUser(
                BRAND_ID,
                "brand-keycloak-id",
                "brand@nongthinh.vn",
                Set.of("ROLE_BRAND"),
                null,
                Set.of()
        );
        when(support.requireAccessibleDisease(DISEASE_ID)).thenReturn(disease);
        when(currentUserProvider.getCurrentUser()).thenReturn(brand);
        when(clockProvider.now()).thenReturn(NOW);
        when(treatmentRepository.findAllByDiseaseIdOrderByPriority(DISEASE_ID))
                .thenReturn(List.of());
        when(repository.save(disease)).thenReturn(disease);

        useCase.execute(DISEASE_ID);

        assertTrue(disease.isDeleted());
        assertEquals(BRAND_ID, disease.getDeletedBy());
        assertEquals(NOW, disease.getDeletedAt());
        verify(repository).save(disease);
    }

    private Disease approvedBrandDisease() {
        Instant createdAt = NOW.minusSeconds(3600);
        Disease disease = Disease.create(
                DISEASE_ID,
                CreatedSource.BRAND,
                BRAND_ID,
                "Đạo ôn lúa",
                "dao-on-lua",
                "Magnaporthe oryzae",
                CROP_TYPE_ID,
                "Lá",
                "Nấm",
                "Mô tả",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                BRAND_ID,
                createdAt
        );
        disease.submitForReview(BRAND_ID, createdAt.plusSeconds(60));
        disease.approve(ADMIN_ID, createdAt.plusSeconds(120));
        return disease;
    }
}
