package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.AiModelCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.AiModelUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelUpdateRequest;

@Mapper(componentModel = "spring")
public interface AiModelMapper {

    AiModelCreationCommand toAiModelCreationCommand(AiModelCreationRequest request);

    AiModelUpdateCommand toAiModelUpdateCommand(AiModelUpdateRequest request);
}
