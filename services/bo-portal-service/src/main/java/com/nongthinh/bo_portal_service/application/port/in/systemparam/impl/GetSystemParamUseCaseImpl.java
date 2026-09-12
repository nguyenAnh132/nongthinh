package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.GetSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSystemParamUseCaseImpl implements GetSystemParamUseCase {

    private final SystemParamRepository systemParamRepository;
    private final SystemParamTypeRepository systemParamTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemParamView execute(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_REQUIRED);
        }

        SystemParam systemParam = systemParamRepository.findByName(name.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_NOT_FOUND, name));

        String typeName = systemParamTypeRepository.findById(systemParam.getTypeId())
                .map(type -> type.getName())
                .orElse(null);

        return SystemParamView.fromSystemParam(systemParam, typeName);
    }
}
