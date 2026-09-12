package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.AiModelVersionClassCommand;
import com.nongthinh.agri_catalog_service.application.command.AiModelVersionCreationCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelVersionClassRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelVersionCreationRequest;

@Mapper(componentModel = "spring")
public interface AiModelVersionMapper {

    AiModelVersionCreationCommand toCommand(AiModelVersionCreationRequest request);

    AiModelVersionClassCommand toCommand(AiModelVersionClassRequest request);
}
