package com.nongthinh.agri_catalog_service.application.port.out;

import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import java.util.UUID;

public interface AiModelVersionMappingReadiness {
    boolean hasCompleteDiseaseMappings(AiModelVersion version);

    boolean hasCompleteDiseaseMappingsForCrop(AiModelVersion version, UUID cropTypeId);
}
