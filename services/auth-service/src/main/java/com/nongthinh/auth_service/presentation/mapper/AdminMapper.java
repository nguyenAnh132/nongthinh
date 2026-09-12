package com.nongthinh.auth_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.auth_service.application.command.RegisterAdminCommand;
import com.nongthinh.auth_service.presentation.dto.request.RegisterAdminRequest;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    RegisterAdminCommand toRegisterAdminCommand(RegisterAdminRequest request);
}
