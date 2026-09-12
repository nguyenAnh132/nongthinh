package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.command.CreateSystemParamCommand;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.CreateSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.common.constant.SystemParamTypeNames;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamDataType;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSystemParamUseCaseImpl implements CreateSystemParamUseCase {

    private final SystemParamRepository systemParamRepository;
    private final SystemParamTypeRepository systemParamTypeRepository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public SystemParamView execute(CreateSystemParamCommand command) {

        long start = System.currentTimeMillis();

        Objects.requireNonNull(command, "command is required");

        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_REQUIRED);
        }
        if (command.value() == null || command.value().isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_REQUIRED);
        }

        String normalizedName = command.name().trim().toUpperCase();
        if (systemParamRepository.existsByName(normalizedName)) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_DUPLICATED, normalizedName);
        }

        SystemParamType systemParamType = resolveType(command.typeId());

        SystemParam systemParam = SystemParam.create(
                normalizedName,
                command.value(),
                command.description(),
                SystemParamDataType.from(command.dataType()),
                systemParamType.getId(),
                clockProvider.now());

        SystemParam saved = systemParamRepository.save(systemParam);

        log.info(
                "[Application - SystemParam] New System Param: {} created successfully | durationMs = {}",
                systemParam.getName(),
                System.currentTimeMillis() - start
        );

        return SystemParamView.fromSystemParam(saved, systemParamType.getName());
    }

    private SystemParamType resolveType(Long typeId) {
        if (typeId == null) {
            return systemParamTypeRepository.findByName(SystemParamTypeNames.OTHER)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.SYSTEM_PARAM_TYPE_NOT_FOUND,
                            SystemParamTypeNames.OTHER));
        }
        return systemParamTypeRepository.findById(typeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SYSTEM_PARAM_TYPE_NOT_FOUND,
                        String.valueOf(typeId)));
    }
}
