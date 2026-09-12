package com.nongthinh.agri_catalog_service.domain.aimodelversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class AiModelVersionTest {

    private static final UUID VERSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID MODEL_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID FILE_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID ADMIN_ID = UUID.fromString("30000000-0000-0000-0000-000000000004");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Test
    void transitionsThroughValidationReadyAndActivation() {
        AiModelVersion version = version();

        version.startValidation();
        version.completeValidation(true, "{\"status\":\"VALIDATED\"}", ADMIN_ID, NOW);
        version.markReady();
        version.activate();

        assertEquals(AiModelVersionStatus.ACTIVE, version.getStatus());
        version.deactivate();
        assertEquals(AiModelVersionStatus.READY, version.getStatus());
        version.retire(ADMIN_ID, NOW);
        assertEquals(AiModelVersionStatus.RETIRED, version.getStatus());
    }

    @Test
    void rejectsActivationBeforeVersionIsReady() {
        BusinessException exception = assertThrows(BusinessException.class, () -> version().activate());

        assertEquals(ErrorCode.AI_MODEL_VERSION_STATE_INVALID, exception.getErrorCode());
    }

    @Test
    void rejectsDuplicateClassIndexInImmutableManifest() {
        List<AiModelVersionClass> duplicateManifest = List.of(
                item(UUID.fromString("30000000-0000-0000-0000-000000000005"), 0, "Brown_Spot"),
                item(UUID.fromString("30000000-0000-0000-0000-000000000006"), 0, "Leaf_Blast")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> AiModelVersion.create(
                        VERSION_ID, MODEL_ID, "1.0.0", FILE_ID, "a".repeat(64),
                        640, 640, duplicateManifest, ADMIN_ID, NOW)
        );

        assertEquals(ErrorCode.AI_MODEL_VERSION_MANIFEST_INVALID, exception.getErrorCode());
    }

    private AiModelVersion version() {
        return AiModelVersion.create(
                VERSION_ID,
                MODEL_ID,
                "1.0.0",
                FILE_ID,
                "a".repeat(64),
                640,
                640,
                List.of(item(UUID.fromString("30000000-0000-0000-0000-000000000005"), 0, "Brown_Spot")),
                ADMIN_ID,
                NOW
        );
    }

    private AiModelVersionClass item(UUID id, int index, String code) {
        return AiModelVersionClass.create(id, VERSION_ID, index, code, code, AiModelClassKind.DISEASE);
    }
}
