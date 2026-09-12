package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.CropTypeCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.CropTypeUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.CropTypeCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.CropTypeUpdateRequest;

@Mapper(componentModel = "spring")
public interface CropTypeMapper {

    CropTypeCreationCommand toCropTypeCreationCommand(CropTypeCreationRequest request);

    CropTypeUpdateCommand toCropTypeUpdateCommand(CropTypeUpdateRequest request);
}

