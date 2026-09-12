package com.nongthinh.auth_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.auth_service.application.command.RegisterFarmerCommand;
import com.nongthinh.auth_service.presentation.dto.request.RegisterFarmerRequest;

@Mapper(componentModel = "spring")
public interface FarmerMapper {

    RegisterFarmerCommand toRegisterFarmerCommand(RegisterFarmerRequest request);

}
