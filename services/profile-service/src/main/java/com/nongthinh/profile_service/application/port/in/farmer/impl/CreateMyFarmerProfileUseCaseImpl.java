package com.nongthinh.profile_service.application.port.in.farmer.impl;

import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.farmer.CreateMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.FarmerProfileCreationUseCase;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileCreationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMyFarmerProfileUseCaseImpl implements CreateMyFarmerProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FarmerProfileCreationUseCase farmerProfileCreationUseCase;

    @Override
    public void execute(MyFarmerProfileCreationRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        farmerProfileCreationUseCase.execute(new FarmerProfileCreationCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.gender(),
                request.phone(),
                request.provinceId(),
                request.communeId(),
                request.addressDetail(),
                request.avatarUrl()
        ));
    }
}
