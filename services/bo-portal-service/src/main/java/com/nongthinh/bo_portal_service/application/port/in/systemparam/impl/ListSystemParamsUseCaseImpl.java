package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.ListSystemParamsUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListSystemParamsUseCaseImpl implements ListSystemParamsUseCase {

    private final SystemParamRepository systemParamRepository;
    private final SystemParamTypeRepository systemParamTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemParamView> execute() {
        Map<Long, String> typeNamesById = systemParamTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SystemParamType::getId, SystemParamType::getName));

        return systemParamRepository.findAll().stream()
                .map(param -> SystemParamView.fromSystemParam(
                        param,
                        typeNamesById.get(param.getTypeId())))
                .toList();
    }
}
