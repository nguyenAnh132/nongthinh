package com.nongthinh.profile_service.presentation.mapper;

import org.mapstruct.Mapper;

import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;
import com.nongthinh.profile_service.application.command.admin.AdminProfileUpdateCommand;
import com.nongthinh.profile_service.presentation.dto.request.AdminProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.AdminProfileUpdateRequest;

@Mapper(componentModel = "spring")
public interface AdminProfileMapper {

    AdminProfileCreationCommand toAdminProfileCreationCommand(AdminProfileCreationRequest request);

    AdminProfileUpdateCommand toAdminProfileUpdateCommand(AdminProfileUpdateRequest request);
}
