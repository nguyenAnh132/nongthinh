package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.model.ResolvedAiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.ResolveAiModelDiseaseMappingsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingResolutionView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveAiModelDiseaseMappingsUseCaseImpl implements ResolveAiModelDiseaseMappingsUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;
    private final AiModelDiseaseMappingRepository mappingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiModelDiseaseMappingResolutionView> execute(
            UUID modelVersionId, UUID cropTypeId, List<String> classCodes) {
        support.requireVersion(modelVersionId);
        List<String> requestedCodes = new LinkedHashSet<>(classCodes).stream().toList();
        Map<String, ResolvedAiModelDiseaseMapping> resolvedByClassCode = mappingRepository
                .resolveValidMappings(modelVersionId, cropTypeId, requestedCodes)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        ResolvedAiModelDiseaseMapping::classCode,
                        Function.identity()));
        if (resolvedByClassCode.size() != requestedCodes.size()) {
            throw new BusinessException(ErrorCode.CATALOG_MAPPING_NOT_READY);
        }
        return requestedCodes.stream()
                .map(resolvedByClassCode::get)
                .map(item -> new AiModelDiseaseMappingResolutionView(
                        item.classCode(), item.diseaseId(), item.displayName(), item.catalogUpdatedAt()))
                .toList();
    }
}
