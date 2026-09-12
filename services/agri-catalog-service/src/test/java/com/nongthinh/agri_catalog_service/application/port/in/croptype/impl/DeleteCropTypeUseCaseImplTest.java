package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class DeleteCropTypeUseCaseImplTest {

    private static final UUID CROP_TYPE_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID ADMIN_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000001"
    );
    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    private final CropTypeUseCaseSupport support = mock(CropTypeUseCaseSupport.class);
    private final CropTypeRepository cropTypeRepository = mock(CropTypeRepository.class);
    private final DiseaseRepository diseaseRepository = mock(DiseaseRepository.class);
    private final AiModelRepository aiModelRepository = mock(AiModelRepository.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final DeleteCropTypeUseCaseImpl useCase = new DeleteCropTypeUseCaseImpl(
            support,
            cropTypeRepository,
            diseaseRepository,
            aiModelRepository,
            clockProvider,
            currentUserProvider
    );

    @Test
    void rejectsDeletionWhenDiseaseUsesCropType() {
        CropType cropType = cropType();
        when(support.requireCropType(CROP_TYPE_ID)).thenReturn(cropType);
        when(diseaseRepository.existsByCropTypeId(CROP_TYPE_ID)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(CROP_TYPE_ID)
        );

        assertEquals(ErrorCode.CROP_TYPE_IN_USE, exception.getErrorCode());
        verifyNoInteractions(cropTypeRepository, clockProvider, currentUserProvider);
    }

    @Test
    void softDeletesCropTypeWhenItIsUnused() {
        CropType cropType = cropType();
        when(support.requireCropType(CROP_TYPE_ID)).thenReturn(cropType);
        when(diseaseRepository.existsByCropTypeId(CROP_TYPE_ID)).thenReturn(false);
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                ADMIN_ID,
                "admin-keycloak-id",
                "admin@nongthinh.vn",
                Set.of("ROLE_ADMIN"),
                null,
                Set.of()
        ));
        when(clockProvider.now()).thenReturn(NOW);

        useCase.execute(CROP_TYPE_ID);

        assertEquals(NOW, cropType.getDeletedAt());
        assertEquals(ADMIN_ID, cropType.getDeletedBy());
        verify(cropTypeRepository).save(cropType);
    }

    @Test
    void rejectsDeletionWhenAiModelCoversCropType() {
        CropType cropType = cropType();
        when(support.requireCropType(CROP_TYPE_ID)).thenReturn(cropType);
        when(diseaseRepository.existsByCropTypeId(CROP_TYPE_ID)).thenReturn(false);
        when(aiModelRepository.existsByCropTypeId(CROP_TYPE_ID)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(CROP_TYPE_ID)
        );

        assertEquals(ErrorCode.CROP_TYPE_IN_USE, exception.getErrorCode());
        verifyNoInteractions(cropTypeRepository, clockProvider, currentUserProvider);
    }

    private CropType cropType() {
        return CropType.create(
                CROP_TYPE_ID,
                "RICE",
                "Lúa",
                null,
                ADMIN_ID,
                NOW.minusSeconds(60)
        );
    }
}
