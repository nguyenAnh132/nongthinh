package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.GetSystemParamValueUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamValueView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSystemParamValueUseCaseImpl implements GetSystemParamValueUseCase {

    private final SystemParamRepository systemParamRepository;

    @Override
    public SystemParamValueView execute(String name) {
        SystemParam systemParam = systemParamRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_NOT_FOUND));

        return SystemParamValueView.fromSystemPram(systemParam);
    }
}
