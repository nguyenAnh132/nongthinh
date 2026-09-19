package com.nongthinh.profile_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.profile_service.application.command.CompleteRegistrationProfileCommand;
import com.nongthinh.profile_service.presentation.dto.request.internal.CompleteRegistrationProfileRequest;

@Mapper(componentModel = "spring")
public interface RegistrationProfileMapper {
    CompleteRegistrationProfileCommand toCommand(CompleteRegistrationProfileRequest request);
}
