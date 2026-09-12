package com.nongthinh.location_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.location_service.application.command.CommuneCreationCommand;
import com.nongthinh.location_service.application.command.CommuneUpdateCommand;
import com.nongthinh.location_service.presentation.dto.request.CommuneCreationRequest;
import com.nongthinh.location_service.presentation.dto.request.CommuneUpdateRequest;

@Mapper(componentModel = "spring")
public interface CommuneMapper {

    CommuneCreationCommand toCommuneCreationCommand(CommuneCreationRequest request);

    CommuneUpdateCommand toCommuneUpdateCommand(CommuneUpdateRequest request);
}
