package com.nongthinh.agri_catalog_service.domain.aimodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class AiModelTest {

    private static final UUID MODEL_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Test
    void selectedCropsRequiresAtLeastOneCrop() {
        BusinessException exception = assertThrows(BusinessException.class, () -> AiModel.create(
                MODEL_ID,
                "RICE_DETECTOR",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(),
                ADMIN_ID,
                NOW
        ));

        assertEquals(ErrorCode.AI_MODEL_CROP_COVERAGE_INVALID, exception.getErrorCode());
    }

    @Test
    void allCropsCannotHaveExplicitCropRecords() {
        BusinessException exception = assertThrows(BusinessException.class, () -> AiModel.create(
                MODEL_ID,
                "GENERAL_DETECTOR",
                "General detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.ALL_CROPS,
                Set.of(CROP_TYPE_ID),
                ADMIN_ID,
                NOW
        ));

        assertEquals(ErrorCode.AI_MODEL_CROP_COVERAGE_INVALID, exception.getErrorCode());
    }

    @Test
    void retiredModelCannotBeChangedAndCodeRemainsStable() {
        AiModel aiModel = AiModel.create(
                MODEL_ID,
                "rice_detector",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(CROP_TYPE_ID),
                ADMIN_ID,
                NOW
        );

        aiModel.retire(ADMIN_ID, NOW.plusSeconds(60));

        BusinessException exception = assertThrows(BusinessException.class, () -> aiModel.update(
                "Changed name",
                null,
                CropCoverageType.ALL_CROPS,
                Set.of(),
                ADMIN_ID,
                NOW.plusSeconds(120)
        ));

        assertEquals(AiModelStatus.RETIRED, aiModel.getStatus());
        assertEquals("RICE_DETECTOR", aiModel.getCode());
        assertEquals(ErrorCode.AI_MODEL_RETIRED, exception.getErrorCode());
    }
}
