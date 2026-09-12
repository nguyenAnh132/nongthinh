package com.nongthinh.profile_service.presentation.mapper;

import org.mapstruct.Mapper;

import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;
import com.nongthinh.profile_service.application.command.farmer.FarmerProfileUpdateCommand;
import com.nongthinh.profile_service.application.command.farmer.UpdateMyFarmerAddressCommand;
import com.nongthinh.profile_service.presentation.dto.request.FarmerProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.AddressUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.FarmerProfileUpdateRequest;

@Mapper(componentModel = "spring")
public interface FarmerProfileMapper {

    FarmerProfileCreationCommand toFarmerProfileCreationCommand(FarmerProfileCreationRequest request);

    FarmerProfileUpdateCommand toFarmerProfileUpdateCommand(FarmerProfileUpdateRequest request);

    UpdateMyFarmerAddressCommand toUpdateMyFarmerAddressCommand(AddressUpdateRequest request);
}
