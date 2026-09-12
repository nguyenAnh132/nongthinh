package com.nongthinh.bo_portal_service.application.port.in.systemparamtype.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.ListSystemParamsGroupedByTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeGroupView;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListSystemParamsGroupedByTypeUseCaseImpl implements ListSystemParamsGroupedByTypeUseCase {

    private final SystemParamTypeRepository systemParamTypeRepository;
    private final SystemParamRepository systemParamRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemParamTypeGroupView> execute() {
        List<SystemParamType> types = systemParamTypeRepository.findAll();
        Map<Long, List<SystemParam>> paramsByTypeId = systemParamRepository.findAll().stream()
                .collect(Collectors.groupingBy(SystemParam::getTypeId));

        return types.stream()
                .map(type -> new SystemParamTypeGroupView(
                        type.getId(),
                        type.getName(),
                        type.getDescription(),
                        type.isSystemDefined(),
                        type.getCreatedAt(),
                        type.getUpdatedAt(),
                        paramsByTypeId.getOrDefault(type.getId(), List.of()).stream()
                                .map(param -> SystemParamView.fromSystemParam(param, type.getName()))
                                .toList()))
                .toList();
    }
}
