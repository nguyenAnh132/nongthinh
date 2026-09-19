package com.nongthinh.auth_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;
import com.nongthinh.auth_service.presentation.dto.request.CompleteRegistrationRequest;

@Mapper(componentModel = "spring")
public interface RegistrationMapper {
    CompleteRegistrationCommand toCommand(CompleteRegistrationRequest request);
}
