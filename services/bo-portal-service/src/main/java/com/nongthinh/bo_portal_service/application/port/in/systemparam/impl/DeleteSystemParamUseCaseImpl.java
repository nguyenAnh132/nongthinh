package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparam.DeleteSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteSystemParamUseCaseImpl implements DeleteSystemParamUseCase {

    private final SystemParamRepository systemParamRepository;

    @Override
    @Transactional
    public void execute(String name) {

        long start =  System.currentTimeMillis();

        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_REQUIRED);
        }
        String normalizedName = name.trim().toUpperCase();
        SystemParam systemParam = systemParamRepository.findByName(normalizedName)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_NOT_FOUND, name));

        systemParam.ensureDeletable();

        systemParamRepository.deleteByName(normalizedName);

        log.info(
                "[Application - SystemParam] System Param: {} deleted successfully | durationMs={}",
                systemParam.getName(),
                System.currentTimeMillis() -start
        );
    }
}
