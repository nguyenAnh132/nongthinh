package com.nongthinh.profile_service.application.port.in.farmer;

import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileUpdateRequest;

public interface UpdateMyFarmerProfileUseCase {

    FarmerProfileView execute(MyFarmerProfileUpdateRequest request);
}
