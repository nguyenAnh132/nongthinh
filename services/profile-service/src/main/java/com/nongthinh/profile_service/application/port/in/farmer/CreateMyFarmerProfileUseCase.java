package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileCreationRequest;

public interface CreateMyFarmerProfileUseCase {

    void execute(MyFarmerProfileCreationRequest request);
}
