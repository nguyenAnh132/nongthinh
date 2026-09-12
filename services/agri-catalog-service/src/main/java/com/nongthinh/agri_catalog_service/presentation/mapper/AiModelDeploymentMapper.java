package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.AiModelDeploymentCreationCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelDeploymentCreationRequest;

@Mapper(componentModel = "spring")
public interface AiModelDeploymentMapper {

    AiModelDeploymentCreationCommand toCommand(AiModelDeploymentCreationRequest request);
}
