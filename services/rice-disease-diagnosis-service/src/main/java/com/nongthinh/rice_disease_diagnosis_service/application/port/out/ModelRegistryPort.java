package com.nongthinh.rice_disease_diagnosis_service.application.port.out;

import java.util.List;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ResolvedDiseaseMapping;

public interface ModelRegistryPort {
    ActiveModelDeployment resolveActiveDeployment(UUID cropTypeId);

    List<ResolvedDiseaseMapping> resolveDiseaseMappings(
            UUID modelVersionId, UUID cropTypeId, List<String> classCodes);
}
