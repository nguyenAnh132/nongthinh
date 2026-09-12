package com.nongthinh.profile_service.presentation.mapper;

import org.mapstruct.Mapper;

import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;
import com.nongthinh.profile_service.application.command.brand.BrandProfileUpdateCommand;
import com.nongthinh.profile_service.application.command.brand.UpdateMyBrandOfficeAddressCommand;
import com.nongthinh.profile_service.presentation.dto.request.BrandProfileCreationRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.AddressUpdateRequest;
import com.nongthinh.profile_service.presentation.dto.request.update.BrandProfileUpdateRequest;

@Mapper(componentModel = "spring")
public interface BrandProfileMapper {

    BrandProfileCreationCommand toBrandProfileCreationCommand(BrandProfileCreationRequest request);

    BrandProfileUpdateCommand toBrandProfileUpdateCommand(BrandProfileUpdateRequest request);

    UpdateMyBrandOfficeAddressCommand toUpdateMyBrandOfficeAddressCommand(AddressUpdateRequest request);
}
