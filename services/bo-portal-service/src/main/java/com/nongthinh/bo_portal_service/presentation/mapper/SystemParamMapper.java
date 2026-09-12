package com.nongthinh.bo_portal_service.presentation.mapper;

import com.nongthinh.bo_portal_service.application.command.CreateSystemParamCommand;
import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamCommand;
import com.nongthinh.bo_portal_service.application.command.UpdateSystemParamTypeAssignmentCommand;
import com.nongthinh.bo_portal_service.presentation.dto.request.CreateSystemParamRequest;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateSystemParamRequest;
import com.nongthinh.bo_portal_service.presentation.dto.request.UpdateSystemParamTypeAssignmentRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemParamMapper {
    CreateSystemParamCommand toCreateCommand(CreateSystemParamRequest request);

    UpdateSystemParamCommand toUpdateCommand(UpdateSystemParamRequest request);

    UpdateSystemParamTypeAssignmentCommand toUpdateTypeAssignmentCommand(UpdateSystemParamTypeAssignmentRequest request);
}
