package com.nongthinh.bo_portal_service.presentation.mapper;

import com.nongthinh.bo_portal_service.application.command.CreateSystemParamTypeCommand;
import com.nongthinh.bo_portal_service.presentation.dto.request.CreateSystemParamTypeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemParamTypeMapper {

    CreateSystemParamTypeCommand toCreateCommand(CreateSystemParamTypeRequest request);
}
