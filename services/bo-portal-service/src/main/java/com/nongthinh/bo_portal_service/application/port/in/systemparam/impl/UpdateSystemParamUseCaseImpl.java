package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamCommand;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.UpdateSystemParamUseCase;
import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSystemParamUseCaseImpl implements UpdateSystemParamUseCase {

    private final SystemParamRepository systemParamRepository;
    private final SystemParamTypeRepository systemParamTypeRepository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public SystemParamView execute(String name, UpdateSystemParamCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_REQUIRED);
        }
        if (command.value() == null || command.value().isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_VALUE_REQUIRED);
        }

        SystemParam systemParam = systemParamRepository.findByName(name.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_NOT_FOUND, name));

        systemParam.update(command.value(), command.description(), clockProvider.now());

        SystemParam saved = systemParamRepository.save(systemParam);

        String typeName = systemParamTypeRepository.findById(saved.getTypeId())
                .map(type -> type.getName())
                .orElse(null);

        return SystemParamView.fromSystemParam(saved, typeName);
    }
}
