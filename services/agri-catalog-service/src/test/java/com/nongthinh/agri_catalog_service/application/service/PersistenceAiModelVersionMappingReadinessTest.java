package com.nongthinh.agri_catalog_service.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;

class PersistenceAiModelVersionMappingReadinessTest {

    private static final UUID MODEL_ID = UUID.fromString("43000000-0000-0000-0000-000000000001");
    private static final UUID VERSION_ID = UUID.fromString("43000000-0000-0000-0000-000000000002");
    private static final UUID CLASS_ID = UUID.fromString("43000000-0000-0000-0000-000000000003");
    private static final UUID CROP_ID = UUID.fromString("43000000-0000-0000-0000-000000000004");
    private static final UUID DISEASE_ID = UUID.fromString("43000000-0000-0000-0000-000000000005");
    private static final UUID ACTOR_ID = UUID.fromString("43000000-0000-0000-0000-000000000006");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    private final AiModelRepository modelRepository = mock(AiModelRepository.class);
    private final CropTypeRepository cropTypeRepository = mock(CropTypeRepository.class);
    private final AiModelDiseaseMappingRepository mappingRepository = mock(AiModelDiseaseMappingRepository.class);
    private final PersistenceAiModelVersionMappingReadiness readiness =
            new PersistenceAiModelVersionMappingReadiness(
                    modelRepository, cropTypeRepository, mappingRepository);

    @Test
    void requiresEveryDiseaseClassToBeMappedForEachSupportedCrop() {
        AiModelVersion version = version();
        when(modelRepository.findById(MODEL_ID)).thenReturn(Optional.of(selectedCropModel()));
        when(mappingRepository.findAllValidByClassIdsAndCropTypeIds(Set.of(CLASS_ID), Set.of(CROP_ID)))
                .thenReturn(List.of());

        assertFalse(readiness.hasCompleteDiseaseMappings(version));

        when(mappingRepository.findAllValidByClassIdsAndCropTypeIds(Set.of(CLASS_ID), Set.of(CROP_ID)))
                .thenReturn(List.of(AiModelDiseaseMapping.create(
                        UUID.randomUUID(), CLASS_ID, CROP_ID, DISEASE_ID, ACTOR_ID, NOW)));

        assertTrue(readiness.hasCompleteDiseaseMappings(version));
        assertTrue(readiness.hasCompleteDiseaseMappingsForCrop(version, CROP_ID));
    }

    private AiModel selectedCropModel() {
        return AiModel.create(
                MODEL_ID,
                "RICE_DETECTOR",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(CROP_ID),
                ACTOR_ID,
                NOW);
    }

    private AiModelVersion version() {
        return AiModelVersion.create(
                VERSION_ID,
                MODEL_ID,
                "1.0.0",
                UUID.randomUUID(),
                "a".repeat(64),
                224,
                224,
                List.of(AiModelVersionClass.create(
                        CLASS_ID, VERSION_ID, 0, "Brown_Spot", "Brown spot", AiModelClassKind.DISEASE)),
                ACTOR_ID,
                NOW);
    }
}
