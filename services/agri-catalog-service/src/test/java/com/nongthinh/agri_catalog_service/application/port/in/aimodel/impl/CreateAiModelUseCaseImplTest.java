package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

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
import com.nongthinh.agri_catalog_service.application.command.AiModelCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class CreateAiModelUseCaseImplTest {

    private static final UUID MODEL_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    private final AiModelRepository aiModelRepository = mock(AiModelRepository.class);
    private final CropTypeRepository cropTypeRepository = mock(CropTypeRepository.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final CreateAiModelUseCaseImpl useCase = new CreateAiModelUseCaseImpl(
            new AiModelUseCaseSupport(aiModelRepository, cropTypeRepository),
            aiModelRepository,
            idGenerator,
            clockProvider,
            currentUserProvider
    );

    @BeforeEach
    void setUp() {
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(idGenerator.generate()).thenReturn(MODEL_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                ADMIN_ID,
                "admin-keycloak-id",
                "admin@nongthinh.vn",
                Set.of("ROLE_ADMIN"),
                null,
                Set.of()
        ));
    }

    @Test
    void createsSelectedCoverageOnlyForActiveExistingCropTypes() {
        when(cropTypeRepository.findById(CROP_TYPE_ID)).thenReturn(Optional.of(activeCropType()));

        var view = useCase.execute(command(CropCoverageType.SELECTED_CROPS, Set.of(CROP_TYPE_ID)));

        assertEquals(CropCoverageType.SELECTED_CROPS, view.cropCoverageType());
        assertEquals(Set.of(CROP_TYPE_ID), view.cropTypeIds());
        verify(aiModelRepository).save(any(AiModel.class));
    }

    @Test
    void rejectsInactiveCropType() {
        CropType inactiveCropType = activeCropType();
        inactiveCropType.update("Rice", null, false, ADMIN_ID, NOW);
        when(cropTypeRepository.findById(CROP_TYPE_ID)).thenReturn(Optional.of(inactiveCropType));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(command(CropCoverageType.SELECTED_CROPS, Set.of(CROP_TYPE_ID)))
        );

        assertEquals(ErrorCode.CROP_TYPE_INACTIVE, exception.getErrorCode());
        verify(aiModelRepository, never()).save(any(AiModel.class));
    }

    private AiModelCreationCommand command(CropCoverageType coverageType, Set<UUID> cropTypeIds) {
        return new AiModelCreationCommand(
                "RICE_DETECTOR",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                coverageType,
                cropTypeIds
        );
    }

    private CropType activeCropType() {
        return CropType.create(CROP_TYPE_ID, "RICE", "Rice", null, ADMIN_ID, NOW);
    }
}
