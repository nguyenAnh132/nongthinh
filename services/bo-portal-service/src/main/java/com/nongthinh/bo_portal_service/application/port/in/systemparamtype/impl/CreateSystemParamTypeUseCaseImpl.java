package com.nongthinh.bo_portal_service.application.port.in.systemparamtype.impl;

import com.nongthinh.bo_portal_service.application.command.CreateSystemParamTypeCommand;
import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.CreateSystemParamTypeUseCase;
import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSystemParamTypeUseCaseImpl implements CreateSystemParamTypeUseCase {

    private final SystemParamTypeRepository systemParamTypeRepository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public SystemParamTypeView execute(CreateSystemParamTypeCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_NAME_REQUIRED);
        }

        String normalizedName = command.name().trim();
        if (systemParamTypeRepository.existsByName(normalizedName)) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_NAME_DUPLICATED, normalizedName);
        }

        SystemParamType systemParamType = SystemParamType.create(
                normalizedName,
                command.description(),
                clockProvider.now());

        SystemParamType saved = systemParamTypeRepository.save(systemParamType);

        log.info(
                "[Application - SystemParamType] System Param Type {} created successfully | durationMs={}",
                saved.getName(),
                System.currentTimeMillis() - start
        );

        return SystemParamTypeView.fromSystemParamType(saved);
    }
}
