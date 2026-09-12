package com.nongthinh.location_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.location_service.application.command.ProvinceCreationCommand;
import com.nongthinh.location_service.application.command.ProvinceUpdateCommand;
import com.nongthinh.location_service.presentation.dto.request.ProvinceCreationRequest;
import com.nongthinh.location_service.presentation.dto.request.ProvinceUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    ProvinceCreationCommand toProvinceCreationCommand(ProvinceCreationRequest request);

    ProvinceUpdateCommand toProvinceUpdateCommand(ProvinceUpdateRequest request);
}
