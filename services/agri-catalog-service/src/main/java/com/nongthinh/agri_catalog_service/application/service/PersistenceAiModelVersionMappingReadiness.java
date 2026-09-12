package com.nongthinh.agri_catalog_service.application.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionMappingReadiness;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersistenceAiModelVersionMappingReadiness implements AiModelVersionMappingReadiness {

    private final AiModelRepository aiModelRepository;
    private final CropTypeRepository cropTypeRepository;
    private final AiModelDiseaseMappingRepository mappingRepository;

    @Override
    public boolean hasCompleteDiseaseMappings(AiModelVersion version) {
        Set<UUID> classIds = diseaseClassIds(version);
        if (classIds.isEmpty()) {
            return true;
        }
        Set<UUID> cropTypeIds = supportedCropTypeIds(version);
        if (cropTypeIds.isEmpty()) {
            return false;
        }
        Set<MappingKey> validMappings = mappingRepository
                .findAllValidByClassIdsAndCropTypeIds(classIds, cropTypeIds)
                .stream()
                .map(mapping -> new MappingKey(mapping.getModelVersionClassId(), mapping.getCropTypeId()))
                .collect(java.util.stream.Collectors.toSet());
        return validMappings.size() == classIds.size() * cropTypeIds.size();
    }

    @Override
    public boolean hasCompleteDiseaseMappingsForCrop(AiModelVersion version, UUID cropTypeId) {
        Set<UUID> classIds = diseaseClassIds(version);
        if (classIds.isEmpty()) {
            return true;
        }
        Set<MappingKey> validMappings = mappingRepository
                .findAllValidByClassIdsAndCropTypeIds(classIds, Set.of(cropTypeId))
                .stream()
                .map(mapping -> new MappingKey(mapping.getModelVersionClassId(), mapping.getCropTypeId()))
                .collect(java.util.stream.Collectors.toSet());
        return validMappings.size() == classIds.size();
    }

    private Set<UUID> diseaseClassIds(AiModelVersion version) {
        return version.getClasses().stream()
                .filter(item -> item.getClassKind() == AiModelClassKind.DISEASE)
                .map(item -> item.getId())
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<UUID> supportedCropTypeIds(AiModelVersion version) {
        return aiModelRepository.findById(version.getModelId())
                .map(model -> model.getCropCoverageType() == CropCoverageType.SELECTED_CROPS
                        ? model.getCropTypeIds()
                        : cropTypeRepository.findAllActive().stream()
                                .map(item -> item.getId())
                                .collect(java.util.stream.Collectors.toSet()))
                .orElseGet(HashSet::new);
    }

    private record MappingKey(UUID classId, UUID cropTypeId) {
    }
}
