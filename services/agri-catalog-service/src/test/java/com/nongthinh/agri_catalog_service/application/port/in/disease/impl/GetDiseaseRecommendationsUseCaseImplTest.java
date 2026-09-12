package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;

class GetDiseaseRecommendationsUseCaseImplTest {

    private static final UUID DISEASE_ID = UUID.fromString("44000000-0000-0000-0000-000000000001");
    private static final UUID CROP_ID = UUID.fromString("44000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("44000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    private final DiseaseRepository diseaseRepository = mock(DiseaseRepository.class);
    private final ProductDiseaseTreatmentRepository treatmentRepository = mock(ProductDiseaseTreatmentRepository.class);
    private final GetDiseaseRecommendationsUseCaseImpl useCase = new GetDiseaseRecommendationsUseCaseImpl(
            diseaseRepository, treatmentRepository);

    @Test
    void capsRecommendationsAtThreeAndReturnsAnEmptyArrayWhenNoneExist() {
        when(diseaseRepository.findById(DISEASE_ID)).thenReturn(Optional.of(approvedDisease()));
        when(treatmentRepository.findPublicRecommendationsByDiseaseId(DISEASE_ID, 3)).thenReturn(List.of());

        var result = useCase.execute(DISEASE_ID, 100);

        assertTrue(result.isEmpty());
        verify(treatmentRepository).findPublicRecommendationsByDiseaseId(eq(DISEASE_ID), eq(3));
    }

    @Test
    void usesRequestedLimitWhenItIsBelowTheMaximum() {
        when(diseaseRepository.findById(DISEASE_ID)).thenReturn(Optional.of(approvedDisease()));
        when(treatmentRepository.findPublicRecommendationsByDiseaseId(DISEASE_ID, 2)).thenReturn(List.of());

        assertEquals(List.of(), useCase.execute(DISEASE_ID, 2));
        verify(treatmentRepository).findPublicRecommendationsByDiseaseId(DISEASE_ID, 2);
    }

    private Disease approvedDisease() {
        return Disease.create(
                DISEASE_ID, CreatedSource.ADMIN, null, "Brown spot", "brown-spot", null, CROP_ID,
                null, null, null, null, null, null, null, null, null, null, ADMIN_ID, NOW);
    }
}
