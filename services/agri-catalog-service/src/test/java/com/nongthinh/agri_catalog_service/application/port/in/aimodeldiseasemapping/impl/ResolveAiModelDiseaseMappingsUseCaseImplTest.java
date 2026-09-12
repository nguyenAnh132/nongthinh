package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.model.ResolvedAiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class ResolveAiModelDiseaseMappingsUseCaseImplTest {

    private static final UUID VERSION_ID = UUID.fromString("42000000-0000-0000-0000-000000000001");
    private static final UUID MODEL_ID = UUID.fromString("42000000-0000-0000-0000-000000000002");
    private static final UUID CROP_ID = UUID.fromString("42000000-0000-0000-0000-000000000003");
    private static final UUID ACTOR_ID = UUID.fromString("42000000-0000-0000-0000-000000000004");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    private final AiModelDiseaseMappingRepository mappingRepository = mock(AiModelDiseaseMappingRepository.class);
    private final AiModelVersionRepository versionRepository = mock(AiModelVersionRepository.class);
    private final ResolveAiModelDiseaseMappingsUseCaseImpl useCase = new ResolveAiModelDiseaseMappingsUseCaseImpl(
            new AiModelDiseaseMappingUseCaseSupport(
                    mappingRepository, versionRepository, mock(CropTypeRepository.class), mock(DiseaseRepository.class)),
            mappingRepository);

    @Test
    void resolvesBatchInRequestOrder() {
        when(versionRepository.findById(VERSION_ID)).thenReturn(Optional.of(version()));
        UUID brownSpotId = UUID.randomUUID();
        UUID leafBlastId = UUID.randomUUID();
        when(mappingRepository.resolveValidMappings(VERSION_ID, CROP_ID, List.of("Leaf_Blast", "Brown_Spot")))
                .thenReturn(List.of(
                        new ResolvedAiModelDiseaseMapping("Brown_Spot", brownSpotId, "Brown spot", NOW),
                        new ResolvedAiModelDiseaseMapping("Leaf_Blast", leafBlastId, "Leaf blast", NOW)));

        var result = useCase.execute(VERSION_ID, CROP_ID, List.of("Leaf_Blast", "Brown_Spot"));

        assertEquals(List.of("Leaf_Blast", "Brown_Spot"), result.stream().map(item -> item.classCode()).toList());
        assertEquals(leafBlastId, result.getFirst().diseaseId());
    }

    @Test
    void returnsCatalogNotReadyWhenOneMappingIsMissingOrInvalid() {
        when(versionRepository.findById(VERSION_ID)).thenReturn(Optional.of(version()));
        when(mappingRepository.resolveValidMappings(VERSION_ID, CROP_ID, List.of("Brown_Spot")))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(VERSION_ID, CROP_ID, List.of("Brown_Spot")));

        assertEquals(ErrorCode.CATALOG_MAPPING_NOT_READY, exception.getErrorCode());
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
                        UUID.randomUUID(), VERSION_ID, 0, "Brown_Spot", "Brown spot", AiModelClassKind.DISEASE)),
                ACTOR_ID,
                NOW);
    }
}
