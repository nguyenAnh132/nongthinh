package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;

@SpringBootTest
@ActiveProfiles("test")
class AiModelRepositoryImplTest {

    private static final UUID MODEL_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("40000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Autowired
    private AiModelRepository aiModelRepository;

    @Autowired
    private CropTypeRepository cropTypeRepository;

    @Test
    void replacesSelectedCropMappingsWhenCoverageChangesToAllCrops() {
        cropTypeRepository.save(CropType.create(
                CROP_TYPE_ID,
                "RICE_REGISTRY_TEST",
                "Rice",
                null,
                ADMIN_ID,
                NOW
        ));
        AiModel aiModel = AiModel.create(
                MODEL_ID,
                "RICE_REGISTRY_TEST",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(CROP_TYPE_ID),
                ADMIN_ID,
                NOW
        );

        aiModelRepository.save(aiModel);
        assertTrue(aiModelRepository.existsByCropTypeId(CROP_TYPE_ID));

        aiModel.update(
                "Rice detector v2",
                null,
                CropCoverageType.ALL_CROPS,
                Set.of(),
                ADMIN_ID,
                NOW.plusSeconds(60)
        );
        aiModelRepository.save(aiModel);

        assertFalse(aiModelRepository.existsByCropTypeId(CROP_TYPE_ID));
        assertTrue(aiModelRepository.findById(MODEL_ID).orElseThrow().getCropTypeIds().isEmpty());
    }
}
