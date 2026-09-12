package com.nongthinh.bo_portal_service.application.port.in.systemparamtype.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.GetSystemParamTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSystemParamTypeUseCaseImpl implements GetSystemParamTypeUseCase {

    private final SystemParamTypeRepository systemParamTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemParamTypeView execute(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_ID_REQUIRED);
        }

        SystemParamType systemParamType = systemParamTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_NOT_FOUND, String.valueOf(id)));

        return SystemParamTypeView.fromSystemParamType(systemParamType);
    }
}
