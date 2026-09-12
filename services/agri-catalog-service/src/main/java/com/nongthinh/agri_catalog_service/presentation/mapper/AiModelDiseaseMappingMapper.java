package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.AiModelDiseaseMappingRequest;

@Mapper(componentModel = "spring")
public interface AiModelDiseaseMappingMapper {
    AiModelDiseaseMappingCommand toCommand(AiModelDiseaseMappingRequest request);
}
