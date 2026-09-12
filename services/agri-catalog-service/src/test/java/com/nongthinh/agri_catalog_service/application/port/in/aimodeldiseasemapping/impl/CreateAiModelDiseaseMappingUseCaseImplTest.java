package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class CreateAiModelDiseaseMappingUseCaseImplTest {

    private static final UUID MAPPING_ID = UUID.fromString("41000000-0000-0000-0000-000000000001");
    private static final UUID CLASS_ID = UUID.fromString("41000000-0000-0000-0000-000000000002");
    private static final UUID VERSION_ID = UUID.fromString("41000000-0000-0000-0000-000000000003");
    private static final UUID CROP_ID = UUID.fromString("41000000-0000-0000-0000-000000000004");
    private static final UUID DISEASE_ID = UUID.fromString("41000000-0000-0000-0000-000000000005");
    private static final UUID ADMIN_ID = UUID.fromString("41000000-0000-0000-0000-000000000006");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    private final AiModelDiseaseMappingRepository mappingRepository = mock(AiModelDiseaseMappingRepository.class);
    private final AiModelVersionRepository versionRepository = mock(AiModelVersionRepository.class);
    private final CropTypeRepository cropTypeRepository = mock(CropTypeRepository.class);
    private final DiseaseRepository diseaseRepository = mock(DiseaseRepository.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final CreateAiModelDiseaseMappingUseCaseImpl useCase = new CreateAiModelDiseaseMappingUseCaseImpl(
            new AiModelDiseaseMappingUseCaseSupport(
                    mappingRepository, versionRepository, cropTypeRepository, diseaseRepository),
            mappingRepository, idGenerator, clockProvider, currentUserProvider);

    @BeforeEach
    void setUp() {
        when(versionRepository.findClassById(CLASS_ID)).thenReturn(Optional.of(diseaseClass()));
        when(cropTypeRepository.findById(CROP_ID)).thenReturn(Optional.of(activeCrop()));
        when(diseaseRepository.findById(DISEASE_ID)).thenReturn(Optional.of(approvedDisease(CROP_ID)));
        when(mappingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mappingRepository.existsByModelVersionClassIdAndCropTypeId(CLASS_ID, CROP_ID)).thenReturn(false);
        when(idGenerator.generate()).thenReturn(MAPPING_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                ADMIN_ID, "admin", "admin@nongthinh.vn", Set.of("ROLE_ADMIN"), null, Set.of()));
    }

    @Test
    void createsMappingForApprovedDiseaseOfTheSameCrop() {
        var result = useCase.execute(command());

        assertEquals(MAPPING_ID, result.id());
        assertEquals(CLASS_ID, result.modelVersionClassId());
        assertEquals(CROP_ID, result.cropTypeId());
        assertEquals(DISEASE_ID, result.diseaseId());
        verify(mappingRepository).save(any());
    }

    @Test
    void rejectsHealthyClass() {
        when(versionRepository.findClassById(CLASS_ID)).thenReturn(Optional.of(AiModelVersionClass.create(
                CLASS_ID, VERSION_ID, 0, "Healthy", "Healthy", AiModelClassKind.HEALTHY)));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(command()));

        assertEquals(ErrorCode.AI_MODEL_DISEASE_MAPPING_CLASS_INVALID, exception.getErrorCode());
        verify(mappingRepository, never()).save(any());
    }

    @Test
    void rejectsDiseaseForAnotherCrop() {
        when(diseaseRepository.findById(DISEASE_ID)).thenReturn(Optional.of(approvedDisease(UUID.randomUUID())));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(command()));

        assertEquals(ErrorCode.AI_MODEL_DISEASE_MAPPING_DISEASE_INVALID, exception.getErrorCode());
    }

    @Test
    void rejectsDiseaseThatIsNotApproved() {
        when(diseaseRepository.findById(DISEASE_ID)).thenReturn(Optional.of(draftDisease()));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(command()));

        assertEquals(ErrorCode.AI_MODEL_DISEASE_MAPPING_DISEASE_INVALID, exception.getErrorCode());
    }

    @Test
    void rejectsDuplicateClassCropMapping() {
        when(mappingRepository.existsByModelVersionClassIdAndCropTypeId(CLASS_ID, CROP_ID)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(command()));

        assertEquals(ErrorCode.AI_MODEL_DISEASE_MAPPING_ALREADY_EXISTS, exception.getErrorCode());
    }

    private AiModelDiseaseMappingCommand command() {
        return new AiModelDiseaseMappingCommand(CLASS_ID, CROP_ID, DISEASE_ID);
    }

    private AiModelVersionClass diseaseClass() {
        return AiModelVersionClass.create(
                CLASS_ID, VERSION_ID, 1, "Brown_Spot", "Brown spot", AiModelClassKind.DISEASE);
    }

    private CropType activeCrop() {
        return CropType.create(CROP_ID, "RICE", "Rice", null, ADMIN_ID, NOW);
    }

    private Disease approvedDisease(UUID cropTypeId) {
        return Disease.create(
                DISEASE_ID, CreatedSource.ADMIN, null, "Brown spot", "brown-spot", null, cropTypeId,
                null, null, null, null, null, null, null, null, null, null, ADMIN_ID, NOW);
    }

    private Disease draftDisease() {
        return Disease.create(
                DISEASE_ID, CreatedSource.BRAND, UUID.randomUUID(), "Brown spot", "brown-spot", null, CROP_ID,
                null, null, null, null, null, null, null, null, null, null, ADMIN_ID, NOW);
    }
}
