package com.nongthinh.bo_portal_service.application.port.in.systemparam.impl;

import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamTypeAssignmentCommand;
import com.nongthinh.bo_portal_service.application.port.in.systemparam.UpdateSystemParamTypeAssignmentUseCase;
import com.nongthinh.bo_portal_service.application.port.out.ClockProvider;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamRepository;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamView;
import com.nongthinh.bo_portal_service.common.exception.ErrorCode;
import com.nongthinh.bo_portal_service.domain.exception.BusinessException;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSystemParamTypeAssignmentUseCaseImpl implements UpdateSystemParamTypeAssignmentUseCase {

    private final SystemParamRepository systemParamRepository;
    private final SystemParamTypeRepository systemParamTypeRepository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public SystemParamView execute(String name, UpdateSystemParamTypeAssignmentCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_NAME_REQUIRED);
        }
        if (command.typeId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_PARAM_TYPE_ID_REQUIRED);
        }

        SystemParam systemParam = systemParamRepository.findByName(name.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_PARAM_NOT_FOUND, name));

        SystemParamType systemParamType = systemParamTypeRepository.findById(command.typeId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SYSTEM_PARAM_TYPE_NOT_FOUND,
                        String.valueOf(command.typeId())));

        systemParam.changeType(systemParamType.getId(), clockProvider.now());

        SystemParam saved = systemParamRepository.save(systemParam);
        return SystemParamView.fromSystemParam(saved, systemParamType.getName());
    }
}
