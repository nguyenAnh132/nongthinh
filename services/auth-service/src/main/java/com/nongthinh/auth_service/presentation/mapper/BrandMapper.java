package com.nongthinh.auth_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.auth_service.application.command.RegisterBrandCommand;
import com.nongthinh.auth_service.presentation.dto.request.RegisterBrandRequest;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    RegisterBrandCommand toRegisterBrandCommand(RegisterBrandRequest request);
}
