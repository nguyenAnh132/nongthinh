package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.DiseaseCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.DiseaseUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.DiseaseCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.DiseaseUpdateRequest;

@Mapper(componentModel = "spring")
public interface DiseaseMapper {

    DiseaseCreationCommand toDiseaseCreationCommand(DiseaseCreationRequest request);

    DiseaseUpdateCommand toDiseaseUpdateCommand(DiseaseUpdateRequest request);
}
