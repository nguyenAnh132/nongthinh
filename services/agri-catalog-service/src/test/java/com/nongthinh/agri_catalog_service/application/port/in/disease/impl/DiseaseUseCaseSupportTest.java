package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class DiseaseUseCaseSupportTest {

    private static final UUID CROP_TYPE_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID ADMIN_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000001"
    );
    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    private final CropTypeRepository cropTypeRepository = mock(CropTypeRepository.class);
    private final DiseaseUseCaseSupport support = new DiseaseUseCaseSupport(
            mock(DiseaseRepository.class),
            cropTypeRepository,
            mock(DiseaseReviewHistoryRepository.class),
            mock(CurrentUserProvider.class),
            mock(IdGenerator.class)
    );

    @Test
    void rejectsMissingCropType() {
        when(cropTypeRepository.findById(CROP_TYPE_ID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> support.requireActiveCropType(CROP_TYPE_ID)
        );

        assertEquals(ErrorCode.CROP_TYPE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void rejectsInactiveCropType() {
        CropType cropType = CropType.create(
                CROP_TYPE_ID,
                "RICE",
                "Lúa",
                null,
                ADMIN_ID,
                NOW
        );
        cropType.update("Lúa", null, false, ADMIN_ID, NOW.plusSeconds(60));
        when(cropTypeRepository.findById(CROP_TYPE_ID)).thenReturn(Optional.of(cropType));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> support.requireActiveCropType(CROP_TYPE_ID)
        );

        assertEquals(ErrorCode.CROP_TYPE_INACTIVE, exception.getErrorCode());
    }

    @Test
    void acceptsActiveCropType() {
        when(cropTypeRepository.findById(CROP_TYPE_ID)).thenReturn(Optional.of(
                CropType.create(CROP_TYPE_ID, "RICE", "Lúa", null, ADMIN_ID, NOW)
        ));

        assertDoesNotThrow(() -> support.requireActiveCropType(CROP_TYPE_ID));
    }
}
